package org.volunteer.server.dto;

import org.volunteer.server.model.ServiceMeta;
 
public record AssignmentDto(
        String volunteerId,
        String volunteerName,
        ServiceMeta service
) {} 
