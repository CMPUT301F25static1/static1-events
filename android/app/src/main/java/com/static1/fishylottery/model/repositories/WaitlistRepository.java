package com.static1.fishylottery.model.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores a user's intent to join the waitlist under: events/{eventId}/waitlist/{profileId}
 */
public class WaitlistRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> joinWaitlist(@NonNull Event event, @NonNull WaitlistEntry entry) {
        String eventId = event.getEventId();
        String profileId = entry.getProfile().getUid();
        if (eventId == null || profileId == null) {
            return Tasks.forResult(null);
        }

        DocumentReference ref = db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(profileId);

        return ref.set(entry, SetOptions.merge());
    }

    public Task<List<WaitlistEntry>> getWaitlist(@NonNull Event event) {

        String eventId = event.getEventId();

        if (eventId == null) {
            throw new IllegalArgumentException("eventId is null");
        }

        return db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    QuerySnapshot querySnapshot = task.getResult();

                    List<WaitlistEntry> list = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        WaitlistEntry entry = doc.toObject(WaitlistEntry.class);

                        if (entry != null) {
                            list.add(entry);
                        }
                    }

                    return list;
                });
    }
}
