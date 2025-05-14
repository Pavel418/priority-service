package org.volunteer.server.dto;

import java.util.List;

import jakarta.validation.constraints.Size;

/** Matches the JSON payload sent by the Swing client. */
public record PreferenceUpdateRequest(
        String clientId,
        @Size(min = 1, max = 5) List<String> preferences,
        String volunteerName                // optional, can be null
) {}