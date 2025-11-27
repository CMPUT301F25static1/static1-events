package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.static1.fishylottery.model.entities.Event;
import java.util.List;

/**
 * This class abstracts the Firestore handling of events.
 */
public interface IEventRepository {
    public Task<Event> addEvent(Event event);
    public Task<Void> updateEvent(Event event);
    public Task<Void> deleteEvent(Event event);
    public Task<Event> getEventById(String eventId);
    public Task<List<Event>> fetchAllEvents();
    public Task<List<Event>> fetchEventsByOrganizerId(String uid);
}