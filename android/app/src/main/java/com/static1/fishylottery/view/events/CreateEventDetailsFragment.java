package com.static1.fishylottery.view.events;


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
import android.widget.Toast;


import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
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


    private EditText
            textInputTitle,
            textInputDescription,
            textInputLocation,
            textInputHost,
            textInputWaitlistMaximum,
            textInputCapacity,
            textInputSelectCount,
            startDate,
            startTime,
            endDate,
            endTime,
            deadlineDate,
            deadlineTime;


    private AutoCompleteTextView eventTypeDropdown, eventInterestsDropdown;
    private CreateEventViewModel vm;


    private boolean[] selectedInterests;
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_create_event_details, container, false);


        vm = new ViewModelProvider(requireActivity()).get(CreateEventViewModel.class);


        // Observe validation errors from the ViewModel and surface as a Toast
        vm.getValidationError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
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
        textInputWaitlistMaximum = view.findViewById(R.id.input_waitlist_max);
        textInputSelectCount = view.findViewById(R.id.input_select_n);


        eventTypeDropdown = view.findViewById(R.id.dropdown_event_type);
        eventInterestsDropdown = view.findViewById(R.id.dropdown_interests);


        String[] eventTypes = {
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
                android.R.layout.simple_dropdown_item_1line,
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


        nextButton.setOnClickListener(v -> {
            updateDetails();
            Navigation.findNavController(view)
                    .navigate(R.id.action_eventDetails_to_eventPoster);
        });


        return view;
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
        event.setMaxWaitlistSize(safeParse(textInputWaitlistMaximum.getText().toString()));


        // Number of entrants to select (N). Null/blank => no fixed selection count.
        if (textInputSelectCount != null) {
            Integer selectCount = safeParse(textInputSelectCount.getText().toString());
            event.setSelectCount(selectCount);
        }


        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault());
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
}


