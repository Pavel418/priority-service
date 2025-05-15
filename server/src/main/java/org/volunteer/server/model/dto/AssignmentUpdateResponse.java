package org.volunteer.server.model.dto;

import java.util.List;

 
/** Broadcast body pushed to all WebSocket subscribers. */
public record AssignmentUpdateResponse(List<AssignmentDto> assignments) { }
