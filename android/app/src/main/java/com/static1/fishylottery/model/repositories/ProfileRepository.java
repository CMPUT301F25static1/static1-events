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
public class ProfileRepository implements IProfileRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference profilesRef = db.collection("profiles");

    @Override
    public Task<Void> addProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).set(profile);
    }

    @Override
    public Task<Void> updateProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).set(profile);
    }

    @Override
    public Task<Void> deleteProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).delete();
    }

    @Override
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

    @Override
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

    @Override
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
