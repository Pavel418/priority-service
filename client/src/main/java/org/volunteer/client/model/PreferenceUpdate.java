package org.volunteer.client.model;

import com.google.gson.annotations.SerializedName;
import org.volunteer.client.session.SessionManager;

import java.util.List;
import java.util.Objects;

/**
 * Represents a volunteer's service preference submission.
 * <p>
 * Automatically includes the client's identity and validates preferences before submission.
 * </p>
 */
public final class PreferenceUpdate {
    @SerializedName("clientId")
    private final String clientId;

    @SerializedName("preferences")
    private final List<String> serviceIds;

    /**
     * Creates a new preference submission with automatic client ID inclusion.
     *
     * @param serviceIds Ordered list of service IDs (1st = highest preference)
     * @throws IllegalStateException if client ID isn't initialized
     * @throws IllegalArgumentException if preferences are invalid
     */
    public PreferenceUpdate(List<String> serviceIds) {
        this.clientId = SessionManager.getClientId();
        this.serviceIds = validatePreferences(serviceIds);
    }

    private List<String> validatePreferences(List<String> serviceIds) {
        Objects.requireNonNull(serviceIds, "Service IDs cannot be null");

        if (serviceIds.size() > 5) {
            throw new IllegalArgumentException("Maximum 5 preferences allowed");
        }

        if (serviceIds.stream().distinct().count() != serviceIds.size()) {
            throw new IllegalArgumentException("Duplicate preferences not allowed");
        }

        return List.copyOf(serviceIds); // Return immutable copy
    }

    // Gson constructor for deserialization
    private PreferenceUpdate() {
        this.clientId = null;
        this.serviceIds = null;
    }

    public String getClientId() {
        return clientId;
    }

    public List<String> getServiceIds() {
        return List.copyOf(serviceIds); // Defensive copy
    }

    @Override
    public String toString() {
        return "PreferenceUpdate{" +
                "clientId='" + clientId + '\'' +
                ", serviceIds=" + serviceIds +
                '}';
    }
}