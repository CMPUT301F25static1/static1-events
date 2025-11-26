package com.static1.fishylottery.model.repositories;

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

    // ---------------------------------------------------------------------
    // limit-enforcing helpers
    // ---------------------------------------------------------------------

    /**
     * Adds/updates the entrant on the waitlist, but first enforces the event’s optional
     * waitlist limit if it’s enabled (Event.waitlistLimited == true and waitlistLimit != null).
     * If the entrant is already on the waitlist, it updates/merges without checking the limit.
     */
    public Task<Void> addToWaitlistRespectingLimit(@NonNull Event event, @NonNull WaitlistEntry entry) {
        String eventId = event.getEventId();
        String profileId = entry.getProfile().getUid();

        if (eventId == null) {
            return Tasks.forException(new Exception("Event ID cannot be null"));
        }
        if (profileId == null) {
            return Tasks.forException(new Exception("UID cannot be null"));
        }

        // If no limit is enabled, just use the normal path.
        if (!(Boolean.TRUE.equals(event.getWaitlistLimited()) && event.getWaitlistLimit() != null)) {
            return addToWaitlist(event, entry);
        }

        // If already present, allow update (idempotent) without limit check.
        DocumentReference docRef = db.collection(EVENTS)
                .document(eventId)
                .collection(WAITLIST)
                .document(profileId);

        return docRef.get().continueWithTask(getTask -> {
            if (!getTask.isSuccessful()) throw getTask.getException();

            DocumentSnapshot doc = getTask.getResult();
            if (doc != null && doc.exists()) {
                // Already on the waitlist → merge/update as usual.
                return addToWaitlist(event, entry);
            }

            // Not on the list yet → enforce limit.
            return getWaitlistCount(event).continueWithTask(countTask -> {
                if (!countTask.isSuccessful()) throw countTask.getException();

                long current = countTask.getResult() != null ? countTask.getResult() : 0L;
                int limit = event.getWaitlistLimit() != null ? event.getWaitlistLimit() : Integer.MAX_VALUE;

                if (current >= limit) {
                    return Tasks.forException(new IllegalStateException("Waitlist is full"));
                }
                return addToWaitlist(event, entry);
            });
        });
    }

    /**
     * Returns the current number of entries in the event’s waitlist.
     * (Simple query-size count; acceptable for small/medium lists.)
     */
    public Task<Long> getWaitlistCount(@NonNull Event event) {
        String eventId = event.getEventId();
        if (eventId == null) {
            return Tasks.forException(new Exception("Event ID cannot be null"));
        }

        return db.collection(EVENTS)
                .document(eventId)
                .collection(WAITLIST)
                .get()
                .continueWith(t -> {
                    if (!t.isSuccessful()) throw t.getException();
                    QuerySnapshot qs = t.getResult();
                    return qs != null ? (long) qs.size() : 0L;
                });
    }

    /**
     * Convenience helper that can be called from UI to decide if the “Join waitlist” button
     * should be enabled. If limit not enabled, returns true.
     */
    public Task<Boolean> isWaitlistAcceptingMore(@NonNull Event event) {
        if (!(Boolean.TRUE.equals(event.getWaitlistLimited()) && event.getWaitlistLimit() != null)) {
            return Tasks.forResult(true);
        }
        return getWaitlistCount(event).continueWith(t -> {
            if (!t.isSuccessful()) throw t.getException();
            long count = t.getResult() != null ? t.getResult() : 0L;
            return count < event.getWaitlistLimit();
        });
    }
}

