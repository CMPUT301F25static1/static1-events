package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.EventDetailsViewModel;
import com.static1.fishylottery.model.entities.Event;

import java.io.Serializable;
import java.util.ArrayList;

public class MyEventsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_events, container, false);
    }

    public static class EventDetailsFragment extends Fragment {

        private EventDetailsViewModel viewModel;
        private Event currentEvent;

        private TextView eventTitle, eventDescription, eventLocation, eventCapacity;
        private RecyclerView waitingListRecycler, invitedListRecycler, enrolledListRecycler;
        private Button btnBack;

        private com.static1.fishylottery.view.events.EntrantAdapter waitingListAdapter, invitedListAdapter, enrolledListAdapter;

        public static EventDetailsFragment newInstance(Event event) {
            EventDetailsFragment fragment = new EventDetailsFragment();
            Bundle args = new Bundle();
            args.putSerializable("event", (Serializable) event);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                currentEvent = (Event) getArguments().getSerializable("event");
            }
            viewModel = new ViewModelProvider(this).get(EventDetailsViewModel.class);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_hosted_event_details_waitlist, container, false);

            initializeViews(view);
            setupRecyclerViews();
            loadEventData();
            observeData();

            return view;
        }

        private void initializeViews(View view) {
            eventTitle = view.findViewById(R.id.text_event_title);
            eventDescription = view.findViewById(R.id.text_event_description);
            eventLocation = view.findViewById(R.id.text_event_location);
            eventCapacity = view.findViewById(R.id.text_event_capacity);

            waitingListRecycler = view.findViewById(R.id.recycler_waiting_list);
            invitedListRecycler = view.findViewById(R.id.recycler_invited_list);
            enrolledListRecycler = view.findViewById(R.id.recycler_enrolled_list);

            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        private void setupRecyclerViews() {
            // Waiting List RecyclerView
            waitingListAdapter = new com.static1.fishylottery.view.events.EntrantAdapter(new ArrayList<>(), com.static1.fishylottery.view.events.EntrantAdapter.ListType.WAITING);
            waitingListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            waitingListRecycler.setAdapter(waitingListAdapter);

            // Invited List RecyclerView
            invitedListAdapter = new com.static1.fishylottery.view.events.EntrantAdapter(new ArrayList<>(), com.static1.fishylottery.view.events.EntrantAdapter.ListType.INVITED);
            invitedListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            invitedListRecycler.setAdapter(invitedListAdapter);

            // Enrolled List RecyclerView
            enrolledListAdapter = new com.static1.fishylottery.view.events.EntrantAdapter(new ArrayList<>(), com.static1.fishylottery.view.events.EntrantAdapter.ListType.ENROLLED);
            enrolledListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            enrolledListRecycler.setAdapter(enrolledListAdapter);
        }

        private void loadEventData() {
            if (currentEvent != null) {
                eventTitle.setText(currentEvent.getTitle());
                eventDescription.setText(currentEvent.getDescription());
                eventLocation.setText(currentEvent.getLocation());
                eventCapacity.setText(String.format("Capacity: %d", currentEvent.getCapacity()));

                // Load entrants data
                viewModel.loadEventEntrants(currentEvent.getEventId());
            }
        }

        private void observeData() {
            viewModel.getWaitingList().observe(getViewLifecycleOwner(), waitingList -> {
                waitingListAdapter.updateData(waitingList);
            });

            viewModel.getInvitedList().observe(getViewLifecycleOwner(), invitedList -> {
                invitedListAdapter.updateData(invitedList);
            });

            viewModel.getEnrolledList().observe(getViewLifecycleOwner(), enrolledList -> {
                enrolledListAdapter.updateData(enrolledList);
            });
        }
    }
}
