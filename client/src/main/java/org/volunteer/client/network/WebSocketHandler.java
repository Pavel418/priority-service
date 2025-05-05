package org.volunteer.client.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.volunteer.client.network.config.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.*;

/**
 * Handles WebSocket connectivity and automatic reconnection for the volunteer client.
 * <p>
 * This class manages the full lifecycle of WebSocket connections including:
 * - Initial connection establishment
 * - Exponential backoff reconnection strategy
 * - Message handling delegation
 * - Graceful shutdown procedures
 * - Error handling and status propagation through callbacks
 * </p>
 *
 * Implements {@link Listener} to receive WebSocket events and delegates application-specific
 * handling to the provided {@link NetworkListener}.
 */
public class WebSocketHandler implements Listener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    private WebSocket webSocket;
    private final ScheduledExecutorService scheduler;
    private final NetworkListener callback;
    private int reconnectAttempts = 0;
    private final HttpClient client;

    /**
     * Constructs a new WebSocketHandler with the specified network event callback.
     *
     * @param callback The listener implementation for handling network events
     *                and message reception. Must not be null.
     */
    public WebSocketHandler(NetworkListener callback) {
        this.callback = callback;
        // Configure single-threaded scheduler with daemon threads to prevent JVM blocking
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("WebSocket-Reconnect-Scheduler");
            t.setDaemon(true);  // Allow JVM to exit without waiting for scheduler
            return t;
        });
        // Initialize HTTP client with configured connection timeout
        this.client = HttpClient.newBuilder()
                .connectTimeout(Environment.getConnectionTimeout())
                .build();
        // Initiate first connection attempt
        connect();
    }

    /**
     * Initiates a new WebSocket connection using the URL from environment configuration.
     * <p>
     * This method is called both for initial connection and subsequent reconnection attempts.
     * Uses asynchronous completion handlers to avoid blocking the calling thread.
     * </p>
     */
    private void connect() {
        URI uri = URI.create(Environment.getWebSocketUrl());
        logger.info("Attempting WebSocket connection to {}", uri);

        // Asynchronous connection attempt with completion handlers
        client.newWebSocketBuilder()
                .buildAsync(uri, this)
                .thenAccept(this::onConnected)
                .exceptionally(this::onConnectionFailure);
    }

    /**
     * Handles successful WebSocket connection establishment.
     *
     * @param ws The connected WebSocket instance
     */
    private void onConnected(WebSocket ws) {
        this.webSocket = ws;
        this.reconnectAttempts = 0;  // Reset retry counter on successful connection
        logger.info("WebSocket connection established.");
        callback.onConnectionEstablished();
    }

    /**
     * Handles connection failure and initiates reconnection logic.
     *
     * @param ex The exception that caused the connection failure
     * @return null to satisfy CompletableFuture exception handling
     */
    private Void onConnectionFailure(Throwable ex) {
        logger.error("WebSocket connection failed: {}", ex.getMessage(), ex);
        scheduleReconnection();
        return null;
    }

    /**
     * Schedules a reconnection attempt using exponential backoff strategy.
     * <p>
     * The delay between attempts increases linearly with each retry up to the
     * maximum configured number of retries. After max retries are exceeded,
     * permanent failure is reported to the callback.
     * </p>
     */
    private void scheduleReconnection() {
        if (reconnectAttempts < Environment.getMaxRetries()) {
            // Calculate delay with linear backoff: delay * (attempt + 1)
            long delay = Environment.getRetryDelay().toMillis() * (reconnectAttempts + 1);
            logger.info("Scheduling reconnection attempt {} after {}ms", reconnectAttempts + 1, delay);
            scheduler.schedule(this::connect, delay, TimeUnit.MILLISECONDS);
            reconnectAttempts++;
        } else {
            logger.warn("Max reconnection attempts ({}) reached. Giving up.", Environment.getMaxRetries());
            callback.onConnectionFailed();
        }
    }

    /**
     * Receives and processes text messages from the WebSocket.
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            logger.debug("Received message: {}", data);
            // Delegate message processing to application callback
            callback.onAssignmentUpdate(data.toString());
        } catch (Exception e) {
            logger.error("Error handling WebSocket message", e);
        }
        return CompletableFuture.completedStage(null);
    }

    /**
     * Handles WebSocket closure events and initiates reconnection.
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        logger.info("WebSocket closed. Status: {}, Reason: {}", statusCode, reason);
        callback.onConnectionClosed(statusCode, reason);
        scheduleReconnection();  // Attempt to reconnect on unexpected closure
        return CompletableFuture.completedStage(null);
    }

    /**
     * Handles WebSocket protocol errors.
     * {@inheritDoc}
     */
    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        logger.error("WebSocket error occurred", error);
    }

    /**
     * Initiates graceful shutdown of the WebSocket connection and associated resources.
     * <p>
     * Sends normal closure frame to server and terminates reconnection scheduler.
     * Should be called during application shutdown to ensure clean exit.
     * </p>
     */
    public void close() {
        if (webSocket != null) {
            logger.info("Closing WebSocket connection.");
            // Send normal closure (status 1000) with shutdown reason
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client shutdown");
        }
        shutdownScheduler();
    }

    /**
     * Shuts down the reconnection scheduler with graceful termination.
     * <p>
     * Allows up to 5 seconds for existing tasks to complete before forcing shutdown.
     * Ensures application can exit even if scheduler tasks are hanging.
     * </p>
     */
    private void shutdownScheduler() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Scheduler did not terminate in time. Forcing shutdown.");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted during scheduler shutdown", e);
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
    }
}