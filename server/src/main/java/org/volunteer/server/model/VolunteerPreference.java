package org.volunteer.server.model;

import java.io.Serializable;
import java.util.List;

/** Represents a volunteer's preference list. */
public final class VolunteerPreference implements Serializable {
    private final String volunteerId;
    private final String volunteerName; // may be blank
    private final List<String> rankedServiceIds; // size <= 5, order = preference

    public VolunteerPreference(String volunteerId, String volunteerName, List<String> rankedServiceIds) {
        this.volunteerId = volunteerId;
        this.volunteerName = volunteerName;
        this.rankedServiceIds = List.copyOf(rankedServiceIds); // Make immutable
    }

    public String volunteerId() {
        return volunteerId;
    }

    public String volunteerName() {
        return volunteerName;
    }

    public List<String> rankedServiceIds() {
        return rankedServiceIds;
    }

    // Optionally add equals(), hashCode(), toString()
} 