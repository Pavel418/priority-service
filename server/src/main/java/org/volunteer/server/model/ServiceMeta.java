// Path: src/main/java/org/volunteer/server/model/ServiceMeta.java
package org.volunteer.server.model;

// import java.io.Serializable; // Temporarily remove if causing linter issues with records

/** Immutable definition of a service slot exposed to volunteers. */
public record ServiceMeta(
        String id,
        String name,
        String description,
        int maxCapacity
) {
    // Records are implicitly final and cannot be abstract.
    // All fields are implicitly final.
    // Canonical constructor, getters, equals(), hashCode(), and toString() are auto-generated.
}