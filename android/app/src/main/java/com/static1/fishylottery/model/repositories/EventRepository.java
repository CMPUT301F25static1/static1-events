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

public class EventRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference eventsRef = db.collection("events");

    public Task<DocumentReference> addEvent(Event event) {
        return eventsRef.add(event)
                .addOnSuccessListener(doc -> {
                    String eventId = doc.getId();
                    event.setEventId(eventId);
                });
    }

    public Task<Void> updateEvent(Event event) {
        String eventId = event.getEventId();

        if (eventId == null) {
            throw new IllegalArgumentException("Event missing eventId");
        }

        return eventsRef.document(eventId).set(event);
    }

    public Task<Void> deleteEvent(Event event) {
        String eventId = event.getEventId();

        if (eventId == null) {
            throw new IllegalArgumentException("Event missing eventId");
        }

        return eventsRef.document(eventId).delete();
    }

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
