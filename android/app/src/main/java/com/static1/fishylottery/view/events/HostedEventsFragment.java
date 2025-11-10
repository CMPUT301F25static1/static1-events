package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.static1.fishylottery.MainActivity;
import com.static1.fishylottery.MainApplication;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.services.AuthManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HostedEventsFragment extends Fragment {

    private EventRepository eventsRepo;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private TextView textNoEventsMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_hosted_events, container, false);

        // The Event list
        eventsRepo = new EventRepository();
        textNoEventsMessage = view.findViewById(R.id.text_no_events_message);
        recyclerView = view.findViewById(R.id.recycler_hosted_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
                        adapter.submitList(events);

                        if (events.isEmpty()) {
                            textNoEventsMessage.setVisibility(View.VISIBLE);
                        } else {
                            textNoEventsMessage.setVisibility(View.GONE);
                        }

                    }).addOnFailureListener(e -> {
                        Log.e("HostedEvents", "Failed to fetch events", e);
                        Toast.makeText(getContext(), "Unable to get events", Toast.LENGTH_LONG).show();
                        textNoEventsMessage.setVisibility(View.VISIBLE);
                    });
        }
    }
}
