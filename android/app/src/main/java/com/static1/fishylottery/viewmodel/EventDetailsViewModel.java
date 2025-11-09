package com.static1.fishylottery.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.ProfileRepository;
import com.static1.fishylottery.model.repositories.WaitlistRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventDetailsViewModel extends ViewModel {

    private WaitlistRepository waitlistRepository;
    private ProfileRepository profileRepository;
    private MutableLiveData<Event> event = new MutableLiveData<>();
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private MutableLiveData<WaitlistEntry> waitlistEntry = new MutableLiveData<>();
    private MutableLiveData<List<Profile>> waitingList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Profile>> invitedList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Profile>> enrolledList = new MutableLiveData<>(new ArrayList<>());

    public EventDetailsViewModel() {
        waitlistRepository = new WaitlistRepository();
        profileRepository = new ProfileRepository();
    }

    public LiveData<String> getMessage() { return message; };
    public LiveData<Event> getEvent() {
        return event;
    }
    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<WaitlistEntry> getWaitlistEntry() {
        return waitlistEntry;
    }

    public void setEvent(Event event) {
        this.event.setValue(event);
    }

    public void loadWaitlistEntry(Event event) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) return;

        waitlistRepository.getWaitlistEntry(event, user.getUid()).addOnSuccessListener(entry -> {
            waitlistEntry.setValue(entry);
        });
    }

    public void loadEventEntrants(String eventId) {
        // simulated with empty lists
        // TODO: actual data fetching from Firestore needed
    }

    public void removeFromInvitedList(String userId, String eventId) {
        // TODO: Implement removal from invited list in database
        List<Profile> currentInvited = invitedList.getValue();
        if (currentInvited != null) {
            List<Profile> updatedList = new ArrayList<>();
            for (Profile profile : currentInvited) {
                if (!profile.getUid().equals(userId)) {
                    updatedList.add(profile);
                }
            }
            invitedList.setValue(updatedList);
        }
    }

    public LiveData<List<Profile>> getWaitingList() {
        return waitingList;
    }

    public LiveData<List<Profile>> getInvitedList() {
        return invitedList;
    }

    public LiveData<List<Profile>> getEnrolledList() {
        return enrolledList;
    }

    /**
     * Allows the user to join the waitlist.
     */
    public void joinWaitlist() {
        Event e = event.getValue();

        if (e == null || e.getEventId() == null) {
            message.setValue("Missing event ID");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            message.setValue("Please sign in first");
            return;
        }

        loading.setValue(true);
        String uid = user.getUid();

        profileRepository.getProfileById(uid)
                .addOnSuccessListener(profile -> {
                    if (profile == null) {
                        loading.setValue(false);
                        message.setValue("Profile not found.");
                        return;
                    }

                    WaitlistEntry entry = new WaitlistEntry();
                    entry.setJoinedAt(new Date());
                    entry.setProfile(profile);
                    entry.setStatus("waiting");

                    waitlistRepository.addToWaitlist(e, entry)
                            .addOnSuccessListener(unused -> {
                                loading.setValue(false);
                                message.setValue("Joined waitlist!");
                                waitlistEntry.setValue(entry);
                            })
                            .addOnFailureListener(exception -> {
                                loading.setValue(false);
                                message.setValue("Failed: " + exception.getMessage());
                            });
                })
                .addOnFailureListener(exception -> {
                    loading.setValue(false);
                    message.setValue("Error loading profile: " + exception.getMessage());
                });
    }

    /**
     * Allows the entrant to leave a waitlist. They must already be on a waitlist.
     * The UI buttons and success/error messages are updated by this method.
     */
    public void leaveWaitlist() {
        Event e = event.getValue();
        if (e == null) {
            return;
        }

        WaitlistEntry currentEntry = waitlistEntry.getValue();

        if (currentEntry == null) {
            message.setValue("User is not on a waitlist");
            return;
        }

        String profileId = currentEntry.getProfile().getUid();

        if (profileId == null) {
            message.setValue("Failed to leave waitlist");
            return;
        }

        waitlistRepository.deleteFromWaitlist(e, profileId)
                 .addOnSuccessListener(l -> {
                     loading.setValue(false);
                     message.setValue("Removed from the waitlist");
                     waitlistEntry.setValue(null);
                 })
                .addOnFailureListener(exception -> {
                    loading.setValue(false);
                    message.setValue("Failed to leave the waitlist");
                    Log.d("EventDetails", "Failed to delete from waitlist", exception);
                });
    }

    /**
     * Accepts the invite for a user if they have one.
     * User must be on a waitlist and their status is "invited"
     */
    public void acceptInvite() {
        WaitlistEntry currentEntry = waitlistEntry.getValue();
        Event currentEvent = event.getValue();

        if (currentEvent == null) {
            message.setValue("The event is not ready");
            return;
        }

        if (currentEntry == null) {
            message.setValue("Not on the waitlist");
            return;
        }

        if (!"invited".equals(currentEntry.getStatus())) {
            message.setValue("No invitation to accept");
            return;
        }

        // We have checked the conditions, the acceptance status can now be updated
        currentEntry.setStatus("accepted");
        currentEntry.setAcceptedAt(new Date());

        loading.setValue(true);

        waitlistRepository.addToWaitlist(event.getValue(), currentEntry)
                .addOnSuccessListener(unused -> {
                    loading.setValue(false);
                    message.setValue("Successfully accepted invite!");

                    // Update the live data with the current entry
                    waitlistEntry.setValue(currentEntry);
                })
                .addOnFailureListener(exception -> {
                    loading.setValue(false);
                    message.setValue("Could not accept invite");
                    Log.e("Waitlist", "Failed to accept invite", exception);
                });
    }

    public void declineInvite() {
        WaitlistEntry currentEntry = waitlistEntry.getValue();
        Event currentEvent = event.getValue();

        if (currentEvent == null) {
            message.setValue("The event is not ready");
            return;
        }

        if (currentEntry == null) {
            message.setValue("Not on the waitlist");
            return;
        }

        if (!"invited".equals(currentEntry.getStatus())) {
            message.setValue("No invitation to decline");
            return;
        }

        // Update status and timestamp
        currentEntry.setStatus("declined");
        currentEntry.setDeclinedAt(new Date());

        loading.setValue(true);

        waitlistRepository.addToWaitlist(currentEvent, currentEntry)
                .addOnSuccessListener(unused -> {
                    loading.setValue(false);
                    message.setValue("Successfully declined invite!");
                    waitlistEntry.setValue(currentEntry);
                })
                .addOnFailureListener(exception -> {
                    loading.setValue(false);
                    message.setValue("Could not decline invite");
                    Log.e("Waitlist", "Failed to decline invite", exception);
                });    }
}