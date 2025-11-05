package com.static1.fishylottery.controller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.static1.fishylottery.model.repositories.NotificationSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendNotificationViewModel extends ViewModel {
    private final NotificationSender sender = new NotificationSender();
    private final MutableLiveData<Boolean> sending = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> success = new MutableLiveData<>(false);

    public LiveData<Boolean> getSending() { return sending; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getSuccess() { return success; }

    /** Parse comma separated UIDs (for CHOSEN flow). */
    public List<String> parseUids(String input) {
        if (input == null || input.trim().isEmpty()) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (String s : Arrays.asList(input.split(","))) {
            String t = s.trim();
            if (!t.isEmpty()) list.add(t);
        }
        return list;
    }

    public void send(String eventId,
                     NotificationSender.Audience audience,
                     String title,
                     String message,
                     String organizerUid,
                     String chosenUidsCsv) {

        error.setValue(null);
        success.setValue(false);

        if (title == null || title.trim().isEmpty()) { error.setValue("Title required"); return; }
        if (message == null || message.trim().isEmpty()) { error.setValue("Message required"); return; }

        List<String> chosen = audience == NotificationSender.Audience.CHOSEN ? parseUids(chosenUidsCsv) : new ArrayList<>();
        if (audience == NotificationSender.Audience.CHOSEN && chosen.isEmpty()) {
            error.setValue("Provide at least one UID for chosen entrants");
            return;
        }

        sending.setValue(true);
        Task<Void> t = sender.send(eventId, audience, title.trim(), message.trim(), organizerUid, chosen);
        t.addOnSuccessListener(v -> { sending.setValue(false); success.setValue(true); })
                .addOnFailureListener(e -> { sending.setValue(false); error.setValue(e.getMessage()); });
    }
}
