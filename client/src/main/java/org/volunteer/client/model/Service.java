package org.volunteer.client.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serial;
import java.io.Serializable;

public record Service(
        @SerializedName("id") String serviceId,
        @SerializedName("name") String serviceName,
        @SerializedName("description") String serviceDescription,
        @SerializedName("maxCapacity") int maxVolunteers
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return serviceName;
    }
}
