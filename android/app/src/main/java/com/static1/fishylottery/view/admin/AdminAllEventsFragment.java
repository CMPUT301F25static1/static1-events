package com.static1.fishylottery.view.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.model.repositories.IEventRepository;
import com.static1.fishylottery.view.events.EventAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminAllEventsFragment extends Fragment {
    private IEventRepository eventsRepo;
    private RecyclerView recyclerView;
    private EventAdapter adapter;

    private List<Event> allEvents = new ArrayList<>();

    public AdminAllEventsFragment() {
        this.eventsRepo = new EventRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_all_events, container, false);

        recyclerView = view.findViewById(R.id.recycler_all_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(requireContext(), true);
        adapter.setOnEventClickListener(event -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            Navigation.findNavController(view).navigate(R.id.action_admin_all_events_to_eventDetails, bundle);
        });

        adapter.setOnDeleteClickListener(event -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete " + event.getTitle() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteEvent(event);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        recyclerView.setAdapter(adapter);

        getEvents();

        return view;
    }

    private void getEvents() {
        eventsRepo.fetchAllEvents().addOnSuccessListener(events -> {
            allEvents = new ArrayList<>(events);
            sortEventsByStartDate(allEvents);
            adapter.submitList(allEvents);
        }).addOnFailureListener(e -> {
            Log.e("AdminAllEvents", "Failed to fetch events", e);
            Toast.makeText(requireContext(), "Could not get events", Toast.LENGTH_LONG).show();
        });
    }

    private void deleteEvent(Event event) {
        eventsRepo.deleteEvent(event.getEventId()).addOnSuccessListener(aVoid -> {
            Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();
            getEvents();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
        });
    }

    private void sortEventsByStartDate(List<Event> events) {
        events.sort(Comparator.comparing(Event::getEventStartDate));
    }
}