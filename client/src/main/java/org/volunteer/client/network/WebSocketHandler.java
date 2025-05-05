package org.volunteer.client.network;

import org.volunteer.client.network.config.Environment;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketHandler implements Listener {
    private WebSocket webSocket;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final NetworkListener callback;
    private int reconnectAttempts = 0;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Environment.getConnectionTimeout())
            .build();

    public WebSocketHandler(NetworkListener callback) {
        this.callback = callback;
        connect();
    }

    private void connect() {
        client.newWebSocketBuilder()
                .buildAsync(URI.create(Environment.getWebSocketUrl()), this)
                .thenAccept(ws -> {
                    webSocket = ws;
                    reconnectAttempts = 0;
                    callback.onConnectionEstablished();
                })
                .exceptionally(ex -> {
                    scheduleReconnection();
                    return null;
                });
    }

    private void scheduleReconnection() {
        if (reconnectAttempts < Environment.getMaxRetries()) {
            long delay = Environment.getRetryDelay().toMillis() * (reconnectAttempts + 1);
            scheduler.schedule(this::connect, delay, TimeUnit.MILLISECONDS);
            reconnectAttempts++;
        } else {
            callback.onConnectionFailed();
        }
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        callback.onAssignmentUpdate(data.toString());
        return CompletableFuture.completedStage(null);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        callback.onConnectionClosed(statusCode, reason);
        scheduleReconnection();
        return CompletableFuture.completedStage(null);
    }

    public void close() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client shutdown");
        }
        scheduler.shutdown();
    }
}