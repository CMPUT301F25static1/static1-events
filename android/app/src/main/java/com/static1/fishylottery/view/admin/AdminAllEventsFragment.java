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

/**
 * Fragment that displays a complete list of all events for administrators.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Fetch and display all events retrieved from the event repository.</li>
 *     <li>Allow admins to navigate to event details.</li>
 *     <li>Allow admins to delete events using a confirmation dialog.</li>
 *     <li>Sort events chronologically by their start date.</li>
 * </ul>
 * <p>
 * This fragment is part of the administrative workflow for event management.
 */
public class AdminAllEventsFragment extends Fragment {

    private IEventRepository eventsRepo;
    private RecyclerView recyclerView;
    private EventAdapter adapter;

    private List<Event> allEvents = new ArrayList<>();

    /**
     * Constructor used for dependency injection (e.g., during testing).
     *
     * @param eventsRepo custom repository implementation for event operations.
     */
    public AdminAllEventsFragment(IEventRepository eventsRepo) {
        this.eventsRepo = eventsRepo;
    }

    /**
     * Default constructor used when instantiated by Android.
     * Initializes the fragment with the default {@link EventRepository}.
     */
    public AdminAllEventsFragment() {
        this.eventsRepo = new EventRepository();
    }

    /**
     * Inflates the layout for the admin events list, sets up the RecyclerView,
     * configures item click listeners for viewing details and deleting events,
     * and triggers loading the event list.
     *
     * @param inflater the LayoutInflater used to inflate views.
     * @param container the parent view container.
     * @param savedInstanceState saved fragment state.
     * @return the root view for this fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_all_events, container, false);

        recyclerView = view.findViewById(R.id.recycler_all_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Adapter configured for admin mode
        adapter = new EventAdapter(requireContext(), true);

        // Navigate to event details when clicked
        adapter.setOnEventClickListener(event -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            Navigation.findNavController(view)
                    .navigate(R.id.action_admin_all_events_to_eventDetails, bundle);
        });

        // Handle event deletion
        adapter.setOnDeleteClickListener(event -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete " + event.getTitle() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        recyclerView.setAdapter(adapter);

        getEvents();

        return view;
    }

    /**
     * Fetches all events from the repository and updates the UI.
     * <p>
     * On success:
     * <ul>
     *     <li>Copies events locally</li>
     *     <li>Sorts them by start date</li>
     *     <li>Submits the list to the adapter</li>
     * </ul>
     * <p>
     * On failure:
     * <ul>
     *     <li>Logs an error to Logcat</li>
     *     <li>Displays a Toast to inform the admin</li>
     * </ul>
     */
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

    /**
     * Deletes the given event using the repository.
     * <p>
     * On success:
     * <ul>
     *     <li>Shows a confirmation toast</li>
     *     <li>Reloads the updated event list</li>
     * </ul>
     * On failure:
     * <ul>
     *     <li>Shows an error toast</li>
     * </ul>
     *
     * @param event the event to delete.
     */
    private void deleteEvent(Event event) {
        eventsRepo.deleteEvent(event).addOnSuccessListener(aVoid -> {
            Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();
            getEvents();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Sorts the provided list of events in ascending order by start date.
     *
     * @param events list of events to be sorted.
     */
    private void sortEventsByStartDate(List<Event> events) {
        events.sort(Comparator.comparing(Event::getEventStartDate));
    }
}
