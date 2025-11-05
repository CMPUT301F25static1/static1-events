package com.static1.fishylottery.model.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;
import com.static1.fishylottery.model.entities.AppNotification;

import java.util.*;

public class NotificationSender {

    public enum Audience { SELECTED, WAITLIST, CANCELLED, CHOSEN } // CHOSEN = specific UIDs

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Query all entrant UIDs for an event by status. Adjust collection/field names if your schema differs. */
    private Task<QuerySnapshot> getEntrantsByStatus(@NonNull String eventId, @NonNull String status) {
        // Assumes: events/{eventId}/entrants/{uid} with field "status"
        return db.collection("events").document(eventId)
                .collection("entrants")
                .whereEqualTo("status", status)
                .get();
    }

    /** Send one notification to each uid (batched). Returns a Task that completes when all writes finish. */
    private Task<Void> sendToUids(@NonNull List<String> uids, @NonNull AppNotification notif) {
        if (uids.isEmpty()) return com.google.android.gms.tasks.Tasks.forResult(null);

        WriteBatch batch = db.batch();
        Timestamp ts = Timestamp.now();

        for (String uid : uids) {
            DocumentReference ref = db.collection("users").document(uid)
                    .collection("notifications").document();
            Map<String, Object> data = new HashMap<>();
            data.put("eventId", notif.getEventId());
            data.put("senderId", notif.getSenderId());
            data.put("title", notif.getTitle());
            data.put("message", notif.getMessage());
            data.put("type", notif.getType());
            data.put("createdAt", ts);
            data.put("read", false);
            batch.set(ref, data, SetOptions.merge());
        }
        return batch.commit();
    }

    /** Core API used by ViewModel & hooks. If audience==CHOSEN, pass non-empty chosenUids. */
    public Task<Void> send(@NonNull String eventId,
                           @NonNull Audience audience,
                           @NonNull String title,
                           @NonNull String message,
                           @NonNull String organizerUid,
                           @NonNull List<String> chosenUidsIfAny) {

        AppNotification n = new AppNotification(
                eventId,
                organizerUid,
                title,
                message,
                audience == Audience.CHOSEN ? "ORGANIZER_CHOSEN" : "ORGANIZER_BROADCAST",
                new Date(), false
        );

        switch (audience) {
            case SELECTED:
                return getEntrantsByStatus(eventId, "SELECTED")
                        .onSuccessTask(snap -> sendToUids(extractUids(snap), n));
            case WAITLIST:
                return getEntrantsByStatus(eventId, "WAITLIST")
                        .onSuccessTask(snap -> sendToUids(extractUids(snap), n));
            case CANCELLED:
                return getEntrantsByStatus(eventId, "CANCELLED")
                        .onSuccessTask(snap -> sendToUids(extractUids(snap), n));
            case CHOSEN:
                return sendToUids(new ArrayList<>(chosenUidsIfAny), n);
        }
        return com.google.android.gms.tasks.Tasks.forResult(null);
    }

    private List<String> extractUids(QuerySnapshot snap) {
        List<String> out = new ArrayList<>();
        if (snap != null) {
            for (DocumentSnapshot d : snap.getDocuments()) {
                // If docId is UID (recommended)
                out.add(d.getId());
                // If your schema stores uid in a field instead, do:
                // String uid = d.getString("uid"); if (uid != null) out.add(uid);
            }
        }
        return out;
    }

    /** Hooks for auto win/lose notifications */
    public Task<Void> sendChosenWin(@NonNull String eventId, @NonNull String winnerUid, @NonNull String organizerUid) {
        AppNotification n = new AppNotification(eventId, organizerUid,
                "You were selected!", "Congrats—you're in! Check event details.",
                "CHOSEN_WIN", new Date(), false);
        return sendToUids(Collections.singletonList(winnerUid), n);
    }

    public Task<Void> sendNotChosen(@NonNull String eventId, @NonNull String uid, @NonNull String organizerUid) {
        AppNotification n = new AppNotification(eventId, organizerUid,
                "Not selected", "You weren't selected this time. Try future events!",
                "NOT_CHOSEN", new Date(), false);
        return sendToUids(Collections.singletonList(uid), n);
    }
}
