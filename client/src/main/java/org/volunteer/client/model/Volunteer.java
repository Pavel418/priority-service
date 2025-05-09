package org.volunteer.client.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record Volunteer(
        @SerializedName("volunteerId") String id,
        @SerializedName("displayName") String name,
        @SerializedName("preferences") List<Service> rankedPreferences,
        @SerializedName("currentAssignment") Assignment activeAssignment,
        @SerializedName("assignmentHistory") List<Assignment> pastAssignments
) {
    public int getPreferenceIndex(Service service) {
        return rankedPreferences.indexOf(service);
    }
}