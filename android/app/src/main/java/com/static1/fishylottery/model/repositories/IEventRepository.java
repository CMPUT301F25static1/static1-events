package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.static1.fishylottery.model.entities.Event;
import java.util.List;

/**
 * This class abstracts the Firestore handling of events.
 */
public interface IEventRepository {
    Task<Event> addEvent(Event event);
    Task<Void> updateEvent(Event event);
    Task<Void> deleteEvent(Event event);
    Task<Event> getEventById(String eventId);
    Task<List<Event>> fetchAllEvents();
    Task<List<Event>> fetchEventsByOrganizerId(String uid);
    public Task<Void> drawEntrants(String eventId);
    Task<List<String>> fetchCancelledEntrantIds(String eventId);
    Task<Void> cancelSelectedEntrant(String eventId, String profileId);
}