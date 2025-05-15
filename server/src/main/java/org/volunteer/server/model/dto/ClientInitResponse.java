package org.volunteer.server.model.dto;

import java.util.List;
import java.util.UUID;
import org.volunteer.server.model.ServiceMeta;

/**
 * Response DTO for client initialization requests.
 * Contains the client ID and available services.
 */
public record ClientInitResponse(
    String clientId,
    List<ServiceMeta> services
) { } 