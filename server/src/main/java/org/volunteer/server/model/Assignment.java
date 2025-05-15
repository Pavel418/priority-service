package org.volunteer.server.model;

/** Final volunteer → service mapping produced by the GA. */
public record Assignment(
        String volunteerId,
        String volunteerName,
        String serviceId
) {} 
