package com.static1.fishylottery.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.static1.fishylottery.model.entities.AppNotification;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.NotificationRepository;
import com.static1.fishylottery.model.repositories.WaitlistRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendNotificationsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> sending = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> success = new MutableLiveData<>(false);
    private WaitlistRepository waitlistRepo = new WaitlistRepository();
    private NotificationRepository notificationRepository = new NotificationRepository();

    public LiveData<Boolean> getSending() { return sending; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getSuccess() { return success; }

    public Task<Void> sendCustomNotification(Event event, Audience audience, String title, String message) {
        return waitlistRepo.getWaitlist(event).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            List<WaitlistEntry> waitlist = task.getResult();

            AppNotification notification = new AppNotification();

            String type;

            if (audience == Audience.SELECTED) {
                type = "invitation";
            } else if (audience == Audience.CANCELLED) {
                type = "declined";
            } else {
                type = "pending";
            }

            notification.setEventId(event.getEventId());
            notification.setRead(false);
            notification.setMessage(message);
            notification.setStatus("pending");
            notification.setType(type);
            notification.setTitle(title);
            notification.setCreatedAt(new Date());
            notification.setSenderId(senderId);



            for (WaitlistEntry entry : waitlist) {
                // TODO: Filter by audience
                String uid = entry.getProfile().getUid();

                // Send the notification to the user
                notificationRepository.addNotification(uid, notification)
                        .continueWithTask(task2 -> {
                            if (!task2.isSuccessful()) {
                                throw task2.getException();
                            }

                            return Tasks.forResult(null);
                        });
            }

            return Tasks.forResult(null);
        });
    }

    public enum Audience { SELECTED, WAITLIST, CANCELLED }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
}
