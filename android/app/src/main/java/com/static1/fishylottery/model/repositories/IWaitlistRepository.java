package com.static1.fishylottery.model.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;

import java.util.List;

/**
 * Defines the waitlist repository interface which is used by Firestore and by fake models
 * when doing unit testing and UI testing.
 */
public interface IWaitlistRepository {
    /**
     * An entrant can join a waitlist with the event and the created waitlist entry containing the
     * profile of who is joining the waitlist.
     *
     * @param event The event the waitlist belongs to.
     * @param entry Information about the entrant profile and status.
     * @return A task indicating success or failure.
     */
    Task<Void> addToWaitlist(@NonNull Event event, @NonNull WaitlistEntry entry);

    /**
     * Get the full waitlist of everyone on it for a specified event.
     *
     * @param event The event object.
     * @return A list of waitlist entries.
     */
    Task<List<WaitlistEntry>> getWaitlist(@NonNull Event event);

    /**
     * Gets the waitlist entry given the event and uid if it exists.
     *
     * @param event The event object for the waitlist.
     * @param uid The UID of the entrant on the waitlist.
     * @return The entry or null if it does not exist.
     */
    Task<WaitlistEntry> getWaitlistEntry(@NonNull Event event, String uid);

    /**
     * Get the entries across all events that a user is on the waitlist for.
     *
     * @param uid The UID of the entrant user.
     * @return A list of all waitlist entries that belong to that user.
     */
    Task<List<WaitlistEntry>> getEventWaitlistEntriesByUser(@NonNull String uid);

    /**
     * Deletes a waitlist entry from the Firebase references to event waitlist and entrant waitlists.
     *
     * @param event The event for which the user is on the waitlist.
     * @param uid The uid of the user for which they are on the waitlist.
     * @return A task indicating success or failure.
     */
    Task<Void> deleteFromWaitlist(@NonNull Event event, @NonNull String uid);

    /**
     * Deletes from the waitlist by the UID.
     *
     * @param uid The UID of the entrant
     * @return A tasks indicating success of failure.
     */
    Task<Void> deleteFromWaitlistByUser(@NonNull String uid);

    Task<Void> addToWaitlistRespectingLimit(Event e, WaitlistEntry entry);

    /**
     * Marks an invited entrant as declined for the given event, and if possible
     * draws a replacement entrant from the remaining waitlist.
     *
     * @param event The event whose waitlist is being updated.
     * @param uid   The UID of the entrant who is declining the invitation.
     * @return A task indicating success or failure.
     */
    Task<Void> declineInvitationAndDrawReplacement(@NonNull Event event, @NonNull String uid);
}