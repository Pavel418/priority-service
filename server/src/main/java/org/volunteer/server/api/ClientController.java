// --------------  api/ClientController.java
package org.volunteer.server.api;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.volunteer.server.data.ServiceCatalog;
import org.volunteer.server.dto.ClientInitResponse;
import org.volunteer.server.dto.PreferenceUpdateRequest;
import org.volunteer.server.model.VolunteerPreference;
import org.volunteer.server.service.AssignmentService;
import org.volunteer.server.service.PreferenceService;

import jakarta.validation.Valid;

@RestController
public class ClientController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private final ServiceCatalog catalog;
    private final PreferenceService prefSvc;
    private final AssignmentService assignSvc;

    public ClientController(ServiceCatalog catalog,
                            PreferenceService prefSvc,
                            AssignmentService assignSvc) {
        this.catalog = catalog;
        this.prefSvc = prefSvc;
        this.assignSvc = assignSvc;
    }

    // ---- matches GET /client/initialize expected by Swing app
    @GetMapping("/client/initialize")
    public ClientInitResponse init() {
        return new ClientInitResponse(UUID.randomUUID().toString(), catalog.all());
    }

    // ---- matches POST /preferences expected by Swing app - Updated signature and logic
    @PostMapping("/preferences")
    public ResponseEntity<Void> upsert(@Valid @RequestBody PreferenceUpdateRequest req) {
        // Use clientId directly as the volunteer ID (string)
        VolunteerPreference vp = new VolunteerPreference(
                req.clientId(),                // volunteerId  (string form)
                req.volunteerName(),           // may be null / empty
                List.copyOf(req.preferences()) // ranked list (≤5)
        );
    
        prefSvc.save(vp);            // use the new save method directly
        assignSvc.startOptimisation(); // kicks GA
    
        return ResponseEntity.ok().build();
    }
} 