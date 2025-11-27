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
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.model.repositories.IEventRepository;
import com.static1.fishylottery.model.repositories.IWaitlistRepository;
import com.static1.fishylottery.model.repositories.WaitlistRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.model.entities.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyEventsFragment extends Fragment {
    private TextView textNoEventsMessage;
    private RecyclerView myEventsRecycler;
    private IEventRepository eventsRepo;
    private IWaitlistRepository waitlistRepo;
    private EventAdapter adapter;
    private final static String TAG = "MyEvents";

    public MyEventsFragment() {
        this.eventsRepo = new EventRepository();
        this.waitlistRepo = new WaitlistRepository();
    }

    public MyEventsFragment(IEventRepository eventRepository, IWaitlistRepository waitlistRepository) {
        this.eventsRepo = eventRepository;
        this.waitlistRepo = waitlistRepository;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

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

        FragmentActivity activity = getActivity();

        if (activity != null) {
            BottomNavigationView navView = requireActivity().findViewById(R.id.nav_view);
            if (navView != null) {
                navView.post(() -> {
                    myEventsRecycler.setPadding(0, 0,0, navView.getHeight());
                });
            }
        }

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

                // Filter my events (remove old events)
                removePastEvents(myEvents);

                // Sort the remaining events by date
                sortEventsByDate(myEvents);

                // Update the list with adapter
                adapter.submitList(myEvents);

                // Set the no events text
                textNoEventsMessage.setVisibility(myEvents.isEmpty() ? View.VISIBLE : View.GONE);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Unable to get my events", e);
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

    private void removePastEvents(List<Event> events) {
        Date now = new Date();

        // Remove past events
        events.removeIf(event -> event.getEventEndDate().before(now));
    }

    private void sortEventsByDate(List<Event> events) {
        // Sort events by event date, soonest first
        events.sort(Comparator.comparing(Event::getEventStartDate));
    }

}
