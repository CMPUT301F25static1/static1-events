package com.static1.fishylottery.view.events;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.model.repositories.IEventRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment that displays a list of events available for browsing.
 * Supports filtering by interest and date range, with navigation to event details.
 */
public class BrowseEventsFragment extends Fragment {

    /** Repository for accessing event data. */
    private IEventRepository eventsRepo;

    /** RecyclerView for displaying the list of events. */
    private RecyclerView recyclerView;

    /** Adapter for managing event data in the RecyclerView. */
    private EventAdapter adapter;

    /** Spinner for selecting event interests. */
    private Spinner spinnerInterests;

    /** EditText for selecting the start date filter. */
    private EditText etStartDate;

    /** EditText for selecting the end date filter. */
    private EditText etEndDate;

    /** Date format for parsing and displaying dates. */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    /** List of all events fetched from the repository. */
    private List<Event> allEvents = new ArrayList<>(); // Store all events

    /**
     * Default constructor that initializes the event repository.
     */
    public BrowseEventsFragment() {
        this.eventsRepo = new EventRepository();
    }

    /**
     * Constructor that allows injection of a custom event repository.
     *
     * @param eventRepository the event repository to use
     */
    public BrowseEventsFragment(IEventRepository eventRepository) {
        this.eventsRepo = eventRepository;
    }

    /**
     * Inflates the fragment's layout and sets up the RecyclerView, filters, and navigation.
     *
     * @param inflater           the LayoutInflater to inflate the layout
     * @param container          the parent ViewGroup
     * @param savedInstanceState the saved instance state, if any
     * @return the inflated View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_events, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_browse_events);
        spinnerInterests = view.findViewById(R.id.spinnerInterests);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(requireContext());
        adapter.setOnEventClickListener(event -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            Navigation.findNavController(view).navigate(R.id.action_events_to_eventDetails, bundle);
        });
        recyclerView.setAdapter(adapter);

        // Setup interests spinner
        setupInterestsSpinner();

        // Setup date picker fields
        setupPickerField(etStartDate);
        setupPickerField(etEndDate);

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        Button buttonApplyFilters = view.findViewById(R.id.buttonApplyFilters);
        View bottomSheet = view.findViewById(R.id.bottom_sheet_filters);
        BottomSheetBehavior<View> sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        sheetBehavior.setHideable(true);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        buttonApplyFilters.setOnClickListener(v -> {
            // When applying the filters, close the bottom navigation view
            filterEvents();
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        getEvents(); // load

        FragmentActivity activity = getActivity();

        if (activity != null) {
            BottomNavigationView navView = activity.findViewById(R.id.nav_view);
            if (navView != null) {
                navView.post(() -> {
                    recyclerView.setPadding(0, 0, 0, navView.getHeight());
                    bottomSheet.setPadding(0, 0, 0, navView.getHeight());
                });
            }

            // Add the menu provider for the filter button menu
            activity.addMenuProvider(new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.menu_event_filters, menu);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.action_filter) {
                        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        return true;
                    }
                    return false;
                }
            }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        }

        return view;
    }

    /**
     * Sets up the interests spinner with available options from resources.
     */
    private void setupInterestsSpinner() {
        String[] interestsArray = getResources().getStringArray(R.array.interests_array);
        ArrayAdapter<String> interestsAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                interestsArray
        );
        interestsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInterests.setAdapter(interestsAdapter);

        spinnerInterests.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Configures an EditText to prevent soft keyboard input and allow date picker clicks.
     *
     * @param et the EditText to configure
     */
    private void setupPickerField(EditText et) {
        et.setFocusable(false);
        et.setClickable(true);
        et.setInputType(InputType.TYPE_NULL);
    }

