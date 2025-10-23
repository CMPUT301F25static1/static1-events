package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.static1.fishylottery.model.entities.Profile;

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

    public Task<QuerySnapshot> getAllProfiles() {
        return profilesRef.get();
    }

    public Task<DocumentSnapshot> getProfileById(String uid) {
        return profilesRef.document(uid).get();
    }
}
