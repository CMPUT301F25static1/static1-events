package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.model.entities.Event;

import java.util.ArrayList;
import java.util.List;

public class FakeEventRepository implements IEventRepository {
    private List<Event> events = new ArrayList<>();
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
}
