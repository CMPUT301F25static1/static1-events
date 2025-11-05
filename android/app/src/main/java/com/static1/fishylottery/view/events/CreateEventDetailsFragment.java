package com.static1.fishylottery.view.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.CreateEventControllerViewModel;
import com.static1.fishylottery.model.entities.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateEventDetailsFragment extends Fragment {

    // Date/time display formats used by the pickers
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    // Text inputs
    private EditText
            textInputTitle,
            textInputDescription,
            textInputLocation,
            textInputHost;

    // Date/time inputs (as EditTexts that show pickers)
    private EditText
            startDate, startTime,
            endDate, endTime,
            deadlineDate, deadlineTime;

    private CreateEventControllerViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_event_details, container, false);

        vm = new ViewModelProvider(requireActivity())
                .get(CreateEventControllerViewModel.class);

        // Observe validation errors from the ViewModel and surface as a Toast
        vm.getValidationError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Buttons
        Button nextButton = view.findViewById(R.id.button_next_poster);

        // Basic text fields
        textInputTitle       = view.findViewById(R.id.input_event_title);
        textInputDescription = view.findViewById(R.id.input_event_description);
        textInputLocation    = view.findViewById(R.id.input_location);
        textInputHost        = view.findViewById(R.id.input_hosted_by);

        // Date/time fields
        startDate    = view.findViewById(R.id.input_start_date);
        startTime    = view.findViewById(R.id.input_start_time);
        endDate      = view.findViewById(R.id.input_end_date);
        endTime      = view.findViewById(R.id.input_end_time);
        deadlineDate = view.findViewById(R.id.input_deadline_date);
        deadlineTime = view.findViewById(R.id.input_deadline_time);

        // Make date/time EditTexts non-editable by keyboard but clickable to open pickers
        setupPickerField(startDate);
        setupPickerField(startTime);
        setupPickerField(endDate);
        setupPickerField(endTime);
        setupPickerField(deadlineDate);
        setupPickerField(deadlineTime);

        // Picker click listeners
        startDate.setOnClickListener(v -> showDatePicker(startDate));
        endDate.setOnClickListener(v -> showDatePicker(endDate));
        deadlineDate.setOnClickListener(v -> showDatePicker(deadlineDate));

        startTime.setOnClickListener(v -> showTimePicker(startTime));
        endTime.setOnClickListener(v -> showTimePicker(endTime));
        deadlineTime.setOnClickListener(v -> showTimePicker(deadlineTime));

        nextButton.setOnClickListener(v -> {
            updateDetails();
            if (vm.submit()) {
                Navigation.findNavController(v)
                        .navigate(R.id.action_eventDetails_to_eventPoster);
            }
        });

        return view;
    }

    /** Copies all UI values into the Event stored in the ViewModel. */
    private void updateDetails() {
        Event event = vm.getEvent().getValue();
        if (event == null) return;

        // Basic text
        event.setTitle(String.valueOf(textInputTitle.getText()));
        event.setDescription(String.valueOf(textInputDescription.getText()));
        event.setLocation(String.valueOf(textInputLocation.getText()));
        event.setHostedBy(String.valueOf(textInputHost.getText()));

        // Date+time pairs
        Date start = combineDateTime(startDate, startTime);
        Date end   = combineDateTime(endDate, endTime);
        Date regCloses = combineDateTime(deadlineDate, deadlineTime);

        event.setEventStartDate(start);
        event.setEventEndDate(end);
        event.setRegistrationCloses(regCloses);

        // audit timestamps
        Date now = new Date();
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(now);
        }
        event.setUpdatedAt(now);

        vm.updateEvent(event);
    }

    /** Prevent soft keyboard; allow click to open pickers. */
    private void setupPickerField(EditText et) {
        et.setFocusable(false);              // prevent keyboard focus
        et.setClickable(true);               // allow clicks
        et.setInputType(InputType.TYPE_NULL);
    }

    /** Opens a DatePickerDialog, initializing from current value if present. */
    private void showDatePicker(final EditText target) {
        final Calendar c = Calendar.getInstance();

        // If already set, initialize picker to that date
        try {
            Date existing = dateFormat.parse(String.valueOf(target.getText()));
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

    /** Opens a TimePickerDialog, initializing from current value if present. */
    private void showTimePicker(final EditText target) {
        final Calendar c = Calendar.getInstance();

        // If already set, initialize picker to that time
        try {
            Date existing = timeFormat.parse(String.valueOf(target.getText()));
            if (existing != null) c.setTime(existing);
        } catch (Exception ignored) {}

        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);

        boolean is24Hour = DateFormat.is24HourFormat(requireContext());

        TimePickerDialog tp = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    chosen.set(Calendar.MINUTE, minute);
                    target.setText(timeFormat.format(chosen.getTime()));
                },
                h, m, is24Hour
        );
        tp.show();
    }

    /** Combines a date EditText and time EditText to a single Date (or null if unparsable). */
    @Nullable
    private Date combineDateTime(EditText dateEt, EditText timeEt) {
        try {
            Date d = dateFormat.parse(String.valueOf(dateEt.getText()));
            Date t = timeFormat.parse(String.valueOf(timeEt.getText()));
            if (d == null || t == null) return null;

            Calendar cd = Calendar.getInstance(); cd.setTime(d);
            Calendar ct = Calendar.getInstance(); ct.setTime(t);

            cd.set(Calendar.HOUR_OF_DAY, ct.get(Calendar.HOUR_OF_DAY));
            cd.set(Calendar.MINUTE,     ct.get(Calendar.MINUTE));
            cd.set(Calendar.SECOND, 0);
            cd.set(Calendar.MILLISECOND, 0);
            return cd.getTime();
        } catch (Exception ignored) {
            return null;
        }
    }
}

