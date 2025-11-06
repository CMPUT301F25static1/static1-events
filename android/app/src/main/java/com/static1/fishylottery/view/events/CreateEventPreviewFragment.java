package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.static1.fishylottery.R;
import com.static1.fishylottery.viewmodel.CreateEventViewModel;
import com.static1.fishylottery.services.DateUtils;

/**
 * Fragment view for showing the preview step of the event creation process for organizers.
 */
public class CreateEventPreviewFragment extends Fragment {

    CreateEventViewModel vm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        vm = new ViewModelProvider(requireActivity()).get(CreateEventViewModel.class);

        View view = inflater.inflate(R.layout.fragment_create_event_preview, container, false);

        TextView textEventTitle = view.findViewById(R.id.text_event_title);
        TextView textEventDescription = view.findViewById(R.id.text_event_description);
        TextView textEventDate = view.findViewById(R.id.text_event_date);
        TextView textEventTime = view.findViewById(R.id.text_event_time);
        TextView textEventLocation = view.findViewById(R.id.text_event_location);
        TextView textRegistrationCloses = view.findViewById(R.id.text_event_registration);
        TextView textHostedBy = view.findViewById(R.id.text_hosted_by);
        TextView textMaxAttendees = view.findViewById(R.id.text_max_attendees);
        TextView textMaxWaitlistSize = view.findViewById(R.id.text_max_waitlist);

        ImageView eventPosterImage = view.findViewById(R.id.image_event_poster);

        Button button = view.findViewById(R.id.button_create_event);

        // Show validation errors emitted by the ViewModel
        vm.getValidationError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && getView() != null) {
                Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
            }
        });

        button.setOnClickListener(v -> {
            boolean ok = vm.submit();   // runs the checks; saves if valid
            if (ok) {
                Snackbar.make(v, "Event created!", Snackbar.LENGTH_SHORT).show();
                // navigate here for a nav target:
                Navigation.findNavController(view).popBackStack(R.id.navigation_events, false);
            }
            // If not ok, the observer above shows the error.
        });

        vm.getEvent().observe(getViewLifecycleOwner(), event -> {
            textEventTitle.setText(event.getTitle());
            textEventDescription.setText(event.getDescription());
            textEventDate.setText(DateUtils.formatDateRange(event.getEventStartDate(), event.getEventEndDate()));
            textEventTime.setText(DateUtils.formatTimeRange(event.getEventStartDate(), event.getEventEndDate()));
            textEventLocation.setText(event.getLocation());
            textHostedBy.setText(event.getHostedBy());
            textRegistrationCloses.setText(DateUtils.formatDateTime(event.getRegistrationCloses()));

            String maxAttendees = "Max Attendees: " + (event.getCapacity() != null ? event.getCapacity().toString() : "None");
            String maxWaitlistSize = "Max Waitlist: " + (event.getMaxWaitlistSize() != null ? event.getMaxWaitlistSize().toString() : "None");

            textMaxAttendees.setText(maxAttendees);
            textMaxWaitlistSize.setText(maxWaitlistSize);
        });

        vm.getImageUri().observe(getViewLifecycleOwner(), eventPosterImage::setImageURI);

        return view;
    }
}
