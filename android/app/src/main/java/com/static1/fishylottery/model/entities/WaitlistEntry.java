package com.static1.fishylottery.model.entities;

import com.google.firebase.Timestamp;

import java.util.Map;

public class WaitlistEntry {

    private String status; // waiting | invited | accepted | declined
    private Timestamp joinedAt;
    private Timestamp invitedAt;
    private Timestamp acceptedAt;
    private Profile profile;

    public WaitlistEntry() { } // Firestore needs no-arg

    public WaitlistEntry(String userId) {
        this.status = "waiting";
    }

    // getters & setters

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Timestamp joinedAt) { this.joinedAt = joinedAt; }

    public Timestamp getInvitedAt() { return invitedAt; }
    public void setInvitedAt(Timestamp invitedAt) { this.invitedAt = invitedAt; }

    public Timestamp getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Timestamp acceptedAt) { this.acceptedAt = acceptedAt; }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}