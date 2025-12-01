package com.static1.fishylottery.model.entities;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

/**
 * Specifies an item in a waitlist which most importantly contains the user's profile and
 * their status on the waitlist. Also have information about when they joined, accepted, or were
 * selected for the event.
 */
public class WaitlistEntry {
    private String status; // waiting | invited | accepted | declined | cancelled
    private Date joinedAt;
    private Date invitedAt;
    private Date declinedAt;
    private Date acceptedAt;
    private Profile profile;
    private GeoPoint joinLocation;
    private String eventId;
    /**
     * No-arg constructor required by Firestore.
     * Creates an empty waitlist entry.
     */
    public WaitlistEntry() { } // Firestore needs no-arg
    /**
     * Creates a waitlist entry for the given user.
     * The entry starts in the {@code "waiting"} status.
     *
     * @param userId the UID of the user joining the waitlist
     */
    public WaitlistEntry(String userId) {
        this.status = "waiting";
    }

    // getters & setters
    /**
     * Returns the current status of this waitlist entry.
     * Possible values: {@code "waiting"}, {@code "invited"},
     * {@code "accepted"}, {@code "declined"}, {@code "cancelled"}.
     *
     * @return the status string for this entry
     */
    public String getStatus() { return status; }
    /**
     * Sets the status of this waitlist entry.
     * Expected values include {@code "waiting"}, {@code "invited"},
     * {@code "accepted"}, {@code "declined"}, or {@code "cancelled"}.
     *
     * @param status the new status for the entry
     */
    public void setStatus(String status) { this.status = status; }
    /**
     * Returns the date/time when the user joined the waitlist.
     *
     * @return the join timestamp, or {@code null} if not recorded
     */
    public Date getJoinedAt() { return joinedAt; }
    /**
     * Sets the date/time when the user joined the waitlist.
     *
     * @param joinedAt the join timestamp to store
     */
    public void setJoinedAt(Date joinedAt) { this.joinedAt = joinedAt; }
    /**
     * Returns the date/time when the user was invited
     * to join the event from the waitlist.
     *
     * @return the invite timestamp, or {@code null} if not invited
     */
    public Date getInvitedAt() { return invitedAt; }
    /**
     * Sets the date/time when the user was invited
     * to join the event from the waitlist.
     *
     * @param invitedAt the invite timestamp to store
     */
    public void setInvitedAt(Date invitedAt) { this.invitedAt = invitedAt; }
    /**
     * Returns the date/time when the user accepted
     * their invitation from the waitlist.
     *
     * @return the acceptance timestamp, or {@code null} if not accepted
     */
    public Date getAcceptedAt() { return acceptedAt; }
    /**
     * Sets the date/time when the user accepted
     * their invitation from the waitlist.
     *
     * @param acceptedAt the acceptance timestamp to store
     */
    public void setAcceptedAt(Date acceptedAt) { this.acceptedAt = acceptedAt; }
    /**
     * Returns the date/time when the user declined
     * their invitation from the waitlist.
     *
     * @return the decline timestamp, or {@code null} if not declined
     */
    public Date getDeclinedAt() {
        return declinedAt;
    }
    /**
     * Sets the date/time when the user declined
     * their invitation from the waitlist.
     *
     * @param declinedAt the decline timestamp to store
     */
    public void setDeclinedAt(Date declinedAt) {
        this.declinedAt = declinedAt;
    }
    /**
     * Returns the profile of the user associated with this waitlist entry.
     *
     * @return the profile for this entry, or {@code null} if not loaded
     */
    public Profile getProfile() {
        return profile;
    }
    /**
     * Associates a profile with this waitlist entry.
     *
     * @param profile the profile of the user on the waitlist
     */
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
    /**
     * Returns the location where the user joined the waitlist,
     * if the app captured one.
     *
     * @return the join location as a {@link GeoPoint}, or {@code null}
     */
    public GeoPoint getJoinLocation()
    {
        return joinLocation;
    }
    /**
     * Sets the location where the user joined the waitlist.
     *
     * @param joinLocation the {@link GeoPoint} representing the join location
     */
    public void setJoinLocation(GeoPoint joinLocation) {
        this.joinLocation = joinLocation;
    }
    /**
     * Returns the ID of the event this waitlist entry belongs to.
     *
     * @return the event ID string
     */
    public String getEventId() {
        return eventId;
    }
    /**
     * Sets the ID of the event this waitlist entry belongs to.
     *
     * @param eventId the event ID string to associate with this entry
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}