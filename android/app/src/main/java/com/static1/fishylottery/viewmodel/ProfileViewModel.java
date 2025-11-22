package com.static1.fishylottery.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.repositories.ProfileRepository;
import com.static1.fishylottery.model.repositories.WaitlistRepository;
import com.static1.fishylottery.services.AuthManager;

public class ProfileViewModel extends ViewModel {

    private final ProfileRepository repository;
    private final MutableLiveData<Profile> profileLiveData = new MutableLiveData<>();
    private static final String TAG = "Profile";

    public ProfileViewModel() {
        this.repository = new ProfileRepository();
    }

    /** For testing or dependency injection */
    public ProfileViewModel(ProfileRepository repository) {
        this.repository = repository;
    }

    public LiveData<Profile> getProfile() {
        if (profileLiveData.getValue() == null) {
            loadProfile();
        }
        return profileLiveData;
    }

    /** Loads current user's profile from repository */
    public void loadProfile() {
        String uid = AuthManager.getInstance().getUserId();
        if (uid == null) return;

        repository.getProfileById(uid)
                .addOnSuccessListener(profileLiveData::setValue)
                .addOnFailureListener(e -> profileLiveData.setValue(null));
    }

    /** Updates a user profile via repository */
    public Task<Void> updateProfile(String firstName, String lastName, String email, String phone) {
        String uid = AuthManager.getInstance().getUserId();
        if (uid == null) {
            return Tasks.forException(new IllegalStateException("User not logged in"));
        }

        Profile updated = new Profile();
        updated.setUid(uid);
        updated.setFirstName(firstName);
        updated.setLastName(lastName);
        updated.setEmail(email);
        updated.setPhone(phone);

        profileLiveData.setValue(updated);

        return repository.updateProfile(updated);
    }

    /** Deletes profile document via repository */
    public Task<Void> deleteProfile() {
        Profile profile = profileLiveData.getValue();

        if (profile == null) {
            throw new IllegalArgumentException("Profile cannot be null");
        }

        String uid = profile.getUid();

        // 1. Remove the profile from Firebase
        return repository.deleteProfile(profile)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Profile deleted, set the data to null
                    profileLiveData.setValue(null);

                    // 2. Remove the user's waitlists
                    WaitlistRepository waitlistRepository = new WaitlistRepository();

                    return waitlistRepository.deleteFromWaitlistByUser(uid);
                })
                .addOnSuccessListener(aVoid -> {
                    profileLiveData.setValue(null);
                    Log.d(TAG, "Successfully delete profile for uid: " + uid);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Unable to delete profile", e);
                });

    }
}
