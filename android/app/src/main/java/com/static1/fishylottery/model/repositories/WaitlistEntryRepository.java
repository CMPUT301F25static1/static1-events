package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;

public class WaitlistEntryRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> addToWaitlist(Event event, WaitlistEntry entry) {
        CollectionReference waitlistRef = getWaitlistCollection(event);
        String uid = entry.getProfile().getUid();
        return waitlistRef.document(uid).set(entry);
    }

    public Task<Void> updateWaitlist(Event event, WaitlistEntry entry) {
        CollectionReference waitlistRef = getWaitlistCollection(event);
        String uid = entry.getProfile().getUid();
        return waitlistRef.document(uid).set(entry);
    }

    public Task<Void> removeFromWaitlist(Event event, WaitlistEntry entry) {
        CollectionReference waitlistRef = getWaitlistCollection(event);
        String uid = entry.getProfile().getUid();
        return waitlistRef.document(uid).delete();
    }


    private CollectionReference getWaitlistCollection(Event event) {
        return db.collection("events").document(event.getEventId()).collection("waitlist");
    }
}
