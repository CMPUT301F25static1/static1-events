package com.static1.fishylottery.controller;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller to manage waitlist actions for a single event.
 *
 * Behavior:
 *  - signup(event, user): if user not on waitlist -> add as waiting and increment event.waitingCount
 *  - leaveWaitlist(event, user): if on waitlist and status == "waiting" -> remove and decrement waitingCount;
 *      if on waitlist and status == "invited" -> convert to accepted (create attendees/{uid}, update statuses and counters)
 *  - acceptInvite(event, user): if on waitlist and status == "invited" -> convert to accepted (same as above)
 *
 * All public methods return a Task<Void> so UI can observe success/failure.
 */
public class WaitlistController {

    private static final String TAG = "WaitlistController";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Optional: keep an event attribute (can be null). Methods accept Event param but controller can also store it.
    private Event event;

    public WaitlistController() {}

    public WaitlistController(Event event) {
        this.event = event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Signup the currentUser for the event's waitlist if they are not already present.
     * Returns a Task<Void> that completes on success, or fails with an Exception.
     */
    public Task<Void> signup(@NonNull Event eventParam, @NonNull Profile currentUser) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (eventParam == null || currentUser == null) {
            tcs.setException(new IllegalArgumentException("Event and user must be provided"));
            return tcs.getTask();
        }

        String eventId = eventParam.getEventId();
        if (eventId == null) {
            tcs.setException(new IllegalArgumentException("Event must have an eventId"));
            return tcs.getTask();
        }

        final DocumentReference waitRef = db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(currentUser.getUid());

        // Check if already present
        waitRef.get()
                .addOnSuccessListener((DocumentSnapshot snap) -> {
                    if (snap.exists()) {
                        tcs.setException(new IllegalStateException("User is already on the waitlist"));
                        return;
                    }

                    // add entry and increment waitingCount in a transaction
                    db.runTransaction((Transaction.Function<Void>) transaction -> {
                                Map<String, Object> entry = new HashMap<>();
                                entry.put("status", "waiting");
                                entry.put("joinedAt", FieldValue.serverTimestamp());
                                entry.put("profile", currentUser); // stores profile object fields
                                transaction.set(waitRef, entry);

                                DocumentReference eventRef = db.collection("events").document(eventId);
                                // increment waitingCount (create if missing)
                                transaction.update(eventRef, "waitingCount", FieldValue.increment(1));
                                return null;
                            }).addOnSuccessListener(aVoid -> tcs.setResult(null))
                            .addOnFailureListener(e -> tcs.setException(e));
                })
                .addOnFailureListener(e -> tcs.setException(e));

        return tcs.getTask();
    }

    /**
     * Leave the waitlist (or if invited, treat as accept).
     *
     * If the user is on the waitlist and status == "waiting" => remove their waitlist doc and decrement waitingCount.
     * If the user is on the waitlist and status == "invited" => convert to accepted (create attendees/{uid},
     * update waitlist.status = "accepted", increment attendeeCount and decrement waitingCount).
     *
     * If the user is not on the waitlist, fails with an exception.
     */
    public Task<Void> leaveWaitlist(@NonNull Event eventParam, @NonNull Profile currentUser) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (eventParam == null || currentUser == null) {
            tcs.setException(new IllegalArgumentException("Event and user must be provided"));
            return tcs.getTask();
        }
        String eventId = eventParam.getEventId();
        if (eventId == null) {
            tcs.setException(new IllegalArgumentException("Event must have an eventId"));
            return tcs.getTask();
        }

        final DocumentReference waitRef = db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(currentUser.getUid());

