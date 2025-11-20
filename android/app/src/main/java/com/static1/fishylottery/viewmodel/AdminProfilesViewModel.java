package com.static1.fishylottery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.repositories.ProfileRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminProfilesViewModel extends ViewModel {
    private final ProfileRepository repository = new ProfileRepository();
    private final MutableLiveData<List<Profile>> profiles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<List<Profile>> getProfiles() {
        return profiles;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void loadProfiles() {
        loading.setValue(true);
        repository.getAllProfiles()
                .addOnSuccessListener(profileList -> {
                    profiles.setValue(profileList);
                    loading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    message.setValue("Failed to load profiles: " + e.getMessage());
                    loading.setValue(false);
                });
    }

    public void deleteProfile(Profile profile) {
        loading.setValue(true);
        repository.deleteProfile(profile)
                .addOnSuccessListener(aVoid -> {
                    message.setValue("Profile deleted successfully");
                    // Reload the list
                    loadProfiles();
                })
                .addOnFailureListener(e -> {
                    message.setValue("Failed to delete profile: " + e.getMessage());
                    loading.setValue(false);
                });
    }
}