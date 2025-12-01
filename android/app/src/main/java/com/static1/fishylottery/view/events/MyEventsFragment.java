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

/**
 * Fragment that displays a list of events the current user is registered for.
 * Allows navigation to event details.
 */
public class MyEventsFragment extends Fragment {

    /** TextView displayed when there are no events. */
    private TextView textNoEventsMessage;

    /** RecyclerView for displaying the list of events. */
    private RecyclerView myEventsRecycler;

    /** Repository for accessing event data. */
    private IEventRepository eventsRepo;

    /** Repository for accessing waitlist data. */
    private IWaitlistRepository waitlistRepo;

    /** Adapter for managing event data in the RecyclerView. */
    private EventAdapter adapter;

    /** Tag for logging purposes. */
    private final static String TAG = "MyEvents";

    /**
     * Default constructor that initializes the event and waitlist repositories.
     */
    public MyEventsFragment() {
        this.eventsRepo = new EventRepository();
        this.waitlistRepo = new WaitlistRepository();
    }

    /**
     * Constructor that allows injection of custom event and waitlist repositories.
     *
     * @param eventRepository   the event repository to use
     * @param waitlistRepository the waitlist repository to use
     */
    public MyEventsFragment(IEventRepository eventRepository, IWaitlistRepository waitlistRepository) {
        this.eventsRepo = eventRepository;
        this.waitlistRepo = waitlistRepository;
    }

    /**
     * Inflates the fragment's layout and sets up the RecyclerView and navigation.
     *
     * @param inflater           the LayoutInflater to inflate the layout
     * @param container          the parent ViewGroup
     * @param savedInstanceState the saved instance state, if any
     * @return the inflated View
     */
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
                    myEventsRecycler.setPadding(0, 0, 0, navView.getHeight());
                });
            }
        }

        getMyEvents(); // load

        return view;
    }

    /**
     * Fetches the user's registered events and updates the UI.
     */
    private void getMyEvents() {
        String uid = AuthManager.getInstance().getUserId();

        if (uid == null) {
            textNoEventsMessage.setVisibility(View.VISIBLE);
            return;
        }

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

    /**
     * Computes the list of events the user is registered for by matching waitlist entries.
     *
     * @param allEvents   the list of all events
     * @param myWaitlists the user's waitlist entries
     * @return the list of events the user is registered for
     */
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

    /**
     * Removes events that have already ended.
     *
     * @param events the list of events to filter
     */
    private void removePastEvents(List<Event> events) {
        Date now = new Date();

        // Remove past events
        events.removeIf(event -> event.getEventEndDate().before(now));
    }

    /**
     * Sorts the list of events by their start date in ascending order.
     *
     * @param events the list of events to sort
     */
    private void sortEventsByDate(List<Event> events) {
        // Sort events by event date, soonest first
        events.sort(Comparator.comparing(Event::getEventStartDate));
    }
}