        waitRef.get()
                .addOnSuccessListener((DocumentSnapshot snap) -> {
                    if (!snap.exists()) {
                        tcs.setException(new IllegalStateException("User is not on the waitlist"));
                        return;
                    }

                    String status = snap.getString("status");
                    if (status == null) status = "waiting"; // default assumption

                    if ("waiting".equals(status)) {
                        // delete the waitlist entry and decrement waitingCount
                        db.runTransaction((Transaction.Function<Void>) transaction -> {
                                    transaction.delete(waitRef);
                                    DocumentReference eventRef = db.collection("events").document(eventId);
                                    transaction.update(eventRef, "waitingCount", FieldValue.increment(-1));
                                    return null;
                                }).addOnSuccessListener(aVoid -> tcs.setResult(null))
                                .addOnFailureListener(e -> tcs.setException(e));
                    } else if ("invited".equals(status)) {
                        // per your spec: if invited, treat leave action as accepted
                        doAcceptInTransaction(eventId, currentUser, waitRef)
                                .addOnSuccessListener(aVoid -> tcs.setResult(null))
                                .addOnFailureListener(e -> tcs.setException(e));
                    } else {
                        // other statuses (accepted/declined) - probably cannot leave; return error
                        tcs.setException(new IllegalStateException("Cannot leave waitlist when status = " + status));
                    }

                }).addOnFailureListener(e -> tcs.setException(e));

        return tcs.getTask();
    }

    /**
     * Accept an invitation (explicit accept). Only allowed when current waitlist status == "invited".
     * This will update the waitlist entry status -> "accepted", create attendees/{uid}, update counters.
     */
    public Task<Void> acceptInvite(@NonNull Event eventParam, @NonNull Profile currentUser) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (eventParam == null || currentUser == null) {
            tcs.setException(new IllegalArgumentException("Event and user must be provided"));
            return tcs.getTask();
        }
        String eventId = eventParam.getEventId();
        if (eventId == null) {
            tcs.setException(new IllegalArgumentException("Event must have an eventId"));
            return tcs.getTask();
        }

        final DocumentReference waitRef = db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(currentUser.getUid());

        waitRef.get()
                .addOnSuccessListener((DocumentSnapshot snap) -> {
                    if (!snap.exists()) {
                        tcs.setException(new IllegalStateException("User is not on the waitlist"));
                        return;
                    }

                    String status = snap.getString("status");
                    if (!"invited".equals(status)) {
                        tcs.setException(new IllegalStateException("Cannot accept invite unless status == invited (current: " + status + ")"));
                        return;
                    }

                    doAcceptInTransaction(eventId, currentUser, waitRef)
                            .addOnSuccessListener(aVoid -> tcs.setResult(null))
                            .addOnFailureListener(e -> tcs.setException(e));
                }).addOnFailureListener(e -> tcs.setException(e));

        return tcs.getTask();
    }

    /**
     * Internal helper: perform the "accept" flow inside a transaction:
     *  - set attendees/{uid} = { userId, acceptedAt }
     *  - update waitlist/{uid}.status = "accepted", acceptedAt = serverTimestamp()
     *  - increment event.attendeeCount
     *  - decrement event.waitingCount
     */
    private Task<Void> doAcceptInTransaction(@NonNull String eventId, @NonNull Profile currentUser, @NonNull DocumentReference waitRef) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        final DocumentReference attendeeRef = db.collection("events")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid());

        final DocumentReference eventRef = db.collection("events").document(eventId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // update/create attendee
            Map<String, Object> attendee = new HashMap<>();
            attendee.put("userId", currentUser.getUid());
            attendee.put("acceptedAt", FieldValue.serverTimestamp());
            transaction.set(attendeeRef, attendee);

            // update waitlist entry status -> accepted
            Map<String, Object> waitUpdates = new HashMap<>();
            waitUpdates.put("status", "accepted");
            waitUpdates.put("acceptedAt", FieldValue.serverTimestamp());
            transaction.update(waitRef, waitUpdates);

            // update counts
            transaction.update(eventRef, "attendeeCount", FieldValue.increment(1));
            transaction.update(eventRef, "waitingCount", FieldValue.increment(-1));

            return null;
        }).addOnSuccessListener(aVoid -> {
            tcs.setResult(null);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "doAcceptInTransaction failed", e);
            tcs.setException(e);
        });

        return tcs.getTask();
    }
}