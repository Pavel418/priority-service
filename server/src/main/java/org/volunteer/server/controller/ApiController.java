// Path: src/main/java/org/volunteer/server/controller/ApiController.java
package org.volunteer.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.volunteer.server.dto.PreferenceUpdateRequest;
import org.volunteer.server.model.VolunteerPreference;
import org.volunteer.server.service.AssignmentService;
import org.volunteer.server.service.PreferenceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final PreferenceService prefSvc;
    private final AssignmentService assignSvc;

    public ApiController(PreferenceService prefSvc, AssignmentService assignSvc) {
        this.prefSvc = prefSvc;
        this.assignSvc = assignSvc;
    }

    @PutMapping("/preferences/{volId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertPreferences(@PathVariable String volId,
                                  @Valid @RequestBody PreferenceUpdateRequest req) {

        VolunteerPreference vp = new VolunteerPreference(
                volId,
                req.volunteerName() == null ? "" : req.volunteerName(),
                List.copyOf(req.preferences()) // Use List.copyOf for immutability
        );
        prefSvc.save(vp);
        assignSvc.startOptimisation();         // auto-trigger
    }

    @GetMapping("/assignment/current")
    public Object currentAssignment() { // Return type Object to match last.get()
        return assignSvc.current();
    }
} 