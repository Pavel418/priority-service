package org.volunteer.server.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.volunteer.server.model.VolunteerPreference;

/**
 * Manages thread-safe storage and retrieval of volunteer preferences.
 * <p>
 * This service maintains preferences in a concurrent map keyed by volunteer IDs,
 * ensuring atomic operations and uniqueness per volunteer. All methods are
 * thread-safe for concurrent access.
 */
@Service
public class PreferenceService {

    private final ConcurrentMap<String, VolunteerPreference> store = new ConcurrentHashMap<>();

    /**
     * Saves or updates a volunteer preference atomically.
     * <p>
     * If a preference with the same volunteer ID exists, it is replaced.
     * The entire operation is thread-safe.
     *
     * @param vp the volunteer preference to save; must not be {@code null}
     * @throws NullPointerException if {@code vp} is {@code null}
     */
    public void save(VolunteerPreference vp) {
        store.put(vp.volunteerId(), vp);
    }

    /**
     * Provides an immutable, volunteer ID-ordered snapshot of all current preferences.
     * <p>
     * The returned list is sorted lexicographically by volunteer ID and reflects
     * the state at the time of invocation. Subsequent changes to the store will not
     * affect the returned list.
     *
     * @return an unmodifiable list of preferences sorted in ascending volunteer ID order
     */
    public List<VolunteerPreference> orderedSnapshot() {
        return store.values().stream()
                .sorted(Comparator.comparing(VolunteerPreference::volunteerId))
                .toList();
    }
}