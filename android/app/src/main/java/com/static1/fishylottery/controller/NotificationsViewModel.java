package com.static1.fishylottery.controller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.static1.fishylottery.model.entities.AppNotification;
import com.static1.fishylottery.model.repositories.NotificationRepository;

import java.util.ArrayList;
import java.util.List;

public class NotificationsViewModel extends ViewModel {
    private final NotificationRepository repo = new NotificationRepository();
    private final MutableLiveData<List<AppNotification>> inbox = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private ListenerRegistration reg;

    public LiveData<List<AppNotification>> getInbox() { return inbox; }
    public LiveData<String> getError() { return error; }

    public void start(String uid) {
        stop();
        reg = repo.listenToUserInbox(uid, (snap, e) -> {
            if (e != null || snap == null) { error.postValue(e != null ? e.getMessage() : "Unknown error"); return; }
            List<AppNotification> list = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                AppNotification n = d.toObject(AppNotification.class);
                if (n != null) { n.setId(d.getId()); list.add(n); }
            }
            inbox.postValue(list);
        });
    }

    public void stop() {
        if (reg != null) { reg.remove(); reg = null; }
    }

    @Override protected void onCleared() {
        stop();
        super.onCleared();
    }
}
