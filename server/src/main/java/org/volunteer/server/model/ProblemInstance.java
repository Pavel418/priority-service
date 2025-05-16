// Path: src/main/java/org/volunteer/server/ga/ProblemInstance.java
package org.volunteer.server.model;

import java.util.List;
import java.util.Map;

/** Immutable snapshot of the optimisation problem at a single point in time. */
public record ProblemInstance(
        List<VolunteerPreference> volunteers,
        List<ServiceMeta> services,
        Map<String, Integer> serviceIndex,         // serviceId → position in list
        int preferencePenalty                      // constant 10 × Ns²
) {} 