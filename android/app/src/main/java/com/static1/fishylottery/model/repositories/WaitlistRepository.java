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
import java.util.Date;
import java.util.List;

/**
 * Stores a user's intent to join the waitlist under: events/{eventId}/waitlist/{profileId}
 */
public class WaitlistRepository implements IWaitlistRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String EVENTS = "events";
    private static final String WAITLIST = "waitlist";
    private static final String ENTRANT_WAITLISTS = "entrantWaitlists";
    /**
     * Adds an entrant to the waitlist for the given event and mirrors the entry
     * under {@code entrantWaitlists/{uid}/events/{eventId}}.
     * <p>
     * Both documents are written in a single Firestore batch.
     *
     * @param event the event whose waitlist is being joined; its {@code eventId} is used
     * @param entry the {@link WaitlistEntry} containing profile and status information
     * @return a {@link Task} that completes when the batch write is committed
     */
    @Override
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
     * Loads all waitlist entries for a specific event.
     *
     * @param event the event whose waitlist should be read
     * @return a {@link Task} that resolves to a list of {@link WaitlistEntry} objects;
     *         the list is empty if no entries exist
     * @throws IllegalArgumentException if the event has a {@code null} {@code eventId}
     */
    @Override
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
    /**
     * Retrieves a single waitlist entry for the given event and user ID.
     *
     * @param event the event whose waitlist entry is requested
     * @param uid   the profile ID / document ID of the entrant
     * @return a {@link Task} that resolves to the {@link WaitlistEntry} or {@code null}
     *         if no corresponding document exists
     */
    @Override
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

                    return doc.toObject(WaitlistEntry.class);
                });
    }
    /**
     * Retrieves all waitlist entries for a user across all events.
     * <p>
     * Reads documents from {@code entrantWaitlists/{uid}/events} and sets the
     * {@code eventId} field on each entry using the document ID.
     *
     * @param uid the profile ID of the user
     * @return a {@link Task} that resolves to a list of {@link WaitlistEntry} objects
     */
    @Override
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
     * Removes a single entrant from the waitlist for an event and deletes the
     * mirrored document under {@code entrantWaitlists}.
     * <p>
     * Both deletions are performed in a single Firestore batch.
     *
     * @param event the event from which the entrant is removed
     * @param uid   the profile ID / document ID of the entrant
     * @return a {@link Task} that completes when the batch delete is committed
     */
    @Override
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
    /**
     * Deletes all waitlist references for a given user across all events.
     * <p>
     * For each event listed under {@code entrantWaitlists/{uid}/events}, the corresponding
     * {@code events/{eventId}/waitlist/{uid}} document is also removed. Finally, the
     * {@code entrantWaitlists/{uid}} document itself is deleted.
     *
     * @param uid the profile ID of the user whose waitlist entries should be removed
     * @return a {@link Task} that completes when all delete operations have been committed
     */
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

                WriteBatch batch = db.batch();

                // For each of the waitlist entries for each user, also remove the waitlist entries for that event
                for (WaitlistEntry entry : waitlists) {
                    Log.d("Waitlist", "Deleting waitlist entry for event ID: " + entry.getEventId());

                    String eventId = entry.getEventId();
                    if (eventId == null) continue;

                    DocumentReference eventWaitlistDocRef = db.collection(EVENTS)
                        .document(eventId)
                        .collection(WAITLIST)
                        .document(uid);

                    DocumentReference entrantWaitlistDocRef = db.collection(ENTRANT_WAITLISTS)
                        .document(uid)
                        .collection(EVENTS)
                        .document(eventId);

                    batch.delete(eventWaitlistDocRef);
                    batch.delete(entrantWaitlistDocRef);
                }

                // Finally, remove the user's waitlist entries
                DocumentReference entrantWaitlistDocRef = db.collection(ENTRANT_WAITLISTS).document(uid);
                batch.delete(entrantWaitlistDocRef);

                // Commit the batch
                return batch.commit();
        });
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

    /**
     * Marks an invited entrant as declined and, if possible, invites a replacement
     * entrant from the remaining waitlist for the same event.
     *
     * Logic:
     *  1. Mark the current entrant as "declined" on BOTH:
     *      - events/{eventId}/waitlist/{uid}
     *      - entrantWaitlists/{uid}/events/{eventId}
     *  2. If they were in "invited" status, look for a replacement:
     *      - Scan the event's waitlist for entries with status == "waiting"
     *      - Choose the earliest joined (by joinedAt) as the replacement
     *      - Mark that replacement as "invited" and set invitedAt (both sides)
     *
     * This is done inside a Firestore transaction so the update + replacement
     * selection are consistent.
     */
    @Override
    public Task<Void> declineInvitationAndDrawReplacement(@NonNull Event event, @NonNull String uid) {
        String eventId = event.getEventId();
        if (eventId == null) {
            return Tasks.forException(new Exception("Event ID cannot be null"));
        }

        // References for this entrant's waitlist docs (both sides)
        DocumentReference eventWaitlistRef = db.collection(EVENTS)
                .document(eventId)
                .collection(WAITLIST)
                .document(uid);

        DocumentReference entrantWaitlistRef = db.collection(ENTRANT_WAITLISTS)
                .document(uid)
                .collection(EVENTS)
                .document(eventId);

        // 1) Get the current entry so we know its previous status
        return eventWaitlistRef.get().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            DocumentSnapshot snapshot = task.getResult();
            if (snapshot == null || !snapshot.exists()) {
                // Nothing to do if the entry doesn't exist
                return Tasks.forResult(null);
            }

            WaitlistEntry entry = snapshot.toObject(WaitlistEntry.class);
            if (entry == null) {
                return Tasks.forResult(null);
            }

            String previousStatus = entry.getStatus();
            Date now = new Date();

            // 2) Mark this entrant as declined on BOTH sides in a batch
            WriteBatch batch = db.batch();
            batch.update(eventWaitlistRef,
                    "status", "declined",
                    "declinedAt", now);
            batch.update(entrantWaitlistRef,
                    "status", "declined",
                    "declinedAt", now);

            return batch.commit().continueWithTask(commitTask -> {
                if (!commitTask.isSuccessful()) {
                    throw commitTask.getException();
                }

                // If they weren't invited, we don't need to draw a replacement
                if (!"invited".equals(previousStatus)) {
                    return Tasks.forResult(null);
                }

                // 3) Find a replacement from remaining "waiting" entries
                return db.collection(EVENTS)
                        .document(eventId)
                        .collection(WAITLIST)
                        .get()
                        .continueWithTask(waitlistTask -> {
                            if (!waitlistTask.isSuccessful()) {
                                throw waitlistTask.getException();
                            }

                            QuerySnapshot allEntriesSnapshot = waitlistTask.getResult();
                            if (allEntriesSnapshot == null || allEntriesSnapshot.isEmpty()) {
                                return Tasks.forResult(null);
                            }

                            DocumentSnapshot replacementDoc = null;
                            Date earliestJoined = null;

                            // Pick the "waiting" entry with the earliest joinedAt
                            for (DocumentSnapshot doc : allEntriesSnapshot.getDocuments()) {
                                WaitlistEntry e = doc.toObject(WaitlistEntry.class);
                                if (e == null) continue;
                                if (!"waiting".equals(e.getStatus())) continue;

                                Date joined = e.getJoinedAt();
                                if (replacementDoc == null) {
                                    replacementDoc = doc;
                                    earliestJoined = joined;
                                } else if (joined != null &&
                                        (earliestJoined == null || joined.before(earliestJoined))) {
                                    replacementDoc = doc;
                                    earliestJoined = joined;
                                }
                            }

                            if (replacementDoc == null) {
                                // No "waiting" entrants left
                                return Tasks.forResult(null);
                            }

                            String replacementUid = replacementDoc.getId();
                            DocumentReference replacementEventRef = replacementDoc.getReference();
                            DocumentReference replacementEntrantRef = db.collection(ENTRANT_WAITLISTS)
                                    .document(replacementUid)
                                    .collection(EVENTS)
                                    .document(eventId);

                            Date inviteTime = new Date();

                            // 4) Promote the replacement to invited on BOTH sides
                            WriteBatch promoteBatch = db.batch();
                            promoteBatch.update(replacementEventRef,
                                    "status", "invited",
                                    "invitedAt", inviteTime);
                            promoteBatch.update(replacementEntrantRef,
                                    "status", "invited",
                                    "invitedAt", inviteTime);

                            return promoteBatch.commit();
                        });
            });
        });
    }
    /**
     * Writes multiple waitlist entries in a single batch operation, updating both
     * the event-side and entrant-side documents for each entry.
     *
     * @param entries a list of {@link WaitlistEntry} objects to write; each must have
     *                a non-null {@code eventId} and {@link WaitlistEntry#getProfile()} UID
     * @return a {@link Task} that completes when the batch has been committed
     */
    @Override
    public Task<Void> updateMultipleEntries(List<WaitlistEntry> entries) {
        WriteBatch batch = db.batch();

        for (WaitlistEntry entry : entries) {
            String eventId = entry.getEventId();
            String uid = entry.getProfile().getUid();

            DocumentReference eventWaitlistEntryRef = db.collection(EVENTS).document(eventId).collection(WAITLIST).document(uid);
            DocumentReference entrantWaitlistRef = db.collection(ENTRANT_WAITLISTS).document(uid).collection(EVENTS).document(eventId);

            batch.set(eventWaitlistEntryRef, entry, SetOptions.merge());
            batch.set(entrantWaitlistRef, entry, SetOptions.merge());
        }
        return batch.commit();
    }
}
