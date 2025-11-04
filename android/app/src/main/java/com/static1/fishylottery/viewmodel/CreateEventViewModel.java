package com.static1.fishylottery.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;

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

        if (imageUri == null) {
            // No image, just upload event directly
            eventsRepository.addEvent(e);
            return;
        }

        uploadImage(imageUri.getValue(), imageUrl -> {
            if (imageUrl != null) {
                // Update the created at and updated at timestamps
                Date now = new Date();
                e.setCreatedAt(now);
                e.setUpdatedAt(now);
                e.setImageUrl(imageUrl);
            }
            eventsRepository.addEvent(e);
        });
    }

    private void uploadImage(Uri imageUri, OnCompleteListener<String> callback) {
        StorageReference ref = storage.getReference()
                .child("images/" + UUID.randomUUID() + ".jpg");

        ref.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> callback.onComplete(uri.toString()))
                .addOnFailureListener(e -> callback.onComplete(null));
    }

    public interface OnCompleteListener<T> {
        void onComplete(T result);
        default void onError(Exception e) {}
    }

}
