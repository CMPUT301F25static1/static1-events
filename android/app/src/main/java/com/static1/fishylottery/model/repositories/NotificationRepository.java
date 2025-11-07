package com.static1.fishylottery.model.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.static1.fishylottery.model.entities.AppNotification;

public class NotificationRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Shortcut to user notifications collection */
    private CollectionReference col(String uid) {
        return db.collection("profiles")
                .document(uid)
                .collection("notifications");
    }

    // ----------------------------------------------------------------------
    // ✅ LISTEN REAL-TIME FOR INBOX (used by NotificationsViewModel)
    // ----------------------------------------------------------------------
    public ListenerRegistration listenToInbox(
            @NonNull String uid,
            @NonNull EventListener<QuerySnapshot> listener
    ) {
        return col(uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    // ----------------------------------------------------------------------
    // ✅ ADD NOTIFICATION
    // ----------------------------------------------------------------------
    public Task<DocumentReference> addNotification(@NonNull String uid,
                                                   @NonNull AppNotification notif) {
        return col(uid).add(notif);
    }

    // ----------------------------------------------------------------------
    // ✅ MARK AS READ
    // ----------------------------------------------------------------------
    public Task<Void> markRead(@NonNull String uid,
                               @NonNull String notifId) {

        return col(uid)
                .document(notifId)
                .update("read", true);
    }

    // ----------------------------------------------------------------------
    // ✅ RESPOND TO INVITATION
    // ----------------------------------------------------------------------
    public Task<Void> respondToInvitation(@NonNull String uid,
                                          @NonNull String notifId,
                                          boolean accept) {

        String status = accept ? "accepted" : "declined";

        return col(uid)
                .document(notifId)
                .update("status", status);
    }

    // ----------------------------------------------------------------------
    // ✅ REQUIRED BY NotificationSender
    // ✅ RESTORED EXACTLY HOW YOUR PROJECT EXPECTS
    // ----------------------------------------------------------------------
    public Task<Void> sendToProfile(@NonNull String uid,
                                    @NonNull AppNotification notif) {

        return col(uid)
                .document()  // auto-generate document ID
                .set(notif);
    }
}
