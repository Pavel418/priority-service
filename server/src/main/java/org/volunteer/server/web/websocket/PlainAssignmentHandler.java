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

/**
 * Keeps all live WebSocket sessions and pushes JSON strings to them.
 */
@Component
public class PlainAssignmentHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus status) {
        sessions.remove(s);
    }

    /** The server never expects to receive any text. */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // ignore – protocol is server-push only
    }

    /* ---------------- internal API ---------------- */

    public void broadcast(Object payload) {
        try {
            String json = mapper.writeValueAsString(payload);
            TextMessage msg = new TextMessage(json);
            sessions.forEach(s -> safeSend(s, msg));
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void safeSend(WebSocketSession s, TextMessage m) {
        try { if (s.isOpen()) s.sendMessage(m); }
        catch (IOException ignored) { sessions.remove(s); }
    }
} 