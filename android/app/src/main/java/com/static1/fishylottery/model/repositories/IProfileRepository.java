package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.static1.fishylottery.model.entities.Profile;

import java.util.List;

/**
 * Defines the profile repository interface which includes utilities to add, update, delete, and
 * fetch profiles from a remote repository. This is used by Firestore in the app as well as by
 * fakes models in unit testing and UI testing.
 */
public interface IProfileRepository {
    /**
     * Adds a profile to the profiles collection. The UID is already determined from Firebase Auth.
     *
     * @param profile The profile to be added.
     * @return A task indicating success or failure.
     */
    public Task<Void> addProfile(Profile profile);

    /**
     * Update the profile with a new object.
     *
     * @param profile The new profile object that should be updated.
     * @return A task indicating success or failure.
     */
    public Task<Void> updateProfile(Profile profile);

    /**
     * Deletes the profile from the profiles collection.
     *
     * @param profile The profile object to delete (only requires the ID to exist)
     * @return A task indicating success or failure.
     */
    Task<Void> deleteProfile(Profile profile);
    /**
     * Gets a list of all of the profiles currently in the database.
     * @return A task with a list of profile objects.
     */
    Task<List<Profile>> getAllProfiles();

    /**
     * Get a single profile object by the UID.
     *
     * @param uid The UID of the profile. Most often comes from Firebase Auth.
     * @return A task with a profile object. Will be null if doesn't exist.
     */
    Task<Profile> getProfileById(String uid);

    /**
     * Fetch many profiles by their UIDs. Uses chunked whereIn queries (max 10 IDs per query).
     */
    Task<List<Profile>> fetchProfilesByIds(List<String> uids);
}
