package com.static1.fishylottery.model.repositories;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class abstracts the Firestore handling of events.
 */
public class EventRepository implements IEventRepository {

    private final FirebaseFirestore db;
    private final CollectionReference eventsRef;

    public EventRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public EventRepository(FirebaseFirestore db) {
        this.db = db;
        this.eventsRef = db.collection("events");
    }

    // ---------- CRUD ----------

    @Override
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

    @Override
    public Task<Void> updateEvent(Event event) {
        String eventId = event.getEventId();
        if (eventId == null) {
            throw new IllegalArgumentException("Event missing eventId");
        }
        return eventsRef.document(eventId).set(event);
    }

    @Override
    public Task<Void> deleteEvent(Event event) {
        String eventId = event.getEventId();
        if (eventId == null) {
            throw new IllegalArgumentException("Event missing eventId");
        }
        return eventsRef.document(eventId).delete();
    }

    @Override
    public Task<Event> getEventById(String eventId) {
        return eventsRef.document(eventId).get().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            DocumentSnapshot doc = task.getResult();
            if (doc.exists()) {
                Event event = doc.toObject(Event.class);
                if (event != null) {
                    event.setEventId(doc.getId());
                    // Fetch waitlist count for this event
                    return fetchWaitlistCountForEvent(event);
                } else {
                    return Tasks.forResult(null);
                }
            } else {
                return Tasks.forResult(null);
            }
        });
    }

    @Override
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
            // Fetch waitlist counts for all events
            return fetchWaitlistCountsForEvents(events);
        });
    }

    @Override
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
                    // Fetch waitlist counts for organizer events
                    return fetchWaitlistCountsForEvents(events);
                });
    }

    // ---------- Waitlist Count Methods ----------

    /**
     * Fetches waitlist counts for multiple events in parallel.
     *
     * @param events list of events to populate
     * @return Task containing the same list with counts populated
     */
    private Task<List<Event>> fetchWaitlistCountsForEvents(List<Event> events) {
        if (events.isEmpty()) {
            return Tasks.forResult(events);
        }

        // Create a list of tasks, one for each event's waitlist count
        List<Task<Void>> countTasks = new ArrayList<>();

        for (Event event : events) {
            Task<Void> countTask = eventsRef
                    .document(event.getEventId())
                    .collection("waitlist")
                    .get()
                    .continueWith(t -> {
                        if (t.isSuccessful()) {
                            int count = t.getResult().size();
                            event.setWaitlistCount(count);
                        } else {
                            // On error, set count to 0
                            event.setWaitlistCount(0);
                        }
                        return null;
                    });
            countTasks.add(countTask);
        }

        // Wait for all count tasks to complete
        return Tasks.whenAllComplete(countTasks)
                .continueWith(task -> events);
    }

    /**
     * Fetches waitlist count for a single event.
     *
     * @param event the event to populate
     * @return Task containing the same event with count populated
     */
    private Task<Event> fetchWaitlistCountForEvent(Event event) {
        return eventsRef
                .document(event.getEventId())
                .collection("waitlist")
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        event.setWaitlistCount(count);
                    } else {
                        event.setWaitlistCount(0);
                    }
                    return event;
                });
    }

    /**
     * Gets the count of waitlist entries by status.
     * Useful for counting only "waiting", "invited", "accepted", etc.
     *
     * @param eventId the event ID
     * @param status the status to filter by
     * @return Task containing the count
     */
    public Task<Integer> getWaitlistCountByStatus(String eventId, String status) {
        return eventsRef
                .document(eventId)
                .collection("waitlist")
                .whereEqualTo("status", status)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().size();
                    }
                    return 0;
                });
    }

    // ---------- Lottery draw ----------

    @Override
    public Task<Void> drawEntrants(String eventId) {
        if (eventId == null) {
            return Tasks.forException(new IllegalArgumentException("eventId is null"));
        }
        DocumentReference eventRef = eventsRef.document(eventId);

        return eventRef.get().continueWithTask(eventTask -> {
            if (!eventTask.isSuccessful()) {
                throw eventTask.getException();
            }
            DocumentSnapshot eventSnap = eventTask.getResult();
            if (!eventSnap.exists()) {
                throw new IllegalStateException("Event not found");
            }
            Event event = eventSnap.toObject(Event.class);
            if (event == null) {
                throw new IllegalStateException("Could not parse the event");
            }

            Integer capacity = event.getCapacity();
            int n = (capacity == null) ? 0 : capacity;
            if (n <= 0) {
                return Tasks.forException(new IllegalStateException("capacity must be > 0 to run draw"));
            }

            CollectionReference waitlistRef = eventRef.collection("waitlist");
            return waitlistRef.get().continueWithTask(waitTask -> {
                if (!waitTask.isSuccessful()) {
                    throw waitTask.getException();
                }
                QuerySnapshot qs = waitTask.getResult();
                List<DocumentSnapshot> docs = qs.getDocuments();
                if (docs.isEmpty()) {
                    return Tasks.forException(new IllegalStateException("No entrants on waitlist"));
                }

                Collections.shuffle(docs);
                int limit = Math.min(n, docs.size());

                WriteBatch batch = db.batch();
                CollectionReference selectedRef = eventRef.collection("selectedEntrants");
                List<String> selectedIds = new ArrayList<>();

                for (int i = 0; i < limit; i++) {
                    DocumentSnapshot d = docs.get(i);
                    String profileId = d.getId();
                    selectedIds.add(profileId);

                    DocumentReference selDoc = selectedRef.document(profileId);
                    Map<String, Object> data = new HashMap<>();
                    data.put("profileId", profileId);
                    data.put("selectedAt", FieldValue.serverTimestamp());
                    batch.set(selDoc, data, SetOptions.merge());

                    Map<String, Object> mark = new HashMap<>();
                    mark.put("selected", true);
                    mark.put("status", "invited");
                    mark.put("invitedAt", new Date());
                    batch.set(d.getReference(), mark, SetOptions.merge());
                }

                Map<String, Object> update = new HashMap<>();
                update.put("selectedEntrants", selectedIds);
                batch.set(eventRef, update, SetOptions.merge());

                return batch.commit();
            });
        });
    }


    /**
     * Return the list of profile IDs that have cancelled their participation for this event.
     * We model this as waitlist documents whose "status" == "cancelled".
     */
    public Task<List<String>> fetchCancelledEntrantIds(String eventId) {
        if (eventId == null) {
            return Tasks.forException(new IllegalArgumentException("eventId is null"));
        }
        DocumentReference eventRef = eventsRef.document(eventId);
        CollectionReference waitlistRef = eventRef.collection("waitlist");

        return waitlistRef.whereEqualTo("status", "cancelled")
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        ids.add(doc.getId()); // doc id is the profileId
                    }
                    return ids;
                });
    }

    /**
     * Mark a selected entrant as cancelled and (optionally) backfill from waitlist.
     * Also appends the cancelled record under events/{eventId}/cancelledEntrants/{profileId}.
     */
    public Task<Void> cancelSelectedEntrant(String eventId, String profileId) {
        if (eventId == null || profileId == null) {
            return Tasks.forException(new IllegalArgumentException("eventId or profileId is null"));
        }

        DocumentReference eventRef = eventsRef.document(eventId);
        CollectionReference selectedRef = eventRef.collection("selectedEntrants");
        CollectionReference waitlistRef = eventRef.collection("waitlist");
        CollectionReference cancelledRef = eventRef.collection("cancelledEntrants");

        return waitlistRef.get().continueWithTask(waitTask -> {
            if (!waitTask.isSuccessful()) {
                throw waitTask.getException();
            }

            DocumentSnapshot replacement = null;
            for (DocumentSnapshot d : waitTask.getResult().getDocuments()) {
                Boolean sel = d.getBoolean("selected");
                if (sel == null || !sel) {
                    replacement = d;
                    break;
                }
            }

            WriteBatch batch = db.batch();

            // record the cancellation
            DocumentReference cancelledDoc = cancelledRef.document(profileId);
            Map<String, Object> cancelData = new HashMap<>();
            cancelData.put("profileId", profileId);
            cancelData.put("cancelledAt", FieldValue.serverTimestamp());
            batch.set(cancelledDoc, cancelData, SetOptions.merge());

            // remove from selected
            batch.delete(selectedRef.document(profileId));
            batch.update(eventRef, "selectedEntrants", FieldValue.arrayRemove(profileId));

            // backfill next from waitlist if available
            if (replacement != null) {
                String replId = replacement.getId();

                DocumentReference selDoc = selectedRef.document(replId);
                Map<String, Object> selData = new HashMap<>();
                selData.put("profileId", replId);
                selData.put("selectedAt", FieldValue.serverTimestamp());
                batch.set(selDoc, selData, SetOptions.merge());

                Map<String, Object> mark = new HashMap<>();
                mark.put("selected", true);
                mark.put("status", "invited");
                mark.put("invitedAt", new Date());
                batch.set(replacement.getReference(), mark, SetOptions.merge());

                batch.update(eventRef, "selectedEntrants", FieldValue.arrayUnion(replId));
            }

            return batch.commit();
        });
    }
}