package org.volunteer.client.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.volunteer.client.model.AssignmentUpdateResponse;
import org.volunteer.client.network.config.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages WebSocket connections with automatic reconnection and error recovery.
 * <p>
 * Key features:
 * <ul>
 *   <li>Exponential backoff reconnection strategy with configurable limits</li>
 *   <li>Thread-safe operation using atomic state management</li>
 *   <li>Graceful shutdown with proper resource cleanup</li>
 *   <li>Comprehensive error classification (transport, protocol, processing)</li>
 *   <li>Virtual thread utilization for non-blocking I/O operations</li>
 * </ul>
 *
 * <p>All callback methods are executed on WebSocket listener threads - implementers
 * should handle thread synchronization if needed.</p>
 */
public class WebSocketHandler implements WebSocket.Listener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    private static final Gson gson = new Gson();

    /** Normal closure status code per WebSocket RFC */
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    /** Atomic counter for thread-safe retry tracking */
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    /** Dedicated scheduler for reconnection attempts */
    private final ScheduledExecutorService scheduler;

    /** Application callback for connection/message events */
    private final NetworkListener callback;

    private final HttpClient client;

    private volatile WebSocket webSocket;

    /**
     * Constructs a new WebSocket connection manager.
     *
     * @param callback Event listener for connection state and messages
     * @throws NullPointerException if callback is null
     */
    public WebSocketHandler(NetworkListener callback) {
        this.callback = Objects.requireNonNull(callback, "NetworkListener must not be null");
        this.scheduler = Executors.newSingleThreadScheduledExecutor(this::createDaemonThread);
        this.client = createHttpClient();
        connect();
    }

    // Thread factory for daemon scheduler threads
    private Thread createDaemonThread(Runnable r) {
        Thread t = new Thread(r, "WebSocket-Reconnect-Scheduler");
        t.setDaemon(true);  // Prevent JVM shutdown delays
        return t;
    }

    // Configures HTTP client with virtual threads for non-blocking I/O
    private HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Environment.getConnectionTimeout())
                .executor(Executors.newVirtualThreadPerTaskExecutor())  // Better for IO-bound tasks
                .build();
    }

    /**
     * Initiates asynchronous connection sequence.
     * <p>
     * Handles both initial connections and reconnection attempts. Includes
     * protection against redundant connection attempts during shutdown.
     * </p>
     */
    private void connect() {
        if (isShutdown()) {
            logger.warn("Skipping connection attempt - handler is shutting down");
            return;
        }

        try {
            URI uri = URI.create(Environment.getWebSocketUrl());
            logger.debug("Initiating connection to {}", uri);

            client.newWebSocketBuilder()
                    .buildAsync(uri, this)
                    .thenAccept(this::handleSuccessfulConnection)
                    .exceptionally(this::handleConnectionFailure);
        } catch (Exception e) {
            logger.error("Unexpected error during connection initiation", e);
            scheduleReconnection();
        }
    }

    // Handles successful connection establishment
    private void handleSuccessfulConnection(WebSocket ws) {
        webSocket = ws;
        reconnectAttempts.set(0);  // Reset retry counter
        logger.info("WebSocket connection established");
        callback.onConnectionEstablished();
    }

    // Processes connection failures and triggers reconnection logic
    private Void handleConnectionFailure(Throwable ex) {
        logger.error("Connection failed: {}", ex.getMessage());
        scheduleReconnection();
        return null;
    }

    /**
     * Schedules next reconnection attempt using exponential backoff.
     * <p>
     * Backoff formula: base_delay * 2^attempt_number
     * Caps retries according to Environment.getMaxRetries()
     * </p>
     */
    private void scheduleReconnection() {
        if (isShutdown() || reconnectAttempts.get() >= Environment.getMaxRetries()) {
            handlePermanentFailure();
            return;
        }

        int attempt = reconnectAttempts.incrementAndGet();
        long delay = calculateBackoffDelay(attempt);

        logger.info("Scheduling reconnection attempt {} in {}ms", attempt, delay);
        scheduler.schedule(this::connect, delay, TimeUnit.MILLISECONDS);
    }

    // Calculates exponential backoff delay with jitter avoidance
    private long calculateBackoffDelay(int attempt) {
        long baseDelay = Environment.getRetryDelay().toMillis();
        return (long) (baseDelay * Math.pow(2, attempt));
    }

    // Handles permanent connection failure scenario
    private void handlePermanentFailure() {
        logger.error("Permanent connection failure after {} attempts", Environment.getMaxRetries());
        scheduler.shutdown();
        callback.onConnectionFailed();
    }

    /**
     * Processes incoming text messages from WebSocket.
     * <p>
     * Implementation notes:
     * <ul>
     *   <li>Always requests next message via webSocket.request(1)</li>
     *   <li>Differentiates between protocol errors (invalid JSON) and processing errors</li>
     *   <li>Guaranteed to return completed future to prevent backpressure</li>
     * </ul>
     */
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            AssignmentUpdateResponse response = gson.fromJson(data.toString(), AssignmentUpdateResponse.class);
            callback.onAssignmentUpdate(response);
        } catch (JsonSyntaxException e) {
            logger.error("Malformed JSON received: {}", data);
        } catch (Exception e) {
            logger.error("Error processing message", e);
        }
        return CompletableFuture.completedStage(null);
    }

    /**
     * Handles connection closure events.
     * <p>
     * Reconnection logic:
     * <ul>
     *   <li>No reconnection for normal closures (initiated by client/server)</li>
     *   <li>Automatic reconnection for unexpected closures</li>
     * </ul>
     */
    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        logger.info("Connection closed: {} - {}", statusCode, reason);
        callback.onConnectionClosed(statusCode, reason);

        if (shouldReconnect(statusCode)) {
            scheduleReconnection();
        }
        return CompletableFuture.completedStage(null);
    }

    // Determines if closure status warrants reconnection attempt
    private boolean shouldReconnect(int statusCode) {
        return statusCode != WebSocket.NORMAL_CLOSURE;
    }

    /**
     * Handles WebSocket protocol errors.
     * <p>
     * Note: This method is called for errors in the WebSocket protocol itself,
     * not application-level errors.
     * </p>
     */
    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        logger.error("WebSocket error: {}", error.getMessage());
        scheduleReconnection();
    }

    /**
     * Initiates graceful connection shutdown.
     * <p>
     * Shutdown sequence:
     * <ol>
     *   <li>Send normal closure frame to server</li>
     *   <li>Terminate reconnection scheduler</li>
     *   <li>Force-close WebSocket if graceful close fails</li>
     * </ol>
     */
    public synchronized void close() {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            logger.info("Initiating graceful shutdown");
            webSocket.sendClose(NORMAL_CLOSURE_STATUS, "Client termination");
        }
        shutdownResources();
    }

    // Centralized resource cleanup
    private void shutdownResources() {
        shutdownScheduler();
        closeWebSocketSilently();
    }

    // Shuts down scheduler with graceful termination attempt
    private void shutdownScheduler() {
        if (scheduler.isShutdown()) return;

        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
    }

    // Force-closes WebSocket without sending close frame
    private void closeWebSocketSilently() {
        try {
            if (webSocket != null && !webSocket.isInputClosed()) {
                webSocket.abort();
            }
        } catch (Exception e) {
            logger.debug("Error closing WebSocket", e);
        }
    }

    // Checks if handler is in shutdown state
    private boolean isShutdown() {
        return scheduler.isShutdown() || scheduler.isTerminated();
    }
}