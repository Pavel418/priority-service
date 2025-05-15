package org.volunteer.server.service;

import org.springframework.stereotype.Service;
import org.volunteer.server.ga.GeneticAlgorithm;
import org.volunteer.server.ga.ProblemInstance;
import org.volunteer.server.model.ServiceMeta;
import org.volunteer.server.model.VolunteerPreference;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class GAService {

    private final ExecutorService executor;

    /**
     * Holds the Future for the currently running GA task (if any).
     * Volatile so visibility is guaranteed across threads.
     */
    private volatile Future<?> currentTask;

    public GAService(ExecutorService gaExecutor) {
        this.executor = gaExecutor;
    }

    /**
     * Launch optimisation asynchronously.  If a previous run is
     * still in-flight, cancel it immediately (via interruption).
     */
    public synchronized CompletableFuture<int[]> solveAsync(Collection<VolunteerPreference> prefs,
                                                            List<ServiceMeta> services) {
        // 1) Cancel any in-progress GA
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }

        // 2) Build the problem instance
        Map<String, Integer> svcIndex = new HashMap<>();
        for (int i = 0; i < services.size(); i++) {
            svcIndex.put(services.get(i).id(), i);
        }
        int preferencePenalty = 10;
        ProblemInstance instance = new ProblemInstance(
                List.copyOf(prefs),
                services,
                svcIndex,
                preferencePenalty
        );

        // 3) Submit new GA task, capturing its Future
        CompletableFuture<int[]> resultFuture = new CompletableFuture<>();
        currentTask = executor.submit(() -> {
            try {
                int[] genes = GeneticAlgorithm.run(instance);
                resultFuture.complete(genes);
            } catch (InterruptedException e) {
                // Task was cancelled: propagate cancellation
                resultFuture.cancel(true);
            } catch (Exception ex) {
                // Any other error
                resultFuture.completeExceptionally(ex);
            }
        });

        return resultFuture;
    }
}
