package com.static1.fishylottery.viewmodel;

import android.content.Context;

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
import com.static1.fishylottery.services.NotificationSettings;

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
    private Context context;

    public LiveData<List<AppNotification>> getInbox() {
        return inbox;
    }

    /**
     * Called by NotificationsFragment.onStart()
     */
    public void start(@NonNull String uid, Context context) {
        this.context = context;
        stop();

        // Check if notifications are enabled before starting listener
        if (!NotificationSettings.areNotificationsEnabled(context)) {
            // If notifications are disabled, post empty list and return
            inbox.postValue(new ArrayList<>());
            return;
        }

        // IMPORTANT: use FirebaseFirestoreException (not Exception)
        reg = repo.listenToInbox(uid, (QuerySnapshot snap, FirebaseFirestoreException err) -> {
            if (snap == null) return;

            // Double-check preference hasn't changed
            if (!NotificationSettings.areNotificationsEnabled(context)) {
                inbox.postValue(new ArrayList<>());
                return;
            }

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
        // Check if notifications are enabled before responding
        if (context != null && NotificationSettings.areNotificationsEnabled(context)) {
            repo.respondToInvitation(uid, notifId, accept);
        }
    }
}