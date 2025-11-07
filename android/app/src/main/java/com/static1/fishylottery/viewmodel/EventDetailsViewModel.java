package com.static1.fishylottery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.EventRepository;
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

    public void setEvent(Event event) {
        this.event.setValue(event);
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

    public boolean canJoinNow() {
        // TODO
        return false;
    }

    public void joinWaitlist(Event event) {
        if (event == null || event.getEventId() == null) {
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

                    waitlistRepository.joinWaitlist(event, entry)
                            .addOnSuccessListener(unused -> {
                                loading.setValue(false);
                                message.setValue("Joined waitlist!");
                            })
                            .addOnFailureListener(e -> {
                                loading.setValue(false);
                                message.setValue("Failed: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    loading.setValue(false);
                    message.setValue("Error loading profile: " + e.getMessage());
                });
    }
}