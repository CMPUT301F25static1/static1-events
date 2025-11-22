package com.static1.fishylottery.model.repositories;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
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
    private static final String ENTRANT_WAITLISTS = "entrantWaitlists";

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

        if (eventId == null) {
            return Tasks.forException(new Exception("Event ID cannot be null"));
        }

        if (profileId == null) {
            return Tasks.forException(new Exception("UID cannot be null"));
        }

        // Set the eventId for the waitlist entry
        entry.setEventId(eventId);

        // Create a batch to set/update both waitlist reference documents
        WriteBatch batch = db.batch();

        // We need to store the waitlist entry in the events waitlist sub-collection
        DocumentReference eventSideRef =
                db.collection(EVENTS)
                    .document(eventId)
                    .collection(WAITLIST)
                    .document(profileId);

        // We also need to store the waitlist entry in the user's waitlist store of events
        DocumentReference entrantSideRef =
                db.collection(ENTRANT_WAITLISTS)
                    .document(profileId)
                    .collection(EVENTS)
                    .document(eventId);

        // Batch set
        batch.set(eventSideRef, entry, SetOptions.merge());
        batch.set(entrantSideRef, entry, SetOptions.merge());

        // Batch commit
        return batch.commit();
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

    public Task<List<WaitlistEntry>> getEventWaitlistEntriesByUser(@NonNull String uid) {
        return db.collection(ENTRANT_WAITLISTS)
                .document(uid)
                .collection(EVENTS)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    List<WaitlistEntry> list = new ArrayList<>();

                    for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                        WaitlistEntry entry = documentSnapshot.toObject(WaitlistEntry.class);

                        if (entry != null) {
                            entry.setEventId(documentSnapshot.getId());
                            list.add(entry);
                        }
                    }

                    return list;
                });
    }

    /**
     * Deletes a waitlist entry from the Firebase references to event waitlist and entrant waitlists.
     *
     * @param event The event for which the user is on the waitlist.
     * @param uid The uid of the user for which they are on the waitlist.
     * @return A task indicating success or failure.
     */
    public Task<Void> deleteFromWaitlist(@NonNull Event event, @NonNull String uid) {
        WriteBatch batch = db.batch();

        String eventId = event.getEventId();

        if (eventId == null) {
            return Tasks.forException(new Exception("Event ID cannot be null"));
        }

        // Delete the event waitlist
        DocumentReference eventWaitlistRef =
                db.collection(EVENTS)
                        .document(eventId)
                        .collection(WAITLIST)
                        .document(uid);

        // Delete the user's waitlist reference for the event
        DocumentReference entrantWaitlistRef =
                db.collection(ENTRANT_WAITLISTS)
                        .document(uid)
                        .collection(EVENTS)
                        .document(eventId);

        // Batch delete
        batch.delete(eventWaitlistRef);
        batch.delete(entrantWaitlistRef);

        // Batch commit
        return batch.commit();
    }

    public Task<Void> deleteFromWaitlistByUser(@NonNull String uid) {
        return db.collection(ENTRANT_WAITLISTS)
            .document(uid)
            .collection(EVENTS)
            .get()
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                QuerySnapshot snapshot = task.getResult();

                List<WaitlistEntry> waitlists = snapshot.toObjects(WaitlistEntry.class);

                Log.d("Waitlist", waitlists.get(0).getEventId());

                WriteBatch batch = db.batch();

                // For each of the waitlist entries for each user, also remove the waitlist entries for that event
                for (WaitlistEntry entry : waitlists) {
                    String eventId = entry.getEventId();
                    if (eventId == null) continue;

                    DocumentReference docRefToDelete = db.collection(EVENTS)
                        .document(eventId)
                        .collection(WAITLIST)
                        .document(uid);

                    batch.delete(docRefToDelete);
                }

                // Finally, remove the user's waitlist entries
                DocumentReference entrantWaitlistDocRef = db.collection(ENTRANT_WAITLISTS).document(uid);
                batch.delete(entrantWaitlistDocRef);

                // Commit the batch
                return batch.commit();
        });
    }
}
