package com.static1.fishylottery.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.static1.fishylottery.model.entities.AppNotification;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.model.repositories.IEventRepository;
import com.static1.fishylottery.model.repositories.IWaitlistRepository;
import com.static1.fishylottery.model.repositories.NotificationRepository;
import com.static1.fishylottery.model.repositories.WaitlistRepository;
import com.static1.fishylottery.services.CsvExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewModel for sharing data between the organizer's hosted event details screens.
 */
public class HostedEventDetailsViewModel extends ViewModel {
    private final IWaitlistRepository waitlistRepository;
    private final IEventRepository eventRepository;
    private final NotificationRepository notificationRepository;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<List<WaitlistEntry>> waitlist = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Event> event = new MutableLiveData<>();

    public HostedEventDetailsViewModel() {
        this.eventRepository = new EventRepository();
        this.waitlistRepository = new WaitlistRepository();
        this.notificationRepository = new NotificationRepository();
    }

    /**
     * Constructs a ViewModel instance using interfaces for the event and waitlist repository so
     * that the appropriate dependencies can be injected, such as in the case of unit and UI
     * testing.
     *
     * @param eventRepository The event repository.
     * @param waitlistRepository The waitlist repository.
     */
    public HostedEventDetailsViewModel(IEventRepository eventRepository, IWaitlistRepository waitlistRepository) {
        this.eventRepository = eventRepository;
        this.waitlistRepository = waitlistRepository;
        this.notificationRepository = new NotificationRepository();
    }


    /**
     * Set the event for the live data.
     *
     * @param e The event object.
     */
    public void setEvent(Event e) {
        event.setValue(e);
    }

    /**
     * Returns the event live data.
     *
     * @return The live event.
     */
    public LiveData<Event> getEvent() {
        return event;
    }

    /**
     * Returns a message indicating success or failure to be used in a toast in the view.
     *
     * @return The live message string.
     */
    public LiveData<String> getMessage() {
        return message;
    }

    /**
     * Returns the waitlist entry list live data.
     *
     * @return The live waitlist.
     */
    public LiveData<List<WaitlistEntry>> getWaitlist() {
        return waitlist;
    }

    /**
     * A boolean indicating if the current state is loading used when making a call to the database.
     *
     * @return The live boolean data.
     */
    public LiveData<Boolean> isLoading() {
        return loading;
    }

