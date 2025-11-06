package com.static1.fishylottery.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
    private final EventRepository eventsRepository = new EventRepository();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public LiveData<Event> getEvent() {
        return event;
    }
    public LiveData<Uri> getImageUri() { return imageUri; }

    public void updateEvent(Event e) { event.setValue(e); }

    public void setImageUri(Uri uri) { imageUri.setValue(uri); }

    public void submit() {
        Event e = event.getValue();

        if (e == null) return;

        // Set the organizer ID
        // TODO: Set the profile now
        e.setOrganizerId(null);

        Date now = new Date();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setStatus("Open");

        if (imageUri == null) {
            // No image, just upload event directly
            eventsRepository.addEvent(e);
            return;
        }

        uploadImage(imageUri.getValue(), imageUrl -> {
            if (imageUrl != null) {
                e.setImageUrl(imageUrl);
            }

            eventsRepository.addEvent(e);
        });
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

}
