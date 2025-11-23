package com.static1.fishylottery.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.repositories.NotificationRepository;
import com.static1.fishylottery.model.repositories.ProfileRepository;
import com.static1.fishylottery.model.repositories.WaitlistRepository;
import com.static1.fishylottery.services.AuthManager;

public class ProfileViewModel extends ViewModel {

    private final ProfileRepository profileRepository;
    private final WaitlistRepository waitlistRepository;
    private final NotificationRepository notificationRepository;
    private final MutableLiveData<Profile> profileLiveData = new MutableLiveData<>();
    private static final String TAG = "Profile";

    public ProfileViewModel() {
        this.profileRepository = new ProfileRepository();
        this.waitlistRepository = new WaitlistRepository();
        this.notificationRepository = new NotificationRepository();
    }

    /** For testing or dependency injection */
    public ProfileViewModel(ProfileRepository pRepo, WaitlistRepository wRepo, NotificationRepository nRepo) {
        this.profileRepository = pRepo;
        this.waitlistRepository = wRepo;
        this.notificationRepository = nRepo;
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

        profileRepository.getProfileById(uid)
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

        return profileRepository.updateProfile(updated);
    }

    /** Deletes profile document via repository */
    public Task<Void> deleteProfile() {
        Profile profile = profileLiveData.getValue();

        if (profile == null) {
            throw new IllegalArgumentException("Profile cannot be null");
        }

        String uid = profile.getUid();

        // 1. Delete all of the notifications for that user
        return notificationRepository.deleteNotificationsByUser(uid)
                .continueWithTask(task1 -> {
                    if (!task1.isSuccessful()) {
                        throw task1.getException();
                    }

                    // 2. Delete the profile from Firebase
                    return profileRepository.deleteProfile(profile)
                            .continueWithTask(task2 -> {
                                if (!task2.isSuccessful()) {
                                    throw task2.getException();
                                }

                                // Profile deleted, set the data to null
                                profileLiveData.setValue(null);

                                // 3. Remove the user's waitlists
                                return waitlistRepository.deleteFromWaitlistByUser(uid);
                            });
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
