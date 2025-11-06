package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores a user's intent to join the waitlist under: events/{eventId}/waitlist/{profileId}
 */
public class WaitlistRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> joinWaitlist(String eventId, String profileId) {
        DocumentReference ref = db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(profileId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("profileId", profileId);
        payload.put("joinedAt", Timestamp.now());

        return ref.set(payload, SetOptions.merge());
    }
}
