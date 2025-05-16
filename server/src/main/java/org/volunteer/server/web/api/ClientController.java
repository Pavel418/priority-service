package org.volunteer.server.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.volunteer.server.model.dto.ClientInitResponse;
import org.volunteer.server.model.dto.PreferenceUpdateRequest;
import org.volunteer.server.service.ClientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Exposes REST endpoints for client initialization and preference management.
 * <p>
 * Designed to interface with legacy Swing clients through fixed endpoint contracts.
 * Validates incoming requests and delegates business logic to the service layer.
 */
@RestController
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * Provides initial client state required for application bootstrap.
     * <p>
     * Called during client startup to retrieve essential configuration and context data.
     * Matches legacy Swing client's expected endpoint structure.
     *
     * @return complete initialization payload including services and default preferences
     */
    @GetMapping("/client/initialize")
    public ClientInitResponse initializeClient() {
        return clientService.initializeClient();
    }

    /**
     * Processes preference updates from client applications.
     * <p>
     * Validates request payload before persistence. Returns 201 Created on success to
     * match legacy client expectations. Silent validation failures return 400 Bad Request.
     *
     * @param request validated preference update payload
     * @return empty response with 201 status code
     */
    @PostMapping("/preferences")
    public ResponseEntity<Void> updatePreference(@Valid @RequestBody PreferenceUpdateRequest request) {
        clientService.updatePreference(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}