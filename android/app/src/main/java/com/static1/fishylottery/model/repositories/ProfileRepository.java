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
    /**
     * Creates or overwrites a profile document in Firestore.
     *
     * @param profile the {@link Profile} to store; its {@code uid} is used
     *                as the document ID in the {@code "profiles"} collection
     * @return a {@link Task} that completes when the write has been committed
     */
    @Override
    public Task<Void> addProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).set(profile);
    }
    /**
     * Updates an existing profile document in Firestore.
     * <p>
     * This uses the profile's {@code uid} as the document ID and overwrites
     * the document with the new values.
     *
     * @param profile the updated {@link Profile} data
     * @return a {@link Task} that completes when the update has been committed
     */
    @Override
    public Task<Void> updateProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).set(profile);
    }
    /**
     * Deletes a profile document from Firestore.
     *
     * @param profile the {@link Profile} whose document should be removed;
     *                its {@code uid} is used as the document ID
     * @return a {@link Task} that completes when the document has been deleted
     */
    @Override
    public Task<Void> deleteProfile(Profile profile) {
        return profilesRef.document(profile.getUid()).delete();
    }
    /**
     * Loads all profiles from the {@code "profiles"} collection.
     *
     * @return a {@link Task} that resolves to a list of all
     *         {@link Profile} objects in Firestore; the list is empty if
     *         there are no profiles or if the collection cannot be read
     */
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
    /**
     * Retrieves a single profile by its unique user ID.
     *
     * @param uid the user ID / document ID of the profile to fetch
     * @return a {@link Task} that resolves to the {@link Profile} with the
     *         given ID, or {@code null} if no matching document exists
     */
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
    /**
     * Fetches multiple profiles for a list of user IDs.
     * <p>
     * Firestore {@code whereIn} queries are limited to 10 values per call,
     * so this method automatically splits the ID list into chunks of at most
     * 10 IDs and runs multiple queries when necessary.
     *
     * @param uids list of user IDs / document IDs to look up; if {@code null}
     *             or empty an empty list is returned
     * @return a {@link Task} that resolves to a list of {@link Profile}
     *         objects for all IDs that could be found; the list may be
     *         smaller than {@code uids} if some documents do not exist
     */
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
