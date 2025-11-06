package com.static1.fishylottery.model.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import com.static1.fishylottery.model.entities.AppNotification;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NotificationRepository {
    private final FirebaseFirestore db;

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /** Live inbox for a user (ordered newest-first). Call remove() on the returned registration to stop. */
    public ListenerRegistration listenToUserInbox(@NonNull String uid,
                                                  @NonNull EventListener<QuerySnapshot> listener) {
        return db.collection("users").document(uid)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    /** Write a single notification to a user's inbox. */
    public Task<DocumentReference> sendToUser(@NonNull String uid, @NonNull AppNotification n) {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", n.getEventId());
        data.put("senderId", n.getSenderId());
        data.put("title", n.getTitle());
        data.put("message", n.getMessage());
        data.put("type", n.getType());
        data.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt() : new Date());
        data.put("read", n.isRead());
        return db.collection("users").document(uid)
                .collection("notifications")
                .add(data);
    }
}