    /**
     * Method to export the current final list of entrants to a selected file.
     *
     * @param context The application context which comes from the activity or fragment.
     * @param uri The file URI from the create document action intent.
     */
    public void exportCsv(Context context, Uri uri) {
        List<WaitlistEntry> acceptedEntrants = waitlist.getValue()
                .stream()
                .filter(e -> e.getStatus().equals("accepted"))
                .collect(Collectors.toList());

        if (acceptedEntrants.isEmpty()) {
            message.setValue("Waitlist is empty");
            return;
        }

        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            CsvExporter exporter = new CsvExporter();
            exporter.exportWaitlist(acceptedEntrants, out);
            message.setValue("Export complete!");
        } catch (IOException e) {
            Log.e("CsvExporter", e.getMessage() != null ? e.getMessage() : "Unknown CSV error");
            message.setValue("Unable to export CSV file");
        }
    }

    /**
     * Runs the lottery randomization algorithm when the button is selected.
     */
    public void runLottery() {
        Event e = event.getValue();

        if (e == null || e.getEventId() == null) {
            message.setValue("No event is selected");
            return;
        }

        Integer eventCapacity = e.getCapacity();

        if (eventCapacity == null || eventCapacity <= 0) {
            message.setValue("Event capacity is zero. None selected");
            return;
        }

        loading.setValue(true);

        // New algorithm for selection and sending instant notifications
        runLotteryWithSelectionAndNotifications(e);

//        eventRepository.drawEntrants(e.getEventId())
//                .addOnSuccessListener(unused -> {
//                    message.setValue("Draw complete! Selected entrants recorded.");
//                    loading.setValue(false);
//                    fetchWaitlist(e);
//                })
//                .addOnFailureListener(err -> {
//                    String msg = (err.getMessage() != null)
//                            ? err.getMessage()
//                            : "Draw failed.";
//                    message.setValue(msg);
//                    loading.setValue(false);
//                });
    }

    /**
     * Fetches the waitlist for a specific event and saves to the viewmodel state.
     *
     * @param event The event object.
     */
    public void fetchWaitlist(@NonNull Event event) {
        loading.setValue(true);
        waitlistRepository.getWaitlist(event)
                .addOnSuccessListener(list -> {
                    waitlist.setValue(list);
                    loading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    message.setValue("Failed to fetch waitlist");
                    loading.setValue(false);
                });
    }

    /**
     * Manually update the waitlist status for an entrant on the waiting list.
     * @param entry The entry to update.
     * @param status The new status to change to.
     */
    public void updateEntrantStatus(@NonNull WaitlistEntry entry, @NonNull String status) {
        Event e = event.getValue();

        if (e == null) {
            message.setValue("Error no event");
            return;
        }

        // Update the entry status for the waitlist
        entry.setStatus(status);

        // Start the update
        loading.setValue(true);
        waitlistRepository.addToWaitlist(e, entry)
                .addOnSuccessListener(a -> {
                    loading.setValue(false);
                    message.setValue("Update entrant on waitlist");
                })
                .addOnFailureListener(exception -> {
                    loading.setValue(false);
                    message.setValue("Failed to update entrant");
                    Log.e("Waitlist", "Failed to update entrant", exception);
                });
    }

    /**
     * Deletes an entrant from the waitlist given the waitlist entry item.
     *
     * @param entry The waitlist entry,
     */
    public void deleteEntrant(@NonNull WaitlistEntry entry) {
        Event e = event.getValue();

        if (e == null) {
            message.setValue("Error no event");
            return;
        }

        String uid = entry.getProfile().getUid();

        if (uid == null) {
            message.setValue("Cannot delete entrant from waitlist");
            return;
        }

        loading.setValue(true);
        waitlistRepository.deleteFromWaitlist(e, uid)
                .addOnSuccessListener(a -> {
                    loading.setValue(false);
                    message.setValue("Delete user from waitlist");
                })
                .addOnFailureListener(exception -> {
                    loading.setValue(false);
                    message.setValue("Unable to delete entrant from waitlist");
                    Log.e("Waitlist", "Unable to delete entrant from waitlist", exception);
                });
    }

    public void resetMessage() {
        message.setValue("");
    }

    private AppNotification createInvitedNotification(@NonNull Event e) {
        AppNotification n = new AppNotification();
        n.setEventId(e.getEventId());
        n.setCreatedAt(new Date());
        n.setTitle("You've Been Invited to " + e.getTitle());
        n.setMessage("You've won the event lottery! Please tap on the invite to accept or decline before the event begins.");
        n.setType("invitation");
        n.setStatus("pending");
        return n;
    }

    private AppNotification createNotSelectedNotification(@NonNull Event e) {
        AppNotification n = new AppNotification();
        n.setEventId(e.getEventId());
        n.setTitle("Sorry, you were not selected for " + e.getTitle());
        n.setMessage("Unfortunately, the lottery has selected others for the event, but if someone backs out, you can still be chosen. You will receive a notification if this happens. Good luck!");
        n.setCreatedAt(new Date());
        n.setType("declined");
        n.setStatus("pending");
        return n;
    }

    private void sendNotification(@NonNull String uid, @NonNull AppNotification notification) {
        notificationRepository.addNotification(uid, notification);
    }

    private void runLotteryWithSelectionAndNotifications(Event e) {
        // Get the current waitlist
        waitlistRepository.getWaitlist(e)
                .addOnSuccessListener(waitlist -> {
                    if (waitlist.isEmpty()) {
                        loading.setValue(false);
                        message.setValue("No one on the waitlist");
                        return;
                    }

                    int invitedCount = 0;
                    int acceptedCount = 0;

                    for (WaitlistEntry entry : waitlist) {
                        String status = entry.getStatus();

                        if ("accepted".equals(status)) {
                            acceptedCount++;
                        } else if ("invited".equals(status)) {
                            invitedCount++;
                        }
                    }

                    int maxSelectableEntrantsCount = e.getCapacity() - invitedCount - acceptedCount;

                    if (maxSelectableEntrantsCount <= 0) {
                        message.setValue("Max invitations has already been sent");
                        loading.setValue(false);
                        return;
                    }

                    // Get the list of entrants that are "waiting"
                    List<WaitlistEntry> waitingEntrants = waitlist.stream()
                            .filter(entry -> "waiting".equals(entry.getStatus()))
                            .collect(Collectors.toList());

                    // Shuffle the list of entrants (no particular order)
                    Collections.shuffle(waitingEntrants);

                    List<WaitlistEntry> invitedEntrants = new ArrayList<>();

                    // Loop through each entrant in the random order and update their invite status until
                    // room is full. Each entrant invited receives a notification and everyone else receives
                    // a not selected notification.
                    for (int i = 0; i < waitingEntrants.size(); i++) {
                        WaitlistEntry entry = waitingEntrants.get(i);
                        String uid = entry.getProfile().getUid();

                        // INVITE: This entrant will be invited
                        if (i < maxSelectableEntrantsCount) {
                            // Invite this person now
                            entry.setInvitedAt(new Date());
                            entry.setStatus("invited");
                            invitedEntrants.add(entry);

                            // Send the invite notification
                            sendNotification(uid, createInvitedNotification(e));
                        } else {
                            // NOT SELECTED: Not chosen this draw
                            // Send a notification to these entrants
                            sendNotification(uid, createNotSelectedNotification(e));
                        }
                    }

                    // Update the entrants
                    waitlistRepository.updateMultipleEntries(invitedEntrants)
                            .addOnSuccessListener(a -> {
                                message.setValue("Draw Complete!");
                                loading.setValue(false);
                                fetchWaitlist(e);
                            })
                            .addOnFailureListener(err -> {
                                String msg = (err.getMessage() != null)
                                        ? err.getMessage()
                                        : "Draw failed.";
                                message.setValue(msg);
                                loading.setValue(false);
                            });


                })
                .addOnFailureListener(err -> {
                    String msg = (err.getMessage() != null)
                            ? err.getMessage()
                            : "Draw failed.";
                    message.setValue(msg);
                    loading.setValue(false);
                });
    }
}
