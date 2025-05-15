package org.volunteer.server.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.volunteer.server.data.ServiceCatalog;
import org.volunteer.server.model.ServiceMeta;
import org.volunteer.server.model.VolunteerPreference;
import org.volunteer.server.model.dto.AssignmentDto;
import org.volunteer.server.model.dto.AssignmentUpdateResponse;
import org.volunteer.server.web.PlainAssignmentHandler;

/**
 * Coordinates volunteer assignment optimization using genetic algorithms.
 * <p>
 * Automatically triggers on preference changes and broadcasts optimized assignments
 * via WebSocket. Requires at least 3 preferences to initiate optimization. All
 * operations are asynchronous and non-blocking.
 */
@Service
public class AssignmentService {

    private final PreferenceService preferenceService;
    private final GAService gaService;
    private final ServiceCatalog catalog;
    private final PlainAssignmentHandler plainWs;

    /**
     * Constructs the service with required dependencies.
     *
     * @param prefSvc preference storage and retrieval service
     * @param gaSvc genetic algorithm optimization engine
     * @param catalog service metadata catalog
     * @param plainWs WebSocket handler for broadcasting assignments
     */
    public AssignmentService(PreferenceService prefSvc,
                             GAService gaSvc,
                             ServiceCatalog catalog,
                             PlainAssignmentHandler plainWs) {
        this.preferenceService = prefSvc;
        this.gaService = gaSvc;
        this.catalog = catalog;
        this.plainWs = plainWs;
    }

    /**
     * Initiates optimization workflow when preferences change.
     * <p>
     * Automatic trigger that requires minimum 3 preferences to start. Optimizes
     * asynchronously and broadcasts results through WebSocket upon completion.
     */
    public void startOptimisation() {
        List<VolunteerPreference> snapshot = preferenceService.orderedSnapshot();
        if (snapshot.size() < 3) return;  // Minimum viable population threshold

        gaService.solveAsync(snapshot, catalog.all())
                .thenAccept(genes -> handleResult(snapshot, genes));
    }

    /**
     * Transforms genetic algorithm results into broadcast-ready assignments.
     *
     * @param vpList snapshot used for optimization (preserves order consistency)
     * @param genes optimized service indices from genetic algorithm
     */
    private void handleResult(List<VolunteerPreference> vpList, int[] genes) {
        List<ServiceMeta> services = catalog.all();
        List<AssignmentDto> out = new ArrayList<>(genes.length);

        for (int i = 0; i < genes.length; i++) {
            VolunteerPreference vp = vpList.get(i);
            ServiceMeta svc = services.get(genes[i]);
            out.add(new AssignmentDto(vp.volunteerId(),
                    vp.volunteerName(),
                    svc));
        }

        plainWs.broadcast(new AssignmentUpdateResponse(out));
    }
}