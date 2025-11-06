package com.static1.fishylottery.controller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.WaitlistRepository;
import com.google.android.gms.tasks.Task;

import java.util.Date;

public class EventDetailsViewModel extends ViewModel {
    private final MutableLiveData<Event> event = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final WaitlistRepository waitlistRepo = new WaitlistRepository();

    public void setEvent(Event e) { event.setValue(e); }
    public LiveData<Event> getEvent() { return event; }
    public LiveData<String> getError() { return error; }

    /** true if registration deadline has not passed */
    public boolean canJoinNow() {
        Event e = event.getValue();
        Date close = (e == null) ? null : e.getRegistrationCloses();
        return e != null && close != null && new Date().before(close);
    }

    /** Call with a stable profileId */
    public Task<Void> joinWaitlist(String profileId) {
        Event e = event.getValue();
        if (e == null || e.getEventId() == null) {
            error.setValue("Event not loaded.");
            return null;
        }
        if (!canJoinNow()) {
            error.setValue("Registration deadline has passed.");
            return null;
        }
        return waitlistRepo.joinWaitlist(e.getEventId(), profileId);
    }
}

