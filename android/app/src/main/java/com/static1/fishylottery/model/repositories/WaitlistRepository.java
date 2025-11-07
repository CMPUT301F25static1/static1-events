package com.static1.fishylottery.model.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores a user's intent to join the waitlist under: events/{eventId}/waitlist/{profileId}
 */
public class WaitlistRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String EVENTS = "events";
    private static final String WAITLIST = "waitlist";

    /**
     * An entrant can join a waitlist with the event and the created waitlist entry containing the
     * profile of who is joining the waitlist.
     *
     * @param event The event the waitlist belongs to.
     * @param entry Information about the entrant profile and status.
     * @return A task indicating success or failure.
     */
    public Task<Void> addToWaitlist(@NonNull Event event, @NonNull WaitlistEntry entry) {
        String eventId = event.getEventId();
        String profileId = entry.getProfile().getUid();
        if (eventId == null || profileId == null) {
            return Tasks.forResult(null);
        }

        DocumentReference ref = db.collection(EVENTS)
                .document(eventId)
                .collection(WAITLIST)
                .document(profileId);

        return ref.set(entry, SetOptions.merge());
    }

    /**
     * Get the full waitlist of everyone on it for a specified event.
     *
     * @param event The event object.
     * @return A list of waitlist entries.
     */
    public Task<List<WaitlistEntry>> getWaitlist(@NonNull Event event) {

        String eventId = event.getEventId();

        if (eventId == null) {
            throw new IllegalArgumentException("eventId is null");
        }

        return db.collection(EVENTS)
                .document(eventId)
                .collection(WAITLIST)
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

    public Task<WaitlistEntry> getWaitlistEntry(@NonNull Event event, String uid) {
        return db.collection(EVENTS)
                .document(event.getEventId())
                .collection(WAITLIST)
                .document(uid)
                .get().continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentSnapshot doc = task.getResult();

                    if (!doc.exists()) {
                        return null;
                    }

                    WaitlistEntry entry = doc.toObject(WaitlistEntry.class);

                    return entry;
                });
    }

    public Task<Void> deleteFromWaitlist(@NonNull Event event, @NonNull String uid) {
        return db.collection(EVENTS)
                .document(event.getEventId())
                .collection(WAITLIST)
                .document(uid)
                .delete();
    }
}
