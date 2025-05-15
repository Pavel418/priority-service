// Path: src/main/java/org/volunteer/server/service/GAService.java
package org.volunteer.server.service;

import org.springframework.stereotype.Service;
import org.volunteer.server.ga.GeneticAlgorithm;
import org.volunteer.server.ga.ProblemInstance;
import org.volunteer.server.model.ServiceMeta;
import org.volunteer.server.model.VolunteerPreference;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Thin façade that turns the current snapshot into a {@link ProblemInstance}
 * and runs the GA on a background executor.
 */
@Service
public class GAService {

    private final ExecutorService executor;

    public GAService(ExecutorService gaExecutor) {
        this.executor = gaExecutor;
    }

    /**
     * Launch optimisation asynchronously and return a future with the best gene
     * vector (service index for each volunteer) once the GA finishes.
     */
    public CompletableFuture<int[]> solveAsync(Collection<VolunteerPreference> prefs,
                                               List<ServiceMeta> services) {

        /* serviceId -> index lookup table */
        Map<String, Integer> svcIndex = new HashMap<>();
        for (int i = 0; i < services.size(); i++) {
            svcIndex.put(services.get(i).id(), i);
        }

        /* penalty constant from the spec: 10 × Ns², with Ns ≤ 5 → 10 is enough */
        int preferencePenalty = 10;

        ProblemInstance instance = new ProblemInstance(
                List.copyOf(prefs),
                services,
                svcIndex,
                preferencePenalty
        );

        /* run GA on the dedicated thread and return the future */
        return CompletableFuture.supplyAsync(() -> GeneticAlgorithm.run(instance), executor);
    }
}
