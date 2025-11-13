package com.static1.fishylottery.model.entities;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

/**
 * Specifies an item in a waitlist which most importantly contains the user's profile and
 * their status on the waitlist. Also have information about when they joined, accepted, or were
 * selected for the event.
 */
public class WaitlistEntry {
    private String status; // waiting | invited | accepted | declined
    private Date joinedAt;
    private Date invitedAt;
    private Date declinedAt;
    private Date acceptedAt;
    private Profile profile;
    private GeoPoint joinLocation;

    public WaitlistEntry() { } // Firestore needs no-arg

    public WaitlistEntry(String userId) {
        this.status = "waiting";
    }

    // getters & setters

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Date joinedAt) { this.joinedAt = joinedAt; }
    public Date getInvitedAt() { return invitedAt; }
    public void setInvitedAt(Date invitedAt) { this.invitedAt = invitedAt; }
    public Date getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Date acceptedAt) { this.acceptedAt = acceptedAt; }
    public Date getDeclinedAt() {
        return declinedAt;
    }
    public void setDeclinedAt(Date declinedAt) {
        this.declinedAt = declinedAt;
    }
    public Profile getProfile() {
        return profile;
    }
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
    public GeoPoint getJoinLocation()
    {
        return joinLocation;
    }
    public void setJoinLocation(GeoPoint joinLocation) {
        this.joinLocation = joinLocation;
    }
}