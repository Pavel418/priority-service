package org.volunteer.client.model;

import com.google.gson.annotations.SerializedName;

public record Service(
        @SerializedName("id") String serviceId,
        @SerializedName("name") String serviceName,
        @SerializedName("description") String serviceDescription,
        @SerializedName("maxCapacity") int maxVolunteers
) {
    public String toString() {
        return serviceName;
    }
}