package com.static1.fishylottery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.model.repositories.IEventRepository;
import com.static1.fishylottery.model.repositories.IProfileRepository;
import com.static1.fishylottery.model.repositories.ProfileRepository;
import com.static1.fishylottery.view.admin.AdminOrganizerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminOrganizersViewModel extends ViewModel {
    private final IEventRepository eventRepository;
    private final IProfileRepository profileRepository;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<List<AdminOrganizerAdapter.OrganizerInfo>> organizers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    // Default constructor - uses real repositories
    public AdminOrganizersViewModel() {
        this.eventRepository = new EventRepository();
        this.profileRepository = new ProfileRepository();
    }

    // Constructor for testing - accepts fake repositories
    public AdminOrganizersViewModel(IEventRepository eventRepository, IProfileRepository profileRepository) {
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
    }

    public LiveData<List<AdminOrganizerAdapter.OrganizerInfo>> getOrganizers() {
        return organizers;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void loadOrganizers() {
        loading.setValue(true);

        // Get all events first
        eventRepository.fetchAllEvents()
                .addOnSuccessListener(events -> {
                    // Count events per organizer
                    Map<String, Integer> eventCounts = new HashMap<>();
                    for (Event event : events) {
                        String orgId = event.getOrganizerId();
                        if (orgId != null) {
                            eventCounts.put(orgId, eventCounts.getOrDefault(orgId, 0) + 1);
                        }
                    }

                    // Get profile info for each organizer
                    List<AdminOrganizerAdapter.OrganizerInfo> organizerList = new ArrayList<>();

                    if (eventCounts.isEmpty()) {
                        organizers.setValue(organizerList);
                        loading.setValue(false);
                        return;
                    }

                    int totalOrganizers = eventCounts.size();
                    final int[] processedOrganizers = {0};

                    for (Map.Entry<String, Integer> entry : eventCounts.entrySet()) {
                        String orgId = entry.getKey();
                        int count = entry.getValue();

                        profileRepository.getProfileById(orgId)
                                .addOnSuccessListener(profile -> {
                                    if (profile != null) {
                                        AdminOrganizerAdapter.OrganizerInfo info =
                                                new AdminOrganizerAdapter.OrganizerInfo(
                                                        orgId,
                                                        profile.getFullName(),
                                                        profile.getEmail(),
                                                        count
                                                );
                                        organizerList.add(info);
                                    }

                                    processedOrganizers[0]++;
                                    if (processedOrganizers[0] == totalOrganizers) {
                                        organizers.setValue(organizerList);
                                        loading.setValue(false);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedOrganizers[0]++;
                                    if (processedOrganizers[0] == totalOrganizers) {
                                        organizers.setValue(organizerList);
                                        loading.setValue(false);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    message.setValue("Failed to load organizers: " + e.getMessage());
                    loading.setValue(false);
                });
    }

    public void removeOrganizer(String organizerId) {
        loading.setValue(true);

        // First, delete all events by this organizer
        eventRepository.fetchEventsByOrganizerId(organizerId)
                .addOnSuccessListener(events -> {
                    // Delete each event
                    int totalEvents = events.size();
                    if (totalEvents == 0) {
                        // No events, just delete the profile
                        deleteOrganizerProfile(organizerId);
                        return;
                    }

                    final int[] deletedEvents = {0};
                    for (Event event : events) {
                        eventRepository.deleteEvent(event)
                                .addOnSuccessListener(aVoid -> {
                                    deletedEvents[0]++;
                                    if (deletedEvents[0] == totalEvents) {
                                        // All events deleted, now delete the profile
                                        deleteOrganizerProfile(organizerId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    deletedEvents[0]++;
                                    if (deletedEvents[0] == totalEvents) {
                                        deleteOrganizerProfile(organizerId);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    message.setValue("Failed to fetch organizer events: " + e.getMessage());
                    loading.setValue(false);
                });
    }

    private void deleteOrganizerProfile(String organizerId) {
        profileRepository.getProfileById(organizerId)
                .addOnSuccessListener(profile -> {
                    if (profile != null) {
                        profileRepository.deleteProfile(profile)
                                .addOnSuccessListener(aVoid -> {
                                    message.setValue("Organizer removed successfully");
                                    loadOrganizers(); // Reload the list
                                })
                                .addOnFailureListener(e -> {
                                    message.setValue("Failed to remove organizer profile: " + e.getMessage());
                                    loading.setValue(false);
                                });
                    } else {
                        message.setValue("Organizer profile not found");
                        loading.setValue(false);
                    }
                });
    }
}