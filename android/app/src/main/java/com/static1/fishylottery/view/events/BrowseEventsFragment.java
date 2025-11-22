package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BrowseEventsFragment extends Fragment {
    private EventRepository eventsRepo;
    private RecyclerView recyclerView;
    private EventAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_events, container, false);

        eventsRepo = new EventRepository();

        recyclerView = view.findViewById(R.id.recycler_browse_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(requireContext());
        adapter.setOnEventClickListener(event -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            Navigation.findNavController(view).navigate(R.id.action_events_to_eventDetails, bundle);
        });
        recyclerView.setAdapter(adapter);

        BottomNavigationView navView = requireActivity().findViewById(R.id.nav_view);
        navView.post(() -> {
            recyclerView.setPadding(0, 0,0, navView.getHeight());
        });

        getEvents(); // load
        return view;
    }

    private void getEvents() {
        eventsRepo.fetchAllEvents().addOnSuccessListener(events -> {

            // Filter events (based on registration period)
            removeClosedEvents(events);

            // Sort events
            sortEventsByStartDate(events);

            // Update the list with adapter
            adapter.submitList(events);
        }).addOnFailureListener(e -> {
            Log.e("BrowseEvents", "Failed to fetch events", e);
            Toast.makeText(requireContext(), "Could not get events", Toast.LENGTH_LONG).show();
        });
    }

    private void removeClosedEvents(List<Event> events) {
        Date now = new Date();

        // Remove events after the registration deadline
        events.removeIf(event ->
                event.getRegistrationCloses() != null && event.getRegistrationCloses().before(now));

        // Remove events that are not open to registration
        events.removeIf(event ->
                event.getRegistrationOpens() != null && event.getRegistrationOpens().after(now));
    }

    private void sortEventsByStartDate(List<Event> events) {
        events.sort(Comparator.comparing(Event::getEventStartDate));
    }
}

