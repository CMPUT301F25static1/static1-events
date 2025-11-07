package com.static1.fishylottery.viewmodel;

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

/**
 * Notifications view model which manages that data that is passed to and from the view.
 * Contains methods to listen to the notification inbox and show the content in a live list.
 */
public class NotificationsViewModel extends ViewModel {

    private final NotificationRepository repo = new NotificationRepository();
    private final MutableLiveData<List<AppNotification>> inbox = new MutableLiveData<>();
    private ListenerRegistration reg;

    public LiveData<List<AppNotification>> getInbox() {
        return inbox;
    }

    /**
     * Called by NotificationsFragment.onStart()
     */
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

    /**
     * Called by NotificationsFragment.onStop()
     */
    public void stop() {
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }

    /**
     * Responds to a notification by passing the notification ID and whether or not the user
     * accepted or declines the event.
     *
     * @param uid The uid of the profile that received the notification.
     * @param notifId The unique notification ID in Firebase.
     * @param accept True is accepted, false is declined.
     */
    public void respondToInvitation(String uid, String notifId, boolean accept) {
        repo.respondToInvitation(uid, notifId, accept);
    }
}
