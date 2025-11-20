package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.model.repositories.WaitlistRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.viewmodel.EventDetailsViewModel;
import com.static1.fishylottery.model.entities.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyEventsFragment extends Fragment {
    private TextView textNoEventsMessage;
    private RecyclerView myEventsRecycler;
    private EventRepository eventsRepo;
    private WaitlistRepository waitlistRepo;
    private EventAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        eventsRepo = new EventRepository();
        waitlistRepo = new WaitlistRepository();

        textNoEventsMessage = view.findViewById(R.id.text_no_events_message);

        myEventsRecycler = view.findViewById(R.id.recycler_my_events);
        myEventsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(requireContext());
        adapter.setOnEventClickListener(event -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            Navigation.findNavController(view).navigate(R.id.action_events_to_eventDetails, bundle);
        });
        myEventsRecycler.setAdapter(adapter);

        getMyEvents(); // load

        return view;
    }

    private void getMyEvents() {
        String uid = AuthManager.getInstance().getUserId();

        if (uid == null) {
            textNoEventsMessage.setVisibility(View.VISIBLE);
            return;
        };

        Task<List<WaitlistEntry>> myWaitlistsTask = waitlistRepo.getEventWaitlistEntriesByUser(uid);
        Task<List<Event>> allEventsTask = eventsRepo.fetchAllEvents();

        Tasks.whenAllSuccess(myWaitlistsTask, allEventsTask)
            .addOnSuccessListener(results -> {
                List<WaitlistEntry> myWaitlists = (List<WaitlistEntry>) results.get(0);
                List<Event> allEvents = (List<Event>) results.get(1);

                List<Event> myEvents = computeMyEvents(allEvents, myWaitlists);

                adapter.submitList(myEvents);
                textNoEventsMessage.setVisibility(myEvents.isEmpty() ? View.VISIBLE : View.GONE);
            })
            .addOnFailureListener(e -> {
                Log.e("MyEvents", "Unable to get my events", e);
                Toast.makeText(requireContext(), "Unable to get my events", Toast.LENGTH_LONG).show();
            });
    }

    private List<Event> computeMyEvents(List<Event> allEvents, List<WaitlistEntry> myWaitlists) {
        // Build a fast lookup set of eventIds that the user is on the waitlist for
        Set<String> waitlistedEventIds = new HashSet<>();
        for (WaitlistEntry entry : myWaitlists) {
            waitlistedEventIds.add(entry.getEventId());
        }

        // Filter events whose id matches one in the waitlist
        List<Event> result = new ArrayList<>();
        for (Event event : allEvents) {
            if (waitlistedEventIds.contains(event.getEventId())) {
                result.add(event);
            }
        }

        return result;
    }

}
