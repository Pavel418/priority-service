package org.volunteer.server.model;


/** Immutable definition of a service slot exposed to volunteers. */
public record ServiceMeta(
        String id,
        String name,
        String description,
        int maxCapacity
) {}