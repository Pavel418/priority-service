package org.volunteer.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.volunteer.server.web.PlainAssignmentHandler;

@Configuration
@EnableWebSocket
public class PlainWebSocketConfig implements WebSocketConfigurer {

    private final PlainAssignmentHandler handler;

    public PlainWebSocketConfig(PlainAssignmentHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry reg) {
        /* Client's environment.properties points to ws://host:8765/ (root path) */
        reg.addHandler(handler, "/")
           .setAllowedOriginPatterns("*");     // loosen for demo; tighten in prod
    }
} 