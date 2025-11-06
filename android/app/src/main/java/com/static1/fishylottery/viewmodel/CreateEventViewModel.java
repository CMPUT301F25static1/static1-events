package com.static1.fishylottery.viewmodel;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.static1.fishylottery.MainApplication;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.services.StorageManager;

import java.util.Date;
import java.util.UUID;

/**
 * ViewModel for creating the events and sharing data between the organizer's event creation views
 */
public class CreateEventViewModel extends ViewModel {
    private final MutableLiveData<Event> event = new MutableLiveData<>(new Event());
    private final MutableLiveData<Uri> imageUri = new MutableLiveData<>();
    private final MutableLiveData<String> validationError = new MutableLiveData<>();

    private final EventRepository eventsRepository = new EventRepository();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public LiveData<Event> getEvent() {
        return event;
    }
    public LiveData<Uri> getImageUri() { return imageUri; }

    public LiveData<String> getValidationError() {
        return validationError;
    }

    public void updateEvent(Event e) { event.setValue(e); }

    public void setImageUri(Uri uri) { imageUri.setValue(uri); }

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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e("CreateEvent", "Firebase user not created");
            return;
        }

        // Set the organizer ID
        e.setOrganizerId(user.getUid());

        Date now = new Date();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setStatus("Open");

        if (imageUri == null) {
            // No image, just upload event directly
            eventsRepository.addEvent(e);
            return true;
        }

        uploadImage(imageUri.getValue(), imageUrl -> {
            if (imageUrl != null) {
                e.setImageUrl(imageUrl);
            }

            eventsRepository.addEvent(e);
        });

        return true;
    }

    private void uploadImage(Uri imageUri, OnCompleteListener<String> callback) {
        StorageManager.uploadImage(imageUri, "images/events")
                .addOnSuccessListener(callback::onComplete)
                .addOnFailureListener(e -> callback.onComplete(null));
    }

    public interface OnCompleteListener<T> {
        void onComplete(T result);
        default void onError(Exception e) {}
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
