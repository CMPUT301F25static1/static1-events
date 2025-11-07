package com.static1.fishylottery.model.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.model.entities.AppNotification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sends notifications to profile inboxes.
 * This version removes all Audience enum usage.
 */
public class NotificationSender {

    private final NotificationRepository repo = new NotificationRepository();

    /**
     * Send a notification.
     *
     * @param eventId           Related event ID
     * @param audienceType      "CHOSEN" or any other string
     * @param title             Notification title
     * @param message           Notification message
     * @param organizerUid      Sender UID
     * @param chosenUidsIfAny   Only used when audienceType == "CHOSEN"
     */
    public Task<Void> send(@NonNull String eventId,
                           @NonNull String audienceType,
                           @NonNull String title,
                           @NonNull String message,
                           @NonNull String organizerUid,
                           @NonNull List<String> chosenUidsIfAny) {

        // ✅ Build notification with setters
        AppNotification n = new AppNotification();
        n.setEventId(eventId);
        n.setSenderId(organizerUid);
        n.setTitle(title);
        n.setMessage(message);

        // You can change this to "invitation" if needed
        if ("CHOSEN".equalsIgnoreCase(audienceType)) {
            n.setType("ORGANIZER_CHOSEN");
        } else {
            n.setType("ORGANIZER_BROADCAST");
        }

        n.setStatus("pending");
        n.setCreatedAt(new Date());
        n.setRead(false);

        // ✅ Determine recipients
        List<String> recipients = new ArrayList<>();

        if ("CHOSEN".equalsIgnoreCase(audienceType)) {
            if (chosenUidsIfAny != null) {
                recipients.addAll(chosenUidsIfAny);
            }
        } else {
            // If you later add SELECTED/WAITLIST logic, put here.
            // For now, avoid errors by doing nothing.
        }

        // ✅ Write to Firestore
        for (String uid : recipients) {
            if (uid != null && !uid.isEmpty()) {
                repo.sendToProfile(uid, n);
            }
        }

        return Tasks.forResult(null);
    }
}
