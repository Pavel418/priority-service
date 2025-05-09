package org.volunteer.client.model;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

public record Assignment(
        @SerializedName("volunteerId") String volunteerId,
        @SerializedName("service") Service assignedService,
        @SerializedName("assignedAt") Instant timestamp
) {}
