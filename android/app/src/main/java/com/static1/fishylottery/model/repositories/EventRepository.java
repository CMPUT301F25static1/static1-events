package com.static1.fishylottery.model.repositories;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.static1.fishylottery.model.entities.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * System chooses a specified number of entrants for the event.
     * Logic:
     * - Read event.selectCount from the event document.
     * - If selectCount <= 0 -> error (cannot draw).
     * - Read events/{eventId}/waitlist/*
     * - Shuffle and select min(selectCount, waitlistSize).
     * - For each selected:
     *      - Write events/{eventId}/selectedEntrants/{profileId}
     *      - Mark their waitlist doc with selected = true
     * - Save selectedEntrants: [profileId, ...] on the event document.
     */
    public Task<Void> drawEntrants(String eventId) {
        if (eventId == null) {
            return Tasks.forException(new IllegalArgumentException("eventId is null"));
        }

        DocumentReference eventRef = eventsRef.document(eventId);

        // 1) Load event to read selectCount
        return eventRef.get().continueWithTask(eventTask -> {
            if (!eventTask.isSuccessful()) {
                throw eventTask.getException();
            }

            DocumentSnapshot eventSnap = eventTask.getResult();
            if (!eventSnap.exists()) {
                throw new IllegalStateException("Event not found");
            }

            Long selectCountLong = eventSnap.getLong("selectCount");
            int n = (selectCountLong == null) ? 0 : selectCountLong.intValue();

            if (n <= 0) {
                return Tasks.forException(
                        new IllegalStateException("selectCount must be > 0 to run draw"));
            }

            // 2) Load waitlist
            CollectionReference waitlistRef = eventRef.collection("waitlist");
            return waitlistRef.get().continueWithTask(waitTask -> {
                if (!waitTask.isSuccessful()) {
                    throw waitTask.getException();
                }

                QuerySnapshot qs = waitTask.getResult();
                List<DocumentSnapshot> docs = qs.getDocuments();

                if (docs.isEmpty()) {
                    return Tasks.forException(
                            new IllegalStateException("No entrants on waitlist"));
                }

                // 3) Shuffle and select up to n
                Collections.shuffle(docs);
                int limit = Math.min(n, docs.size());

                WriteBatch batch = db.batch();
                CollectionReference selectedRef =
                        eventRef.collection("selectedEntrants");
                List<String> selectedIds = new ArrayList<>();

                for (int i = 0; i < limit; i++) {
                    DocumentSnapshot d = docs.get(i);
                    String profileId = d.getId();
                    selectedIds.add(profileId);

                    // Write to selectedEntrants
                    DocumentReference selDoc = selectedRef.document(profileId);
                    Map<String, Object> data = new HashMap<>();
                    data.put("profileId", profileId);
                    data.put("selectedAt", FieldValue.serverTimestamp());
                    batch.set(selDoc, data, SetOptions.merge());

                    // Mark waitlist doc as selected
                    Map<String, Object> mark = new HashMap<>();
                    mark.put("selected", true);
                    batch.set(d.getReference(), mark, SetOptions.merge());
                }

                // 4) Store IDs on event doc for quick reference
                Map<String, Object> update = new HashMap<>();
                update.put("selectedEntrants", selectedIds);
                batch.set(eventRef, update, SetOptions.merge());

                return batch.commit();
            });
        });
    }
}

