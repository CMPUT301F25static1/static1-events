package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.static1.fishylottery.model.entities.Event;
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

    public Task<QuerySnapshot> fetchEvents() {
        return eventsRef.get();
    }
}
