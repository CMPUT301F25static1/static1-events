package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.static1.fishylottery.model.entities.Profile;

import java.util.ArrayList;
import java.util.List;

public class ProfileRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference profilesRef = db.collection("profiles");

    public Task<Void> addProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).set(profile);
    }

    public Task<Void> updateProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).set(profile);
    }

    public Task<Void> deleteProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).delete();
    }

    public Task<List<Profile>> getAllProfiles() {
        return profilesRef.get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    QuerySnapshot snapshot = task.getResult();
                    List<Profile> profiles = new ArrayList<>();

                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Profile profile = doc.toObject(Profile.class);
                            if (profile != null) {
                                profiles.add(profile);
                            }
                        }
                    }

                    return profiles;
                });
    }

    public Task<Profile> getProfileById(String uid) {
        return profilesRef
                .document(uid)
                .get()
                .continueWith(task -> {
                    DocumentSnapshot doc = task.getResult();
                    if (doc != null && doc.exists()) {
                        return doc.toObject(Profile.class);
                    } else {
                        return null;
                    }
        });
    }
}
