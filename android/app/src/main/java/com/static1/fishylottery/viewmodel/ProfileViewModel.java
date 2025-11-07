package com.static1.fishylottery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.repositories.ProfileRepository;

public class ProfileViewModel extends ViewModel {

    private final ProfileRepository repository;
    private final MutableLiveData<Profile> profileLiveData = new MutableLiveData<>();

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
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        repository.getProfileById(uid)
                .addOnSuccessListener(profileLiveData::setValue)
                .addOnFailureListener(e -> profileLiveData.setValue(null));
    }

    /** Updates a user profile via repository */
    public Task<Void> updateProfile(String firstName, String lastName, String email, String phone) {
        String uid = FirebaseAuth.getInstance().getUid();
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
        if (profileLiveData.getValue() == null) {
            throw new IllegalArgumentException("Profile cannot be null");
        }
        return repository.deleteProfile(profileLiveData.getValue())
                .addOnSuccessListener(aVoid -> profileLiveData.setValue(null));
    }
}
