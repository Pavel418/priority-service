// Path: src/main/java/org/volunteer/server/ga/FitnessCalculator.java
package org.volunteer.server.util;

import org.volunteer.server.model.VolunteerPreference;
import org.volunteer.server.model.ProblemInstance;
import org.volunteer.server.model.ServiceMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Computes total dissatisfaction cost of a chromosome. */
final class FitnessCalculator {

    static double totalCost(int[] genes, ProblemInstance inst) {
        double cost = 0.0;
        Map<String,Integer> svcLoad = new HashMap<>();
        List<VolunteerPreference> vols = inst.volunteers();

        for (int i = 0; i < genes.length; i++) {
            int svcIdx = genes[i];
            ServiceMeta svc = inst.services().get(svcIdx);
            svcLoad.merge(svc.id(), 1, Integer::sum);

            VolunteerPreference vp = vols.get(i);
            List<String> prefs = vp.rankedServiceIds();
            String assignedId = svc.id();

            int rank = prefs.indexOf(assignedId);
            if (rank >= 0) {
                cost += Math.pow(rank, 2);                         // (i‑1)²  rank is zero‑based
            } else {
                int Ns = prefs.size();
                cost += inst.preferencePenalty() * Math.pow(Ns, 2);
            }
        }

        /* Heavy penalty if any capacity is exceeded → drives repair */
        for (ServiceMeta s : inst.services()) {
            int load = svcLoad.getOrDefault(s.id(), 0);
            if (load > s.maxCapacity()) {
                cost += 1000.0 * (load - s.maxCapacity());
            }
        }
        return cost;
    }

    private FitnessCalculator() {}
} 