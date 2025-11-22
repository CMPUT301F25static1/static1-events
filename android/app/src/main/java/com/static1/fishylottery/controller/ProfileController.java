package com.static1.fishylottery.controller;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.Task;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.repositories.ProfileRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.view.profile.CreateProfileFragment;
import com.static1.fishylottery.view.profile.ProfileViewFragment;

/**
 * Manages the user's profile and what screen to show when the profile page is shown.
 */
public class ProfileController {
    private final ProfileRepository profileRepository;
    private final FragmentManager fragmentManager;
    private final int containerId;

    public ProfileController(ProfileRepository profileRepository,
                             FragmentManager fragmentManager,
                             int containerId) {
        this.profileRepository = profileRepository;
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
    }

    public interface ProfileCallback {
        void onProfileLoaded(Profile profile);
        void onError(Exception e);
    }

    public interface ProfileUploadCallback {
        void onComplete();
        void onError(Exception e);
    }

    /**
     * Loads the initial profile for a user. If no profile is found, the create profile screen
     * is displayed so the user can enter there.
     */
    public void loadInitialProfile() {
        String uid = AuthManager.getInstance().getUserId();

        if (uid == null) {
            Log.e("AuthManager", "No user id");
            showCreateProfile();
            return;
        }

        profileRepository.getProfileById(uid)
                .addOnSuccessListener(profile -> {
                    if (profile == null) {
                        Log.i("Profile", "No Profile yet");
                        showCreateProfile();
                    } else {
                        showProfileView(profile);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Profile", "Could not get profile: " + e.toString());
                    showCreateProfile();
                });

    }

    /**
     * Loads the profile by checking AuthManager and ProfileRepository.
     *
     * @param callback A callback which handles the result of getting the profile.
     */
    public void loadProfile(ProfileCallback callback) {
        String uid = AuthManager.getInstance().getUserId();

        if (uid == null) {
            callback.onError(new Exception("No user ID"));
            return;
        }

        profileRepository.getProfileById(uid)
                .addOnSuccessListener(profile -> {
                    if (profile == null) {
                        callback.onError(new Exception("No profile found"));
                    } else {
                        callback.onProfileLoaded(profile);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(new Exception("Could not fetch profile"));
                });
    }

    /**
     * Uploads a profile to the Firebase upon creation.
     *
     * @param profile The profile to upload.
     * @param callback A callback indicating if success or failure.
     */
    public void uploadProfile(Profile profile, ProfileUploadCallback callback) {
        String uid = AuthManager.getInstance().getUserId();

        if (uid == null) {
            callback.onError(new Exception("The profile UID is null"));
            return;
        }

        profile.setUid(uid);

        profileRepository.addProfile(profile)
                .addOnSuccessListener(s -> callback.onComplete())
                .addOnFailureListener(e -> callback.onError(new Exception("Error occurred")));
    }

    /**
     * Deletes a profile from the database.
     *
     * @param profile The profile object to delete.
     */
    public void deleteProfile(Profile profile) {
        profileRepository.deleteProfile(profile);
    }

    /**
     * Determines if a profile exists in the Firestore for a given user ID.
     * This user ID might already exist in Firebase Auth which is why it can be checked.
     *
     * @return True if the profile exists for a UID.
     */
    public boolean hasProfile() {
        String uid = AuthManager.getInstance().getUserId();

        if (uid == null) return false;

        Task<Profile> task = profileRepository.getProfileById(uid);
        return task.isSuccessful() && task.getResult() != null;
    }

    /**
     * Show the profile view fragment.
     * @param profile The profile view to show,
     */
    public void showProfileView(Profile profile) {
        Fragment fragment = new ProfileViewFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commitAllowingStateLoss();
    }

    /**
     * Show the create profile page so user can create profile upon start up.
     */
    public void showCreateProfile() {
        fragmentManager.beginTransaction()
                .replace(containerId, new CreateProfileFragment())
                .commitAllowingStateLoss();
    }
}
