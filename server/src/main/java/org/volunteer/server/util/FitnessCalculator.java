// Path: src/main/java/org/volunteer/server/ga/FitnessCalculator.java
package org.volunteer.server.util;

import org.volunteer.server.model.VolunteerPreference;
import org.volunteer.server.model.ProblemInstance;
import org.volunteer.server.model.ServiceMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates fitness scores for chromosomes based on preference satisfaction and capacity constraints.
 * <p>
 * Combines quadratic ranking penalties with heavy capacity violation penalties to guide the genetic algorithm
 * toward valid, preference-optimized solutions. All calculations are stateless and thread-safe.
 */
final class FitnessCalculator {

    /**
     * Computes total dissatisfaction cost for a volunteer-service assignment sequence.
     * <p>
     * Cost components:
     * <ul>
     *   <li>Quadratic penalty for non-optimal preference rankings (rank² for matched preferences)</li>
     *   <li>Fixed penalty multiplier for assignments outside volunteer preferences</li>
     *   <li>Linear heavy penalties for service capacity overflows (1000× per overflow unit)</li>
     * </ul>
     *
     * @param genes volunteer-to-service assignment indices
     * @param inst problem context containing services, preferences, and penalty rules
     * @return aggregated cost score where lower values indicate better solutions
     */
    static double totalCost(int[] genes, ProblemInstance inst) {
        double cost = 0.0;
        Map<String,Integer> svcLoad = new HashMap<>();
        List<VolunteerPreference> vols = inst.volunteers();

        // Calculate preference-based costs
        for (int i = 0; i < genes.length; i++) {
            int svcIdx = genes[i];
            ServiceMeta svc = inst.services().get(svcIdx);
            svcLoad.merge(svc.id(), 1, Integer::sum);

            VolunteerPreference vp = vols.get(i);
            List<String> prefs = vp.rankedServiceIds();
            String assignedId = svc.id();

            int rank = prefs.indexOf(assignedId);
            if (rank >= 0) {
                cost += Math.pow(rank, 2);  // Quadratic penalty for preference depth
            } else {
                int Ns = prefs.size();
                cost += inst.preferencePenalty() * Math.pow(Ns, 2);  // Fixed penalty multiplier
            }
        }

        // Apply capacity constraint penalties
        for (ServiceMeta s : inst.services()) {
            int load = svcLoad.getOrDefault(s.id(), 0);
            if (load > s.maxCapacity()) {
                cost += 1000.0 * (load - s.maxCapacity());  // Strong discouragement for overflows
            }
        }
        return cost;
    }

    /** Prevents instantiation - this is a utility class. */
    private FitnessCalculator() {}
}