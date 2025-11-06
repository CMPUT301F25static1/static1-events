package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;

import java.util.ArrayList;
import java.util.List;

public class BrowseEventsFragment extends Fragment {
    private List<Event> events;
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

        getEvents(); // load
        return view;
    }

    private void getEvents() {
        eventsRepo.fetchAllEvents().addOnSuccessListener(events -> {
            adapter.submitList(events);
        }).addOnFailureListener(e -> {
            // TODO: handle error, e.g., show a Toast
            Log.e("BrowseEvents", "Failed to fetch events", e);
        });
    }
}

