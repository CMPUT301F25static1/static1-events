package com.static1.fishylottery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.static1.fishylottery.model.entities.AppNotification;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.view.admin.NotificationLogAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminNotificationLogsViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<NotificationLogAdapter.NotificationLog>> notificationLogs = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<NotificationLogAdapter.NotificationLog>> getNotificationLogs() {
        return notificationLogs;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public void loadAllNotifications() {
        loading.setValue(true);
        List<NotificationLogAdapter.NotificationLog> allLogs = new ArrayList<>();

        // First, get all profiles
        db.collection("profiles").get().addOnSuccessListener(profilesSnapshot -> {
            if (profilesSnapshot.isEmpty()) {
                loading.setValue(false);
                notificationLogs.setValue(allLogs);
                return;
            }

            int totalProfiles = profilesSnapshot.size();
            final int[] processedProfiles = {0};

            // For each profile, get their notifications
            for (DocumentSnapshot profileDoc : profilesSnapshot.getDocuments()) {
                Profile profile = profileDoc.toObject(Profile.class);
                String uid = profileDoc.getId();

                db.collection("profiles")
                        .document(uid)
                        .collection("notifications")
                        .get()
                        .addOnSuccessListener(notificationsSnapshot -> {
                            for (DocumentSnapshot notifDoc : notificationsSnapshot.getDocuments()) {
                                AppNotification notif = notifDoc.toObject(AppNotification.class);
                                if (notif != null && profile != null) {
                                    NotificationLogAdapter.NotificationLog log =
                                            new NotificationLogAdapter.NotificationLog(
                                                    profile.getFullName(),
                                                    profile.getEmail(),
                                                    notif.getTitle(),
                                                    notif.getMessage(),
                                                    notif.getType(),
                                                    notif.getCreatedAt() != null ? notif.getCreatedAt().getTime() : 0
                                            );
                                    allLogs.add(log);
                                }
                            }

                            processedProfiles[0]++;
                            if (processedProfiles[0] == totalProfiles) {
                                // All profiles processed, sort by date descending
                                allLogs.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
                                notificationLogs.setValue(allLogs);
                                loading.setValue(false);
                            }
                        })
                        .addOnFailureListener(e -> {
                            processedProfiles[0]++;
                            if (processedProfiles[0] == totalProfiles) {
                                notificationLogs.setValue(allLogs);
                                loading.setValue(false);
                            }
                        });
            }
        }).addOnFailureListener(e -> {
            loading.setValue(false);
            notificationLogs.setValue(allLogs);
        });
    }
}