package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.model.entities.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeEventRepository implements IEventRepository {
    private List<Event> events = new ArrayList<>();
    // eventId -> cancelled entrant IDs
    private final Map<String, List<String>> cancelledByEvent = new HashMap<>();
    @Override
    public Task<Event> addEvent(Event event) {
        events.add(event);
        return Tasks.forResult(event);
    }

    @Override
    public Task<Void> updateEvent(Event event) {
        events.add(event);
        return Tasks.forResult(null);
    }

    @Override
    public Task<Void> deleteEvent(Event event) {
        events.remove(event);
        return Tasks.forResult(null);
    }

    @Override
    public Task<Event> getEventById(String eventId) {
        return Tasks.forResult(events.get(0));
    }

    @Override
    public Task<List<Event>> fetchAllEvents() {
        return Tasks.forResult(events);
    }

    @Override
    public Task<List<Event>> fetchEventsByOrganizerId(String uid) {
        return Tasks.forResult(events);
    }

    @Override
    public Task<Void> drawEntrants(String eventId) {
        return null;
    }
    @Override
    public Task<List<String>> fetchCancelledEntrantIds(String eventId) {
        List<String> ids = cancelledByEvent.getOrDefault(eventId, new ArrayList<>());
        return Tasks.forResult(new ArrayList<>(ids));
    }

    @Override
    public Task<Void> cancelSelectedEntrant(String eventId, String profileId) {
        List<String> ids = cancelledByEvent.computeIfAbsent(eventId, k -> new ArrayList<>());
        if (!ids.contains(profileId)) {
            ids.add(profileId);
        }
        return Tasks.forResult(null);
    }

    // Helper methods for tests

    public void setCancelledIds(String eventId, List<String> ids) {
        cancelledByEvent.put(eventId, new ArrayList<>(ids));
    }

    public void clear() {
        events.clear();
        cancelledByEvent.clear();
    }
}
