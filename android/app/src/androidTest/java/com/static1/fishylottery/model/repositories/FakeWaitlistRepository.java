package com.static1.fishylottery.model.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fake in-memory implementation with per-event waitlist storage.
 *
 * Used from androidTest so we can run tests without talking to Firestore.
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

    private List<WaitlistEntry> listFor(@NonNull Event event) {
        String eventId = event.getEventId();
        if (eventId == null) {
            throw new IllegalArgumentException("eventId is null");
        }
        return eventWaitlists.computeIfAbsent(eventId, k -> new ArrayList<>());
    }

    @Override
    public Task<Void> addToWaitlist(@NonNull Event event, @NonNull WaitlistEntry entry) {
        listFor(event).add(entry);
        return Tasks.forResult(null);
    }

    /**
     * Same contract as the real repository: honour event.maxWaitlistSize.
     * Counts entries whose status is "waiting" or "invited".
     */
    @Override
    public Task<Void> addToWaitlistRespectingLimit(
            @NonNull Event event,
            @NonNull WaitlistEntry entry
    ) {
        List<WaitlistEntry> list = listFor(event);

        Integer max = event.getMaxWaitlistSize();
        if (max != null && max > 0) {
            int activeCount = 0;
            for (WaitlistEntry e : list) {
                if (e == null) continue;
                String status = e.getStatus();
                if (status == null) continue;

                String s = status.trim().toLowerCase(Locale.ROOT);
                if ("waiting".equals(s) || "invited".equals(s)) {
                    activeCount++;
                }
            }
            if (activeCount >= max) {
                return Tasks.forException(
                        new IllegalStateException("Waitlist is full")
                );
            }
        }

        list.add(entry);
        return Tasks.forResult(null);
    }

    @Override
    public Task<List<WaitlistEntry>> getWaitlist(@NonNull Event event) {
        List<WaitlistEntry> list =
                eventWaitlists.getOrDefault(event.getEventId(), new ArrayList<>());
        // Return a copy so tests don’t accidentally mutate internal state
        return Tasks.forResult(new ArrayList<>(list));
    }

    @Override
    public Task<WaitlistEntry> getWaitlistEntry(@NonNull Event event, String uid) {
        List<WaitlistEntry> list =
                eventWaitlists.getOrDefault(event.getEventId(), new ArrayList<>());
        for (WaitlistEntry e : list) {
            if (e != null
                    && e.getProfile() != null
                    && uid.equals(e.getProfile().getUid())) {
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
                if (e != null
                        && e.getProfile() != null
                        && uid.equals(e.getProfile().getUid())) {
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
            list.removeIf(e ->
                    e != null
                            && e.getProfile() != null
                            && uid.equals(e.getProfile().getUid())
            );
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

}
