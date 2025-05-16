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

@RestController
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    // ---- matches GET /client/initialize expected by Swing app
    @GetMapping("/client/initialize")
    public ClientInitResponse initializeClient() {
        return clientService.initializeClient();
    }

    // ---- matches POST /preferences expected by Swing app - Updated signature and logic
    @PostMapping("/preferences")
    public ResponseEntity<Void> updatePreference(@Valid @RequestBody PreferenceUpdateRequest request) {
        clientService.updatePreference(request);
        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }
} 