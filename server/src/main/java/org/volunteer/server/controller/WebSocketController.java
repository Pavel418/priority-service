// Path: src/main/java/org/volunteer/server/controller/WebSocketController.java
package org.volunteer.server.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * Placeholder if you want clients to actively request optimisation via
 * STOMP /app/optimize.  Currently unused – optimisation is auto-triggered.
 */
@Controller
public class WebSocketController {

    @MessageMapping("/optimize")
    public void noop() { /* no-op – kept for future extension */ }
} 