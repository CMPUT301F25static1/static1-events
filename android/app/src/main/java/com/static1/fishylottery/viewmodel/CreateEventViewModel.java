package com.static1.fishylottery.viewmodel;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.services.StorageManager;

import java.util.Date;

/**
 * ViewModel for creating the events and sharing data between the organizer's event creation views
 */
public class CreateEventViewModel extends ViewModel {
    private final MutableLiveData<Event> event = new MutableLiveData<>(new Event());
    private final MutableLiveData<Boolean> isEdit = new MutableLiveData<>(false);
    private final MutableLiveData<Uri> imageUri = new MutableLiveData<>();
    private final MutableLiveData<String> validationError = new MutableLiveData<>();

    private final EventRepository eventsRepository = new EventRepository();

    /**
     * Live data showing the event object.
     *
     * @return Live data event object.
     */
    public LiveData<Event> getEvent() {
        return event;
    }

    /**
     * Returns a boolean if the view model is in edit mode or create mode for the event.
     *
     * @return A boolean if in edit mode.
     */
    public LiveData<Boolean> isEdit() { return isEdit; }
    public void setIsEdit(boolean isEdit) {
        this.isEdit.setValue(isEdit);
    }

    /**
     * Live data showing the imageURI so that the image can be previewed when updated.
     *
     * @return The image URI
     */
    public LiveData<Uri> getImageUri() { return imageUri; }

    /**
     * Live data showing a message for a validation error (if any)
     *
     * @return Live data string.
     */
    public LiveData<String> getValidationError() {
        return validationError;
    }

    /**
     * Updates the event if it has changed which is used with the form in create events details.
     *
     * @param e The event to update.
     */
    public void updateEvent(Event e) { event.setValue(e); }

    /**
     * Set the imageUri live data to the current imageUri.
     *
     * @param uri The image URI
     */
    public void setImageUri(Uri uri) { imageUri.setValue(uri); }

    /**
     * Submits the data that is updated in the Event object to Firebase while also performing
     * validation to ensure inputs are correct.
     *
     * @return boolean indicated success or failure.
     */
    public boolean submit() {
        Event e = event.getValue();

        boolean _isEdit = isEdit.getValue() != null ? isEdit.getValue() : false;

        if (e == null) {
            validationError.setValue("No event to save.");
            return false;
        }

        if (_isEdit) {
            if (e.getEventId() == null) {
                validationError.setValue("Unable to edit event with no ID");
                return false;
            }
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

        if (_isEdit) {
            return editEvent(e);
        } else {
            return createEvent(e);
        }
    }

    private boolean createEvent(@NonNull Event e) {
        // Get the uid for the organizer
        String uid = AuthManager.getInstance().getUserId();

        if (uid == null) {
            validationError.setValue("Please sign in to create an event");
            Log.e("CreateEvent", "User not signed in");
            return false;
        }

        e.setOrganizerId(uid);

        Date now = new Date();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setRegistrationOpens(now);
        e.setStatus("Open");



        // No image, upload the event as is
        if (imageUri == null) {
            eventsRepository.addEvent(e);
        } else {
            uploadImage(imageUri.getValue(), imageUrl -> {
                if (imageUrl != null) {
                    e.setImageUrl(imageUrl);
                }

                eventsRepository.addEvent(e);
            });
        }

        validationError.setValue(null);
        return true;
    }

    private boolean editEvent(@NonNull Event e)  {
        Date now = new Date();

        e.setUpdatedAt(now);

        // TODO: Handle updating the image poster

        eventsRepository.updateEvent(e);

        return true;
    }


    /**
     * Completion listener when the processes is finished.
     *
     * @param <T> The type of the returned value upon completion.
     */
    public interface OnCompleteListener<T> {
        void onComplete(T result);
        default void onError(Exception e) {}
    }

    private void uploadImage(Uri imageUri, OnCompleteListener<String> callback) {
        StorageManager.uploadImage(imageUri, "images/events")
                .addOnSuccessListener(callback::onComplete)
                .addOnFailureListener(e -> callback.onComplete(null));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
