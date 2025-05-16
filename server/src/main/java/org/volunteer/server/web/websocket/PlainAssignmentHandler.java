package org.volunteer.server.web.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

/**
 * Manages WebSocket connections for broadcasting assignment updates to clients.
 * <p>
 * Maintains thread-safe tracking of active sessions and provides one-way server-push
 * functionality. Automatically cleans up closed connections. All operations are
 * non-blocking and concurrent session access is supported.
 */
@Component
public class PlainAssignmentHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Registers new WebSocket connections in the active sessions pool.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    /**
     * Removes closed WebSocket connections from the active sessions pool.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus status) {
        sessions.remove(s);
    }

    /**
     * No-op implementation as this handler only supports server-to-client pushes.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Protocol designed for unilateral server updates
    }

    /**
     * Broadcasts serialized JSON payload to all active WebSocket sessions.
     * <p>
     * Converts payload to JSON string atomically before distribution. Failed
     * serializations log errors without propagating exceptions. Closed connections
     * are automatically purged during broadcast attempts.
     *
     * @param payload data object to broadcast; must be JSON-serializable
     */
    public void broadcast(Object payload) {
        try {
            String json = mapper.writeValueAsString(payload);
            TextMessage msg = new TextMessage(json);
            sessions.forEach(s -> safeSend(s, msg));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Payload serialization failed", e);
        }
    }

    /**
     * Safely attempts message delivery while handling connection state changes.
     * Silently removes stale sessions from the active pool.
     */
    private void safeSend(WebSocketSession s, TextMessage m) {
        try {
            if (s.isOpen()) s.sendMessage(m);
        } catch (IOException ignored) {
            sessions.remove(s);
        }
    }
}