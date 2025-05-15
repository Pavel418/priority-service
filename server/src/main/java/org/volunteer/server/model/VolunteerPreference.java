package org.volunteer.server.model;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a volunteer's preference list.
 */
public record VolunteerPreference(String volunteerId,
                                  List<String> rankedServiceIds) implements Serializable {
    public VolunteerPreference(String volunteerId, List<String> rankedServiceIds) {
        this.volunteerId = volunteerId;
        this.rankedServiceIds = List.copyOf(rankedServiceIds);
    }
} 