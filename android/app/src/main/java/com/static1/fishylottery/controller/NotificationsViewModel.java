package com.static1.fishylottery.controller;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.static1.fishylottery.model.entities.AppNotification;
import com.static1.fishylottery.model.repositories.NotificationRepository;

import java.util.ArrayList;
import java.util.List;

public class NotificationsViewModel extends ViewModel {

    private final NotificationRepository repo = new NotificationRepository();
    private final MutableLiveData<List<AppNotification>> inbox = new MutableLiveData<>();
    private ListenerRegistration reg;

    public LiveData<List<AppNotification>> getInbox() {
        return inbox;
    }

    /** Called by NotificationsFragment.onStart() */
    public void start(@NonNull String uid) {
        stop();

        // IMPORTANT: use FirebaseFirestoreException (not Exception)
        reg = repo.listenToInbox(uid, (QuerySnapshot snap, FirebaseFirestoreException err) -> {
            if (snap == null) return;

            List<AppNotification> out = new ArrayList<>();

            // IMPORTANT: explicitly type DocumentSnapshot
            for (DocumentSnapshot doc : snap.getDocuments()) {
                AppNotification n = doc.toObject(AppNotification.class);
                if (n != null) {
                    n.setId(doc.getId());
                    out.add(n);
                }
            }

            inbox.postValue(out);
        });
    }

    /** Called by NotificationsFragment.onStop() */
    public void stop() {
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }

    public void respondToInvitation(String uid, String notifId, boolean accept) {
        repo.respondToInvitation(uid, notifId, accept);
    }
}
