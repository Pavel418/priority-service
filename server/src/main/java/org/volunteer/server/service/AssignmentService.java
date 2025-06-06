package org.volunteer.server.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.volunteer.server.data.PreferenceStorage;
import org.volunteer.server.data.ServiceStorage;
import org.volunteer.server.model.ServiceMeta;
import org.volunteer.server.model.VolunteerPreference;
import org.volunteer.server.model.dto.AssignmentDto;
import org.volunteer.server.model.dto.AssignmentUpdateResponse;
import org.volunteer.server.web.websocket.PlainAssignmentHandler;

import lombok.RequiredArgsConstructor;

/**
 * Coordinates volunteer assignment optimization using genetic algorithms.
 * <p>
 * Automatically triggers on preference changes and broadcasts optimized assignments
 * via WebSocket. Requires at least 3 preferences to initiate optimization. All
 * operations are asynchronous and non-blocking.
 */
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final PreferenceStorage preferenceService;
    private final GeneticAlgorithmManager geneticAlgorithmManager;
    private final ServiceStorage catalog;
    private final PlainAssignmentHandler plainWs;

    /**
     * Initiates optimization workflow when preferences change.
     * <p>
     * Automatic trigger that requires minimum 3 preferences to start. Optimizes
     * asynchronously and broadcasts results through WebSocket upon completion.
     */
    public void startOptimisation() {
        List<VolunteerPreference> snapshot = preferenceService.orderedSnapshot();
        if (snapshot.size() < 3) return;  // Minimum viable population threshold

        geneticAlgorithmManager.solveAsync(snapshot, catalog.findAll())
                .thenAccept(genes -> handleResult(snapshot, genes));
    }

    /**
     * Transforms genetic algorithm results into broadcast-ready assignments.
     *
     * @param vpList snapshot used for optimization (preserves order consistency)
     * @param genes optimized service indices from genetic algorithm
     */
    private void handleResult(List<VolunteerPreference> vpList, int[] genes) {
        List<ServiceMeta> services = catalog.findAll();
        List<AssignmentDto> out = new ArrayList<>(genes.length);

        for (int i = 0; i < genes.length; i++) {
            VolunteerPreference vp = vpList.get(i);
            ServiceMeta svc = services.get(genes[i]);
            out.add(new AssignmentDto(vp.volunteerId(),
                    svc));
        }

        plainWs.broadcast(new AssignmentUpdateResponse(out));
    }
}