package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.services.AuthManager;

import java.util.Comparator;
import java.util.List;

public class HostedEventsFragment extends Fragment {

    private EventRepository eventsRepo;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private TextView textNoEventsMessage;
    private final static String TAG = "HostedEvents";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_hosted_events, container, false);

        // The Event list
        eventsRepo = new EventRepository();
        textNoEventsMessage = view.findViewById(R.id.text_no_events_message);
        recyclerView = view.findViewById(R.id.recycler_hosted_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        BottomNavigationView navView = requireActivity().findViewById(R.id.nav_view);
        navView.post(() -> {
            recyclerView.setPadding(0, 0,0, navView.getHeight());
        });

        adapter = new EventAdapter(requireContext());

        adapter.setOnEventClickListener(event -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            Navigation.findNavController(view).navigate(R.id.action_hostedEvents_to_hostedEventDetails, bundle);
        });

        recyclerView.setAdapter(adapter);

        FloatingActionButton addEvent = view.findViewById(R.id.fab_add_event);

        addEvent.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_hostedEvents_to_createEvent);
        });

        getEvents();

        return view;
    }

    private void getEvents() {
        String uid = AuthManager.getInstance().getUserId();

        if (uid != null) {
            eventsRepo.fetchEventsByOrganizerId(uid)
                    .addOnSuccessListener(events -> {
                        // Sort the events
                        sortEventsByDate(events);

                        // Update the UI
                        adapter.submitList(events);

                        textNoEventsMessage.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch events", e);
                        Toast.makeText(requireContext(), "Unable to get events", Toast.LENGTH_LONG).show();
                        textNoEventsMessage.setVisibility(View.VISIBLE);
                    });
        }
    }

    private void sortEventsByDate(List<Event> events) {
        events.sort(Comparator.comparing(Event::getEventEndDate, Comparator.reverseOrder()));
    }

}
