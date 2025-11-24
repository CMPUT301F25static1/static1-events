package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.static1.fishylottery.model.entities.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * The profile wrapper for Firestore which interacts between Firebase documents and Java objects
 * for profiles. This is designed to allow easy, reusable, and shared access of documents
 * to and from the "profiles" collection in Firestore.
 */
public class ProfileRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference profilesRef = db.collection("profiles");

    /**
     * Adds a profile to the profiles collection. The UID is already determined from Firebase Auth.
     *
     * @param profile The profile to be added.
     * @return A task indicating success or failure.
     */
    public Task<Void> addProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).set(profile);
    }

    /**
     * Update the profile with a new object.
     *
     * @param profile The new profile object that should be updated.
     * @return A task indicating success or failure.
     */
    public Task<Void> updateProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).set(profile);
    }

    /**
     * Deletes the profile from the profiles collection.
     *
     * @param profile The profile object to delete (only requires the ID to exist)
     * @return A task indicating success or failure.
     */
    public Task<Void> deleteProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).delete();
    }

    /**
     * Gets a list of all of the profiles currently in the database.
     * @return A task with a list of profile objects.
     */
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

    /**
     * Get a single profile object by the UID.
     *
     * @param uid The UID of the profile. Most often comes from Firebase Auth.
     * @return A task with a profile object. Will be null if doesn't exist.
     */
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

    /**
     * Fetch many profiles by their UIDs. Uses chunked whereIn queries (max 10 IDs per query).
     */
    public Task<List<Profile>> fetchProfilesByIds(List<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        List<Task<QuerySnapshot>> chunkTasks = new ArrayList<>();

        // Firestore whereIn supports up to 10 values per query.
        for (int i = 0; i < uids.size(); i += 10) {
            List<String> chunk = uids.subList(i, Math.min(i + 10, uids.size()));
            chunkTasks.add(
                    profilesRef.whereIn(FieldPath.documentId(), chunk).get()
            );
        }

        // Use the non-typed overload and cast each element to QuerySnapshot.
        return Tasks.whenAllSuccess(chunkTasks)
                .continueWith(t -> {
                    List<Profile> out = new ArrayList<>();
                    List<Object> results = t.getResult(); // may be null

                    if (results != null) {
                        for (Object obj : results) {
                            QuerySnapshot qs = (QuerySnapshot) obj;
                            for (DocumentSnapshot doc : qs.getDocuments()) {
                                Profile p = doc.toObject(Profile.class);
                                if (p != null) {
                                    out.add(p);
                                }
                            }
                        }
                    }
                    return out;
                });
    }
}
