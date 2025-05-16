package org.volunteer.server.model.dto;

import java.util.List;
import org.volunteer.server.model.ServiceMeta;

import lombok.Builder;

/**
 * Response DTO for client initialization requests.
 * Contains the client ID and available services.
 */
@Builder
public record ClientInitResponse(
    String clientId,
    List<ServiceMeta> services
) { } 