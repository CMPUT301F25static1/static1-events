package com.static1.fishylottery.model.repositories;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.static1.fishylottery.model.entities.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * This class abstracts the Firestore handling of events.
 */
public class EventRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference eventsRef = db.collection("events");

    /**
     * Adds a new event to the events collection in Firestore.
     *
     * @param event The event to add to the Firebase.
     * @return Returns the event with the new ID.
     */
    public Task<Event> addEvent(Event event) {
        return eventsRef.add(event)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentReference doc = task.getResult();
                    event.setEventId(doc.getId());
                    return event;
                });
    }

    /**
     * Updates an event in Firebase by overriding the document.
     *
     * @param event The new event object to update with.
     * @return A task indicating success or failure.
     */
    public Task<Void> updateEvent(Event event) {
        String eventId = event.getEventId();

        if (eventId == null) {
            throw new IllegalArgumentException("Event missing eventId");
        }

        return eventsRef.document(eventId).set(event);
    }

    /**
     * Deletes an event from the Firestore.
     *
     * @param event The event object, only the ID is needed.
     * @return A task indicating success or failure.
     */
    public Task<Void> deleteEvent(Event event) {
        String eventId = event.getEventId();

        if (eventId == null) {
            throw new IllegalArgumentException("Event missing eventId");
        }

        return eventsRef.document(eventId).delete();
    }

    /**
     * Get a single event by the ID.
     *
     * @param eventId The ID for the event.
     * @return A task containing an event. Event is null if does not exist.
     */
    public Task<Event> getEventById(String eventId) {
        return eventsRef.document(eventId).get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            DocumentSnapshot doc = task.getResult();

            if (doc.exists()) {
                Event event = doc.toObject(Event.class);

                if (event != null) {
                    event.setEventId(doc.getId());
                    return event;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        });
    }

    /**
     * Fetch all of the events from the database as is (so search criteria)
     *
     * @return Returns a task containing a list of events
     */
    public Task<List<Event>> fetchAllEvents() {
        return eventsRef.get().continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    List<Event> events = new ArrayList<>();

                    QuerySnapshot snapshot = task.getResult();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(doc.getId());
                            events.add(event);
                        }
                    }

                    return Tasks.forResult(events);
                });
    }

    /**
     * Fetch all of the events that are hosted by a particular user given their uid.
     *
     * @param uid The Firebase UID of the authenticated organizer user.
     * @return A task containing the list of events.
     */
    public Task<List<Event>> fetchEventsByOrganizerId(String uid) {
        return eventsRef
                .whereEqualTo("organizerId", uid)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return Tasks.forResult(new ArrayList<>());
                    }

                    List<Event> events = new ArrayList<>();

                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        Event event = doc.toObject(Event.class);

                        if (event != null) {
                            event.setEventId(doc.getId());
                            events.add(event);
                        }
                    }

                    return Tasks.forResult(events);
        });
    }
}
