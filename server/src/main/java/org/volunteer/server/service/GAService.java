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

/**
 * Manages genetic algorithm optimization runs with concurrency control.
 * <p>
 * Executes GA tasks asynchronously using a dedicated executor service. Ensures
 * only one optimization runs at a time by canceling previous tasks on new requests.
 * Results are delivered via CompletableFuture for asynchronous consumption.
 */
@Service
public class GAService {

    private final ExecutorService executor;

    /**
     * Tracks the active GA task's execution handle. Volatile ensures cross-thread
     * visibility of state changes. Null when no active task.
     */
    private volatile Future<?> currentTask;

    /**
     * Constructs the service with a dedicated GA executor.
     *
     * @param gaExecutor executor service configured for GA workloads
     */
    public GAService(ExecutorService gaExecutor) {
        this.executor = gaExecutor;
    }

    /**
     * Initiates a new GA optimization, canceling any in-progress run.
     * <p>
     * Atomic operation due to method synchronization. Previous task receives
     * thread interruption if still running. Optimization penalty weights and
     * service indexing are configured internally.
     *
     * @param prefs current volunteer preferences snapshot
     * @param services available services for assignment
     * @return CompletableFuture that completes with optimized gene sequence or
     *         fails with execution exception
     */
    public synchronized CompletableFuture<int[]> solveAsync(Collection<VolunteerPreference> prefs,
                                                            List<ServiceMeta> services) {
        // Cancel previous optimization if active
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }

        // Configure service lookup index
        Map<String, Integer> svcIndex = new HashMap<>();
        for (int i = 0; i < services.size(); i++) {
            svcIndex.put(services.get(i).id(), i);
        }

        // Build problem instance with fixed penalty weight
        ProblemInstance instance = new ProblemInstance(
                List.copyOf(prefs),
                services,
                svcIndex,
                10  // Fixed preference penalty weight
        );

        // Submit new optimization task
        CompletableFuture<int[]> resultFuture = new CompletableFuture<>();
        currentTask = executor.submit(() -> {
            try {
                int[] genes = GeneticAlgorithm.run(instance);
                resultFuture.complete(genes);
            } catch (Exception ex) {
                resultFuture.completeExceptionally(ex);
            }
        });

        return resultFuture;
    }
}