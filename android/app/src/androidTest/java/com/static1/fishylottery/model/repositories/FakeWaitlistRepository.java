package com.static1.fishylottery.model.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Fake in-memory implementation with per-event waitlist storage.
 */
public class FakeWaitlistRepository implements IWaitlistRepository {

    // Map from eventId -> list of waitlist entries
    private final Map<String, List<WaitlistEntry>> eventWaitlists;

    public FakeWaitlistRepository() {
        this.eventWaitlists = new HashMap<>();
    }

    /**
     * Pre-populate with entries for testing.
     */
    public FakeWaitlistRepository(Map<String, List<WaitlistEntry>> initialData) {
        this.eventWaitlists = new HashMap<>(initialData);
    }

    @Override
    public Task<Void> addToWaitlist(@NonNull Event event, @NonNull WaitlistEntry entry) {
        List<WaitlistEntry> list = eventWaitlists.computeIfAbsent(event.getEventId(), k -> new ArrayList<>());
        list.add(entry);
        return Tasks.forResult(null);
    }

    @Override
    public Task<List<WaitlistEntry>> getWaitlist(@NonNull Event event) {
        List<WaitlistEntry> list = eventWaitlists.getOrDefault(event.getEventId(), new ArrayList<>());
        return Tasks.forResult(new ArrayList<>(list));
    }

    @Override
    public Task<WaitlistEntry> getWaitlistEntry(@NonNull Event event, String uid) {
        List<WaitlistEntry> list = eventWaitlists.getOrDefault(event.getEventId(), new ArrayList<>());
        for (WaitlistEntry e : list) {
            if (e.getProfile().getUid().equals(uid)) {
                return Tasks.forResult(e);
            }
        }
        return Tasks.forResult(null);
    }

    @Override
    public Task<List<WaitlistEntry>> getEventWaitlistEntriesByUser(@NonNull String uid) {
        List<WaitlistEntry> result = new ArrayList<>();
        for (List<WaitlistEntry> list : eventWaitlists.values()) {
            for (WaitlistEntry e : list) {
                if (e.getProfile().getUid().equals(uid)) {
                    result.add(e);
                }
            }
        }
        return Tasks.forResult(result);
    }

    @Override
    public Task<Void> deleteFromWaitlist(@NonNull Event event, @NonNull String uid) {
        List<WaitlistEntry> list = eventWaitlists.get(event.getEventId());
        if (list != null) {
            list.removeIf(e -> e.getProfile().getUid().equals(uid));
        }
        return Tasks.forResult(null);
    }

    @Override
    public Task<Void> deleteFromWaitlistByUser(@NonNull String uid) {
        for (List<WaitlistEntry> list : eventWaitlists.values()) {
            list.removeIf(e ->
                    e != null
                            && e.getProfile() != null
                            && uid.equals(e.getProfile().getUid())
            );
        }
        return Tasks.forResult(null);
    }

    @Override
    public Task<Void> updateMultipleEntries(List<WaitlistEntry> entries) {
        return null;
    }
}