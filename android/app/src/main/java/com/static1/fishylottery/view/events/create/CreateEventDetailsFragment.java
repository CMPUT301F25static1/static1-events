package com.static1.fishylottery.view.events.create;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.GeoPoint;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.GeolocationRequirement;
import com.static1.fishylottery.viewmodel.CreateEventViewModel;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CreateEventDetailsFragment extends Fragment {


    // Date/time display formats used by the pickers
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    private Double selectedLat = null;
    private Double selectedLng = null;
    private Double selectedRadius = null;

    private EditText
            textInputTitle,
            textInputDescription,
            textInputLocation,
            textInputHost,
            textInputWaitlistMaximum,
            textInputCapacity,
            textInputLatitude,
            textInputLongitude,
            textInputRadius,
            startDate,
            startTime,
            endDate,
            endTime,
            deadlineDate,
            deadlineTime;

    private Switch switchGeolocation;
    private AutoCompleteTextView eventTypeDropdown, eventInterestsDropdown;
    private CreateEventViewModel vm;

    // UI for optional waitlist limit
    private MaterialCheckBox checkLimitWaitlist;
    private TextInputLayout tilWaitlistMax;

    private boolean[] selectedInterests;
    private boolean geolocationEnabled = false;
    private final List<String> selectedItems = new ArrayList<>();
    private final List<String> interests = Arrays.asList(
            "Music",
            "Sports",
            "Art",
            "Technology",
            "Travel",
            "Food",
            "Fitness",
            "Education",
            "Nature",
            "Movies",
            "Reading",
            "Gaming",
            "Science",
            "Health",
            "Fashion",
            "Photography",
            "Volunteering",
            "Business",
            "Culture",
            "Socializing"
    );

    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_create_event_details, container, false);


        // Create the view model, but scope it to the
        vm = initViewModel();

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            Event event = (Event) bundle.getSerializable("event");

            if (event != null) {
                vm.updateEvent(event);
                vm.setIsEdit(true);
            } else {
                vm.setIsEdit(false);
            }
        }


        // Observe validation errors from the ViewModel and surface as a Toast
        vm.getValidationError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        vm.getEvent().observe(getViewLifecycleOwner(), event -> {
            boolean isEditing = vm.isEdit().getValue();

            if (!isEditing) return;

            // Update general text fields
            textInputTitle.setText(event.getTitle());
            textInputDescription.setText(event.getDescription());
            textInputCapacity.setText(String.format(Locale.getDefault(), "%d", event.getCapacity()));
            textInputLocation.setText(event.getLocation());
            textInputHost.setText(event.getHostedBy());
            textInputWaitlistMaximum.setText(event.getMaxWaitlistSize() == null ? "" : String.format(
                    Locale.getDefault(),
                    "%d",
                    event.getMaxWaitlistSize()
            ));

            // Update the date fields
            startDate.setText(dateFormat.format(event.getEventStartDate()));
            startTime.setText(timeFormat.format(event.getEventStartDate()));
            endDate.setText(dateFormat.format(event.getEventEndDate()));
            endTime.setText(timeFormat.format(event.getEventEndDate()));
            deadlineDate.setText(dateFormat.format(event.getRegistrationCloses()));
            deadlineTime.setText(timeFormat.format(event.getRegistrationCloses()));

            // Interests and event type
            selectedItems.clear();
            selectedItems.addAll(event.getInterests());
            eventInterestsDropdown.setText(TextUtils.join(", ", selectedItems));
            syncBooleanArray();

            // Event type
            if (event.getEventType() != null) {
                eventTypeDropdown.setText(event.getEventType(), false);
            }

            // Update the geolocation (if necessary)
            GeolocationRequirement geolocationRequirement = event.getLocationRequirement();

            if (geolocationRequirement != null) {
                GeoPoint location = geolocationRequirement.getLocation();
                selectedLng = location.getLongitude();
                selectedLat = location.getLatitude();
                selectedRadius = geolocationRequirement.getRadius();

                setGeolocationEnabled(true);
            } else {
                selectedLng = null;
                selectedLat = null;
                selectedRadius = null;

                setGeolocationEnabled(false);
            }

        });


        // Buttons
        Button nextButton = view.findViewById(R.id.button_next_poster);

        // Basic text fields
        textInputTitle = view.findViewById(R.id.input_event_title);
        textInputDescription = view.findViewById(R.id.input_event_description);
        textInputLocation = view.findViewById(R.id.input_location);
        textInputHost = view.findViewById(R.id.input_hosted_by);
        textInputCapacity = view.findViewById(R.id.input_capacity);

        // Waitlist (existing input) +  controls
        textInputWaitlistMaximum = view.findViewById(R.id.input_waitlist_max);
        checkLimitWaitlist      = view.findViewById(R.id.checkbox_limit_waitlist);
        tilWaitlistMax          = view.findViewById(R.id.layout_waitlist_max);

        switchGeolocation = view.findViewById(R.id.switch_geolocation);
        textInputLatitude = view.findViewById(R.id.input_latitude);
        textInputLongitude = view.findViewById(R.id.input_longitude);
        textInputRadius = view.findViewById(R.id.input_radius);

        textInputLatitude.setEnabled(false);
        textInputLongitude.setEnabled(false);
        textInputRadius.setEnabled(false);

        eventTypeDropdown = view.findViewById(R.id.dropdown_event_type);
        eventInterestsDropdown = view.findViewById(R.id.dropdown_interests);


        String[] eventTypes = {
                "Event type...",
                "Music",
                "Sports",
                "Education",
                "Workshop",
                "Networking",
                "Conference",
                "Festival",
                "Fundraiser",
                "Social Gathering",
                "Other"
        };


        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, // use android built-in
                eventTypes
        );


        selectedInterests = new boolean[interests.size()];


        eventInterestsDropdown.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
            builder.setTitle("Select Interests");
            builder.setMultiChoiceItems(
                    interests.toArray(new String[0]),
                    selectedInterests,
                    (dialog, which, isChecked) -> {
                        if (isChecked) {
                            selectedItems.add(interests.get(which));
                        } else {
                            selectedItems.remove(interests.get(which));
                        }
                    });
            builder.setPositiveButton("OK", (dialog, which) ->
                    eventInterestsDropdown.setText(TextUtils.join(", ", selectedItems)));
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });


        eventTypeDropdown.setAdapter(eventTypeAdapter);
        eventTypeDropdown.setOnClickListener(v -> eventTypeDropdown.showDropDown());
        eventTypeDropdown.setText(eventTypes[0], false);


        // Date/time fields
        startDate = view.findViewById(R.id.input_start_date);
        startTime = view.findViewById(R.id.input_start_time);
        endDate = view.findViewById(R.id.input_end_date);
        endTime = view.findViewById(R.id.input_end_time);
        deadlineDate = view.findViewById(R.id.input_deadline_date);
        deadlineTime = view.findViewById(R.id.input_deadline_time);


        // Make date/time EditTexts non-editable by keyboard but clickable to open pickers
        setupPickerField(startDate);
        setupPickerField(startTime);
        setupPickerField(endDate);
        setupPickerField(endTime);
        setupPickerField(deadlineDate);
        setupPickerField(deadlineTime);


        // click listeners
        startDate.setOnClickListener(v -> showDatePicker(startDate));
        endDate.setOnClickListener(v -> showDatePicker(endDate));
        deadlineDate.setOnClickListener(v -> showDatePicker(deadlineDate));


        startTime.setOnClickListener(v -> showTimePicker(startTime));
        endTime.setOnClickListener(v -> showTimePicker(endTime));
        deadlineTime.setOnClickListener(v -> showTimePicker(deadlineTime));

        // NEW: toggle waitlist limit UI
        checkLimitWaitlist.setOnCheckedChangeListener((btn, checked) -> {
            tilWaitlistMax.setEnabled(checked);
            tilWaitlistMax.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (!checked) {
                textInputWaitlistMaximum.setText(null);
            }
        });

        nextButton.setOnClickListener(v -> {
            updateDetails();
            NavHostFragment.findNavController(this).navigate(R.id.action_eventDetails_to_eventPoster);
        });

        setupLocationSwitch();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupLocationSwitch();
    }


    /** Copies all UI values into the Event stored in the ViewModel. */
    private void updateDetails() {
        Event event = vm.getEvent().getValue();
        if (event == null) return;


        // Set the new values for the event object
        event.setTitle(textInputTitle.getText().toString());
        event.setDescription(textInputDescription.getText().toString());
        event.setLocation(textInputLocation.getText().toString());
        event.setHostedBy(textInputHost.getText().toString());
        event.setEventType(eventTypeDropdown.getText().toString());
        event.setInterests(new ArrayList<>(selectedItems));
        event.setCapacity(safeParse(textInputCapacity.getText().toString()));

        // NEW: wire into model
        boolean limitEnabled = checkLimitWaitlist.isChecked();
        event.setWaitlistLimited(limitEnabled);
        event.setMaxWaitlistSize( // keep your existing field updated
                limitEnabled ? safeParse(textInputWaitlistMaximum.getText().toString()) : null
        );
        event.setWaitlistLimit(   // new explicit field if you added it in Event.java
                limitEnabled ? safeParse(textInputWaitlistMaximum.getText().toString()) : null
        );

        String eventType = eventTypeDropdown.getText().toString();

        if ("Event type...".equals(eventType)) {
            event.setEventType(null);
        } else {
            event.setEventType(eventType);
        }

        try {
            String eventStartDateString =
                    startDate.getText().toString() + " " + startTime.getText().toString();
            String eventEndDateString =
                    endDate.getText().toString() + " " + endTime.getText().toString();
            String eventRegistrationClosesString =
                    deadlineDate.getText().toString() + " " + deadlineTime.getText().toString();


            event.setEventStartDate(formatter.parse(eventStartDateString));
            event.setEventEndDate(formatter.parse(eventEndDateString));
            event.setRegistrationCloses(formatter.parse(eventRegistrationClosesString));
        } catch (ParseException e) {
            Log.e("Date", e.toString());
        }

        // Handle geolocation requirement case
        if (geolocationEnabled) {
            if (selectedLat != null && selectedLng != null && selectedRadius != null) {
                GeolocationRequirement locationRequirement = new GeolocationRequirement();

                locationRequirement.setEnabled(true);
                locationRequirement.setLocation(new GeoPoint(selectedLat, selectedLng));
                locationRequirement.setRadius(selectedRadius);

                event.setLocationRequirement(locationRequirement);
            }
        }


        // Update the event using the view model
        vm.updateEvent(event);
    }


    /** Prevent soft keyboard. allow click to open pickers. */
    private void setupPickerField(EditText et) {
        et.setFocusable(false);
        et.setClickable(true);
        et.setInputType(InputType.TYPE_NULL);
    }


    /** Opens a DatePickerDialog, initializing from current value if present. */
    private void showDatePicker(final EditText target) {
        final Calendar c = Calendar.getInstance();


        try {
            Date existing = dateFormat.parse(target.getText().toString());
            if (existing != null) c.setTime(existing);
        } catch (Exception ignored) {}


        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog dp = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(year, month, dayOfMonth);
                    target.setText(dateFormat.format(chosen.getTime()));
                },
                y, m, d
        );


        dp.show();
    }


    // TimePicker
    private void showTimePicker(final EditText target) {
        final Calendar c = Calendar.getInstance();


        try {
            Date existing = timeFormat.parse(target.getText().toString());
            if (existing != null) {
                c.setTime(existing);
            }
        } catch (Exception ignored) {}


        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);


        boolean is24Hour = android.text.format.DateFormat.is24HourFormat(requireContext());
        TimePickerDialog tp = new TimePickerDialog(
                requireContext(),
                (view, h, m) -> {
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(Calendar.HOUR_OF_DAY, h);
                    chosen.set(Calendar.MINUTE, m);
                    target.setText(timeFormat.format(chosen.getTime()));
                },
                hour, minute,
                is24Hour
        );


        tp.show();
    }


    private Integer safeParse(String s) {
        try {
            if (s == null) return null;
            s = s.trim();
            if (s.isEmpty()) return null;
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setupLocationSwitch() {
        switchGeolocation.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked && button.isPressed() && !geolocationEnabled) {
                // safe NavController call
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_eventDetails_locationPicker);
            } else {
                selectedLat = null;
                selectedLng = null;
                selectedRadius = null;
            }

            setGeolocationEnabled(isChecked);
        });

        getParentFragmentManager().setFragmentResultListener("locationPickerResult",
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    double lat = bundle.getDouble("lat");
                    double lng = bundle.getDouble("lng");
                    double radius = bundle.getDouble("radius");

                    selectedLng = lng;
                    selectedLat = lat;
                    selectedRadius = radius;

                    setGeolocationEnabled(true);
                });

    }

    private void setLatLonRadius(boolean showing) {
        if (showing) {

            if (selectedLat != null) {
                Log.d("GeoLocation", "Latitude: " + selectedLat);
                textInputLatitude.setVisibility(View.VISIBLE);
                textInputLatitude.setText(String.format("%.6f", selectedLat));

            }

            if (selectedLng != null) {
                Log.d("GeoLocation", "Longitude: " + selectedLng);
                textInputLongitude.setVisibility(View.VISIBLE);
                textInputLongitude.setText(String.format("%.6f", selectedLng));
            }

            if (selectedRadius != null) {
                Log.d("GeoLocation", "Radius: " + selectedRadius);
                textInputRadius.setVisibility(View.VISIBLE);
                textInputRadius.setText(String.format("%.1f", selectedRadius / 1000.0));
            }
        } else {
            textInputRadius.setVisibility(View.GONE);
            textInputLongitude.setVisibility(View.GONE);
            textInputLatitude.setVisibility(View.GONE);
        }
    }

    private void setGeolocationEnabled(boolean enabled) {
        if (enabled) {
            geolocationEnabled = true;
            switchGeolocation.setChecked(true);
            setLatLonRadius(true);
        } else {
            geolocationEnabled = false;
            switchGeolocation.setChecked(false);
            setLatLonRadius(false);
        }
    }

    private void syncBooleanArray() {
        for (int i = 0; i < interests.size(); i++) {
            selectedInterests[i] = selectedItems.contains(interests.get(i));
        }
    }

    private CreateEventViewModel initViewModel() {
        // Create the view model, but scope it to the create event navigation graph so that it
        // only lives the lifetime of the 3 views used to create or edit the event.
        ViewModelStoreOwner vmOwner = NavHostFragment.findNavController(this)
                .getViewModelStoreOwner(R.id.create_event_graph);

        return new ViewModelProvider(vmOwner).get(CreateEventViewModel.class);
    }
}


