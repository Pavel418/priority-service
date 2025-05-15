package org.volunteer.server.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.volunteer.server.model.VolunteerPreference;

@Service
public class PreferenceService {

    private final ConcurrentMap<String, VolunteerPreference> store = new ConcurrentHashMap<>();

    /** create *or* update */
    public void save(VolunteerPreference vp) {
        store.put(vp.volunteerId(), vp);   // ← replaces old entry
    }
    
    // Keep for backward compatibility if needed
    public void upsert(String id, VolunteerPreference pref) {
        save(pref);
    }

    public Collection<VolunteerPreference> snapshot() {
        return List.copyOf(store.values());
    }
    
    public List<VolunteerPreference> orderedSnapshot() {
        return store.values().stream()
                .sorted(Comparator.comparing(VolunteerPreference::volunteerId))
                .toList();
    }
} 