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

public class ProfileController {

    private final AuthManager authManager;
    private final ProfileRepository profileRepository;
    private final FragmentManager fragmentManager;
    private final int containerId;
    public ProfileController(AuthManager authManager, ProfileRepository profileRepository, FragmentManager fragmentManager, int containerId) {
        this.authManager = authManager;
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

    public void loadInitialProfile() {
        String uid = authManager.getUserId();

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
    public void loadProfile(ProfileCallback callback) {
        String uid = authManager.getUserId();

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

    public void uploadProfile(Profile profile, ProfileUploadCallback callback) {
        profile.setUid(authManager.getUserId());

        profileRepository.addProfile(profile)
                        .addOnSuccessListener(s -> {
                            callback.onComplete();
                        })
                .addOnFailureListener(e -> {
                    callback.onError(new Exception("Error occured"));
                });
    }

    public void deleteProfile(Profile profile) {
        profileRepository.deleteProfile(profile);
    }

    public boolean hasProfile() {
        String uid = authManager.getUserId();

        if (uid == null) {
            return false;
        }

        Task<Profile> task = profileRepository.getProfileById(uid);

        if (task.isSuccessful()) {
            return task.getResult() != null;
        } else {
            return false;
        }
    }

    public void showProfileView(Profile profile) {
        Fragment fragment = new ProfileViewFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }

    public void showCreateProfile() {
        fragmentManager.beginTransaction()
                .replace(containerId, new CreateProfileFragment())
                .commit();
    }
}
