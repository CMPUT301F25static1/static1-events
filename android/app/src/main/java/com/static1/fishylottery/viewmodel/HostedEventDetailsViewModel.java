package com.static1.fishylottery.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.model.repositories.WaitlistRepository;
import com.static1.fishylottery.services.CsvExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HostedEventDetailsViewModel extends ViewModel {
    private final WaitlistRepository waitlistRepository;
    private final EventRepository eventRepository;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<List<WaitlistEntry>> waitlist = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Event> event = new MutableLiveData<>();

    public void setEvent(Event e) {
        event.setValue(e);
    }

    public LiveData<Event> getEvent() {
        return event;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<List<WaitlistEntry>> getWaitlist() {
        return waitlist;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public HostedEventDetailsViewModel() {
        waitlistRepository = new WaitlistRepository();
        eventRepository = new EventRepository();
    }

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

        eventRepository.drawEntrants(e.getEventId())
                .addOnSuccessListener(unused -> {
                    message.setValue("Draw complete! Selected entrants recorded.");
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

    }

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
}