    /**
     * Displays a DatePickerDialog for the specified EditText, initializing with the current value if set.
     *
     * @param target the EditText to update with the selected date
     */
    private void showDatePicker(final EditText target) {
        final Calendar c = Calendar.getInstance();

        try {
            String currentText = target.getText().toString();
            if (!currentText.isEmpty()) {
                Date existing = dateFormat.parse(currentText);
                if (existing != null) c.setTime(existing);
            }
        } catch (Exception ignored) {}

        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    target.setText(dateFormat.format(selected.getTime()));
                }, y, m, d);
        dp.show();
    }

    /**
     * Filters the event list based on selected interest and date range, then updates the UI.
     */
    private void filterEvents() {
        List<Event> filteredEvents = new ArrayList<>(allEvents);
        applyFilters(filteredEvents);
        sortEventsByStartDate(filteredEvents);
        adapter.submitList(filteredEvents);
    }

    /**
     * Fetches all events from the repository and applies initial filters.
     */
    private void getEvents() {
        eventsRepo.fetchAllEvents().addOnSuccessListener(events -> {
            // Store all events
            allEvents = new ArrayList<>(events);

            // Filter events (based on registration period)
            removeClosedEvents(allEvents);

            // Apply initial filters and display
            filterEvents();
        }).addOnFailureListener(e -> {
            Log.e("BrowseEvents", "Failed to fetch events", e);
            Toast.makeText(requireContext(), "Could not get events", Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Applies interest and date range filters to the event list.
     *
     * @param events the list of events to filter
     */
    private void applyFilters(List<Event> events) {
        // Filter by interest
        if (spinnerInterests != null && spinnerInterests.getSelectedItemPosition() > 0) {
            String selectedInterest = spinnerInterests.getSelectedItem().toString().trim();

            events.removeIf(event -> {
                // Check if event has interests list
                List<String> eventInterests = event.getInterests();

                if (eventInterests == null || eventInterests.isEmpty()) {
                    return true; // Remove events without interests
                }

                // Check if any of the event's interests match the selected interest (case-insensitive)
                for (String interest : eventInterests) {
                    if (interest != null && interest.trim().equalsIgnoreCase(selectedInterest)) {
                        return false; // Keep this event - it has a matching interest
                    }
                }

                return true; // No matching interest found, remove this event
            });
        }

        // Filter by date range
        String startDateStr = etStartDate.getText().toString().trim();
        String endDateStr = etEndDate.getText().toString().trim();

        // Only apply date filter if both dates are set
        if (!startDateStr.isEmpty() && !endDateStr.isEmpty()) {
            try {
                Date startDate = dateFormat.parse(startDateStr);
                Date endDate = dateFormat.parse(endDateStr);

                if (startDate != null && endDate != null) {
                    // Validate that start date is not after end date
                    if (startDate.after(endDate)) {
                        Toast.makeText(requireContext(), "Start date must be before end date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Set time to start of day for startDate
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(startDate);
                    startCal.set(Calendar.HOUR_OF_DAY, 0);
                    startCal.set(Calendar.MINUTE, 0);
                    startCal.set(Calendar.SECOND, 0);
                    startCal.set(Calendar.MILLISECOND, 0);
                    startDate = startCal.getTime();

                    // Set time to end of day for endDate
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(endDate);
                    endCal.set(Calendar.HOUR_OF_DAY, 23);
                    endCal.set(Calendar.MINUTE, 59);
                    endCal.set(Calendar.SECOND, 59);
                    endCal.set(Calendar.MILLISECOND, 999);
                    endDate = endCal.getTime();

                    final Date finalStartDate = startDate;
                    final Date finalEndDate = endDate;

                    events.removeIf(event -> {
                        Date eventDate = event.getEventStartDate();
                        if (eventDate == null) {
                            return true; // Remove events without a date
                        }
                        // Event must start on or after startDate and on or before endDate
                        return eventDate.before(finalStartDate) || eventDate.after(finalEndDate);
                    });
                }
            } catch (Exception e) {
                Log.e("BrowseEvents", "Error parsing dates for filtering", e);
                Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
            }
        } else if (!startDateStr.isEmpty()) {
            // Only start date is set - show events on or after this date
            try {
                Date startDate = dateFormat.parse(startDateStr);
                if (startDate != null) {
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(startDate);
                    startCal.set(Calendar.HOUR_OF_DAY, 0);
                    startCal.set(Calendar.MINUTE, 0);
                    startCal.set(Calendar.SECOND, 0);
                    startCal.set(Calendar.MILLISECOND, 0);
                    final Date finalStartDate = startCal.getTime();

                    events.removeIf(event -> {
                        Date eventDate = event.getEventStartDate();
                        return eventDate == null || eventDate.before(finalStartDate);
                    });
                }
            } catch (Exception e) {
                Log.e("BrowseEvents", "Error parsing start date", e);
            }
        } else if (!endDateStr.isEmpty()) {
            // Only end date is set - show events on or before this date
            try {
                Date endDate = dateFormat.parse(endDateStr);
                if (endDate != null) {
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(endDate);
                    endCal.set(Calendar.HOUR_OF_DAY, 23);
                    endCal.set(Calendar.MINUTE, 59);
                    endCal.set(Calendar.SECOND, 59);
                    endCal.set(Calendar.MILLISECOND, 999);
                    final Date finalEndDate = endCal.getTime();

                    events.removeIf(event -> {
                        Date eventDate = event.getEventStartDate();
                        return eventDate == null || eventDate.after(finalEndDate);
                    });
                }
            } catch (Exception e) {
                Log.e("BrowseEvents", "Error parsing end date", e);
            }
        }
    }

    /**
     * Removes events whose registration period is closed.
     *
     * @param events the list of events to filter
     */
    private void removeClosedEvents(List<Event> events) {
        Date now = new Date();

        // Remove events after the registration deadline
        events.removeIf(event ->
                event.getRegistrationCloses() != null && event.getRegistrationCloses().before(now));

        // Remove events that are not open to registration
        events.removeIf(event ->
                event.getRegistrationOpens() != null && event.getRegistrationOpens().after(now));
    }

    /**
     * Sorts the list of events by their start date in ascending order.
     *
     * @param events the list of events to sort
     */
    private void sortEventsByStartDate(List<Event> events) {
        events.sort(Comparator.comparing(Event::getEventStartDate));
    }
}