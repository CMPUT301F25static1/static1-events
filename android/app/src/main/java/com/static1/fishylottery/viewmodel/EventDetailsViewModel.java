package com.static1.fishylottery.viewmodel;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.GeoPoint;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.logic.JoinWaitlistRules;
import com.static1.fishylottery.model.repositories.IProfileRepository;
import com.static1.fishylottery.model.repositories.IWaitlistRepository;
import com.static1.fishylottery.model.repositories.ProfileRepository;
import com.static1.fishylottery.model.repositories.WaitlistRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.services.LocationService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDetailsViewModel extends ViewModel {

    private IWaitlistRepository waitlistRepository;
    private IProfileRepository profileRepository;
    private MutableLiveData<Event> event = new MutableLiveData<>();
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private MutableLiveData<WaitlistEntry> waitlistEntry = new MutableLiveData<>();
    private MutableLiveData<List<Profile>> waitingList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Profile>> invitedList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Profile>> enrolledList = new MutableLiveData<>(new ArrayList<>());

    // NEW: LiveData for waitlist count
    private MutableLiveData<Integer> waitlistCount = new MutableLiveData<>(0);

    public EventDetailsViewModel() {
        waitlistRepository = new WaitlistRepository();
        profileRepository = new ProfileRepository();
    }

    public EventDetailsViewModel(IWaitlistRepository waitlistRepository, IProfileRepository profileRepository) {
        this.waitlistRepository = waitlistRepository;
        this.profileRepository = profileRepository;
    }

    public LiveData<String> getMessage() { return message; }
    public LiveData<Event> getEvent() { return event; }
    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<WaitlistEntry> getWaitlistEntry() { return waitlistEntry; }

    public LiveData<Integer> getWaitlistCount() { return waitlistCount; }

    public void setEvent(Event event) {
        this.event.setValue(event);
    }

    public void loadWaitlistEntry(Event event) {
        String uid = AuthManager.getInstance().getUserId();
        if (uid == null) return;

        waitlistRepository.getWaitlistEntry(event, uid).addOnSuccessListener(entry -> {
            waitlistEntry.setValue(entry);
        });
    }

    public void loadWaitlistCount(Event event) {
        if (event == null) {
            Log.d("EventDetailsVM", "loadWaitlistCount: event is null");
            waitlistCount.postValue(0);
            return;
        }
        String eid = event.getEventId();
        Log.d("EventDetailsVM", "loadWaitlistCount for eventId=" + eid);
        if (eid == null) {
            waitlistCount.postValue(0);
            return;
        }

        waitlistRepository.getWaitlist(event)
                .addOnSuccessListener(entries -> {
                    int count = 0;
                    if (entries == null) {
                        Log.d("EventDetailsVM", "getWaitlist returned null");
                    } else {
                        Log.d("EventDetailsVM", "getWaitlist returned size=" + entries.size());
                        for (WaitlistEntry entry : entries) {
                            String s = entry == null ? null : entry.getStatus();
                            Log.d("EventDetailsVM", "entry status: " + s);
                            if (s != null) {
                                String ss = s.trim().toLowerCase(Locale.ROOT);
                                // count both waiting and invited (change as you need)
                                if ("waiting".equals(ss) || "invited".equals(ss)) {
                                    count++;
                                }
                            }
                        }
                    }
                    waitlistCount.postValue(count);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetailsVM", "Failed to load waitlist count", e);
                    waitlistCount.postValue(0);
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
    public void joinWaitlist(Context context) {
        Event e = event.getValue();

        if (e == null || e.getEventId() == null) {
            message.setValue("Missing event ID");
            return;
        }

        String uid = AuthManager.getInstance().getUserId();

        if (uid == null) {
            message.setValue("Please sign in first");
            return;
        }

        loading.setValue(true);

        LocationService locationService = LocationService.create(context);

        locationService.getCurrentLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationResult(Location location) {
                Log.d("JoinWaitlist", "The user's location is: " + location.toString());

                // Must validate if the user can join the event if there is geolocation requirement
                boolean canJoin = JoinWaitlistRules.canJoinWithGeolocationRequirement(e, location);

                if (!canJoin) {
                    // User is outside of the join radius
                    message.setValue("You are outside of the geolocation join radius");
                    loading.setValue(false);
                    return;
                }

                // User is able to join, so get the profile and add them to the waitlist
                profileRepository.getProfileById(uid)
                        .addOnSuccessListener(profile -> {
                            if (profile == null) {
                                loading.setValue(false);
                                message.setValue("Profile not found.");
                                return;
                            }

                            GeoPoint joinLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

                            WaitlistEntry entry = new WaitlistEntry();
                            entry.setJoinedAt(new Date());
                            entry.setProfile(profile);
                            entry.setStatus("waiting");
                            entry.setJoinLocation(joinLocation);

                            // *** changed here ***
                            waitlistRepository.addToWaitlistRespectingLimit(e, entry)
                                    .addOnSuccessListener(unused -> {
                                        loading.setValue(false);
                                        message.setValue("Joined waitlist!");
                                        waitlistEntry.setValue(entry);
                                        // Reload waitlist count
                                        loadWaitlistCount(e);
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

            @Override
            public void onLocationError(Exception e) {
                loading.setValue(false);
                message.setValue("Could not get device location: " + e.getMessage());
            }
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
                    // Reload waitlist count
                    loadWaitlistCount(e);
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

        // *** changed here ***
        waitlistRepository.addToWaitlistRespectingLimit(event.getValue(), currentEntry)
                .addOnSuccessListener(unused -> {
                    loading.setValue(false);
                    message.setValue("Successfully accepted invite!");

                    // Update the live data with the current entry
                    waitlistEntry.setValue(currentEntry);
                    // Reload waitlist count (one less in waiting)
                    loadWaitlistCount(currentEvent);
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

        // Get the entrant's UID (needed for repository call)
        Profile profile = currentEntry.getProfile();
        if (profile == null || profile.getUid() == null) {
            message.setValue("Could not identify entrant");
            return;
        }
        String uid = profile.getUid();

        // Update local model for UI
        currentEntry.setStatus("declined");
        currentEntry.setDeclinedAt(new Date());

        loading.setValue(true);

        // Use the new repository method that also draws a replacement entrant
        waitlistRepository.declineInvitationAndDrawReplacement(currentEvent, uid)
                .addOnSuccessListener(unused -> {
                    loading.setValue(false);
                    message.setValue("Successfully declined invite!");
                    // Update LiveData so UI shows declined state
                    waitlistEntry.setValue(currentEntry);
                    // Reload waitlist count – someone else may have just been invited
                    loadWaitlistCount(currentEvent);
                })
                .addOnFailureListener(exception -> {
                    loading.setValue(false);
                    message.setValue("Could not decline invite");
                    Log.e("Waitlist", "Failed to decline invite", exception);
                });
    }
}