package com.static1.fishylottery.controller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.model.repositories.ProfileRepository;
import java.util.ArrayList;
import java.util.List;

public class EventDetailsViewModel extends ViewModel {

    private EventRepository eventRepository;
    private ProfileRepository profileRepository;

    private MutableLiveData<List<Profile>> waitingList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Profile>> invitedList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Profile>> enrolledList = new MutableLiveData<>(new ArrayList<>());

    public EventDetailsViewModel() {
        eventRepository = new EventRepository();
        profileRepository = new ProfileRepository();
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
}