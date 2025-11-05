package com.static1.fishylottery.controller;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;

import java.util.Date;

/**
 * ViewModel for creating events and sharing data between the organizer's event screens.
 */
public class CreateEventControllerViewModel extends ViewModel {

    private final MutableLiveData<Event> event = new MutableLiveData<>(new Event());
    private final MutableLiveData<String> validationError = new MutableLiveData<>();
    private final EventRepository eventsRepository = new EventRepository();

    public LiveData<Event> getEvent() {
        return event;
    }

    public void updateEvent(Event e) {
        event.setValue(e);
    }

    public LiveData<String> getValidationError() {
        return validationError;
    }

    /**
     * Validate the event’s registration window and basic required fields.
     * Returns true if valid; otherwise sets validationError and returns false.
     */
    public boolean submit() {
        Event e = event.getValue();
        if (e == null) {
            validationError.setValue("No event to save.");
            return false;
        }

        // required text fields
        if (isBlank(e.getTitle())) {
            validationError.setValue("Please enter a title.");
            return false;
        }
        if (isBlank(e.getLocation())) {
            validationError.setValue("Please enter a location.");
            return false;
        }

        // date/time logic
        Date start = e.getEventStartDate();
        Date end = e.getEventEndDate();
        Date regClose = e.getRegistrationCloses();

        // all three must be set
        if (start == null || end == null || regClose == null) {
            validationError.setValue("Please select start, end, and registration deadline.");
            return false;
        }

        // start must be strictly before end
        if (!start.before(end)) {
            validationError.setValue("Start time must be before end time.");
            return false;
        }

        // registration deadline must be before the event start
        if (!regClose.before(start)) {
            validationError.setValue("Registration deadline must be before the event start.");
            return false;
        }


        // clear any old error
        validationError.setValue(null);

        eventsRepository.addEvent(e);
        return true;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
