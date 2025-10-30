package com.static1.fishylottery.view.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());


    private EditText
            textInputTitle,
            textInputDescription,
            textInputLocation,
            textInputHost,
            textInputWaitlistMaximum,
    startDate,
    startTime,
    endDate,
    endTime,
    deadlineDate,
    deadlineTime;
    private CreateEventControllerViewModel vm;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_create_event_details, container, false);

        vm = new ViewModelProvider(requireActivity()).get(CreateEventControllerViewModel.class);

        Button nextButton = view.findViewById(R.id.button_next_poster);
        textInputTitle = view.findViewById(R.id.input_event_title);
        textInputDescription = view.findViewById(R.id.input_event_description);
        textInputLocation = view.findViewById(R.id.input_location);
        textInputHost = view.findViewById(R.id.input_hosted_by);

        // find views
        startDate = view.findViewById(R.id.input_start_date);
        startTime = view.findViewById(R.id.input_start_time);
        endDate = view.findViewById(R.id.input_end_date);
        endTime = view.findViewById(R.id.input_end_time);
        deadlineDate = view.findViewById(R.id.input_deadline_date);
        deadlineTime = view.findViewById(R.id.input_deadline_time);

        // make them non-editable by keyboard but clickable
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
            Navigation.findNavController(view).navigate(R.id.action_eventDetails_to_eventPoster);
        });

        return view;
    }

    private void updateDetails() {
        Event event = vm.getEvent().getValue();

        if (event == null) return;

        event.setTitle(textInputTitle.getText().toString());
        event.setDescription(textInputDescription.getText().toString());
        event.setLocation(textInputLocation.getText().toString());
        event.setHostedBy(textInputHost.getText().toString());

        Date now = new Date();

        event.setCreatedAt(now);
        event.setUpdatedAt(now);

        vm.updateEvent(event);
    }

    private void setupPickerField(EditText et) {
        et.setFocusable(false);               // prevent keyboard
        et.setClickable(true);               // allow clicks
        et.setInputType(InputType.TYPE_NULL);
        // optional: give a content description or drawable icon
    }

    // DatePicker
    private void showDatePicker(final EditText target) {
        final Calendar c = Calendar.getInstance();

        // If target already has a date, try to initialize picker to it
        try {
            Date existing = dateFormat.parse(target.getText().toString());
            if (existing != null) c.setTime(existing);
        } catch (Exception ignored) {}

        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, dayOfMonth);
            target.setText(dateFormat.format(chosen.getTime()));
        }, y, m, d);

        dp.show();
    }

    // TimePicker
    private void showTimePicker(final EditText target) {
        final Calendar c = Calendar.getInstance();

        // initialize from existing value if parseable
        try {
            Date existing = timeFormat.parse(target.getText().toString());
            if (existing != null) {
                c.setTime(existing);
            }
        } catch (Exception ignored) {}

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        boolean is24Hour = android.text.format.DateFormat.is24HourFormat(requireContext());
        TimePickerDialog tp = new TimePickerDialog(requireContext(), (view, h, m) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(Calendar.HOUR_OF_DAY, h);
            chosen.set(Calendar.MINUTE, m);
            target.setText(timeFormat.format(chosen.getTime()));
        }, hour, minute, is24Hour);

        tp.show();
    }
}
