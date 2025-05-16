package org.volunteer.client.model;

import com.google.gson.annotations.SerializedName;

public record Assignment(
        @SerializedName("volunteerId") String volunteerId,
        @SerializedName("service") Service assignedService
) {}
