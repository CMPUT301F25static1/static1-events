package com.static1.fishylottery.controller;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;

/**
 * ViewModel for creating the events and sharing data between the organizer's event creation views
 */
public class CreateEventControllerViewModel extends ViewModel {
    private final MutableLiveData<Event> event = new MutableLiveData<>(new Event());
    private final EventRepository eventsRepository = new EventRepository();

    public LiveData<Event> getEvent() {
        return event;
    }

    public void updateEventDetails(String title, String description) {
        Event e = event.getValue();

        if (e == null) return;

        e.setTitle(title);
        e.setDescription(description);
        event.setValue(e);
    }

    public void submit() {
        // TODO: Error handling
        eventsRepository.addEvent(event.getValue());
    }

}
