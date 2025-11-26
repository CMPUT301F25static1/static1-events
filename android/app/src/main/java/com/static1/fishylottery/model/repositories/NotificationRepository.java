package com.static1.fishylottery.model.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.static1.fishylottery.model.entities.AppNotification;

/**
 * The notification wrapper repository responsible for uploading, fetching, and removing notification
 * objects from the Firebase. This maps the Firebase objects to Java objects so they can be used
 * in other contexts and data structures.
 */
public class NotificationRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Shortcut to user notifications collection */
    private CollectionReference col(String uid) {
        return db.collection("profiles")
                .document(uid)
                .collection("notifications");
    }

    /**
     * Listens for real-time input from a user's inbox given their uid.
     *
     * @param uid The UID/profileId of the user.
     * @param listener A listener callback to trigger when there is an update.
     * @return The snapshot listener.
     */
    public ListenerRegistration listenToInbox(
            @NonNull String uid,
            @NonNull EventListener<QuerySnapshot> listener
    ) {
        return col(uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    /**
     * Adds a new notification to the repository
     * @param uid The profile ID of the user to send the notification to.
     * @param notif The notification object.
     * @return A document reference to the newly created notification.
     */
    public Task<DocumentReference> addNotification(@NonNull String uid,
                                                   @NonNull AppNotification notif) {
        return col(uid).add(notif);
    }

    /**
     * Marks the notification as read in the Firebase.
     * @param uid The uid of the profile
     * @param notifId The notification ID for the document.
     * @return A task indicating success or failure of the action.
     */
    public Task<Void> markRead(@NonNull String uid,
                               @NonNull String notifId) {

        return col(uid)
                .document(notifId)
                .update("read", true);
    }

    /**
     * Records the response of an invitation type notification.
     *
     * @param uid The UID of the profile who is receiving the notification.
     * @param notifId The notification ID of the Firestore document.
     * @param accept A boolean indicating if they accept or decline in tbe invite.
     * @return A task indicating success or failure.
     */
    public Task<Void> respondToInvitation(@NonNull String uid,
                                          @NonNull String notifId,
                                          boolean accept) {

        String status = accept ? "accepted" : "declined";

        return col(uid)
                .document(notifId)
                .update("status", status);
    }

    public Task<Void> deleteAllNotifications() {
        // TODO: Deletes all of the notifications
        return Tasks.forResult(null);
    }

    /**
     * Deletes all of the notifications for a user by the UID.
     *
     * @param uid The UID of the profile.
     * @return A task indicating success or failure.
     */
    public Task<Void> deleteNotificationsByUser(@NonNull String uid) {
        return db.collection("profiles")
                .document(uid)
                .collection("notifications")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot snap : task.getResult().getDocuments()) {
                        batch.delete(snap.getReference());
                    }

                    return batch.commit();
                });
    }
}
