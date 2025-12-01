package com.static1.fishylottery.view.events.create;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.GeoPoint;
import com.static1.fishylottery.R;
import com.static1.fishylottery.viewmodel.CreateEventViewModel;
import com.static1.fishylottery.services.DateUtils;


/**
 * Fragment view for showing the preview step of the event creation process for organizers.
 */
public class CreateEventPreviewFragment extends Fragment {

    CreateEventViewModel vm;
    /**
     * Inflates the event preview layout, binds UI elements to the {@link CreateEventViewModel},
     * and wires up the create/update button.
     *
     * <p>This method:
     * <ul>
     *     <li>Initializes the view model scoped to the create event navigation graph.</li>
     *     <li>Subscribes to validation error messages and shows them as toasts.</li>
     *     <li>Observes the event data and populates the preview fields.</li>
     *     <li>Observes the image URI and toggles the poster preview visibility.</li>
     *     <li>Handles submission, showing success feedback and navigating back to
     *     the events list when creation or update succeeds.</li>
     * </ul>
     * </p>
     *
     * @return the root view of the event preview screen
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Initialize the view model
        vm = initViewModel();

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
        TextView textWaitlistCount = view.findViewById(R.id.text_waitlist_count);
        TextView textGeolocationRequirementLocation = view.findViewById(R.id.text_geolocation_requirement_location);
        TextView textGeolocationRequirementRadius = view.findViewById(R.id.text_geolocation_requirement_radius);
        LinearLayout layoutGeolocationRequirement = view.findViewById(R.id.layout_geolocation);

        ImageView eventPosterImage = view.findViewById(R.id.image_event_poster);

        Button button = view.findViewById(R.id.button_create_event);

        eventPosterImage.setVisibility(View.GONE);

        // Show validation errors emitted by the ViewModel
        vm.getValidationError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

        vm.isEdit().observe(getViewLifecycleOwner(), isEdit -> {
            button.setText(isEdit ? "Update" : "Create");
        });

        button.setOnClickListener(v -> {
            boolean ok = vm.submit();   // runs the checks; saves if valid
            boolean isEdit = vm.isEdit().getValue();

            if (ok) {
                if (isEdit) {
                    Toast.makeText(requireContext(), "Event updated!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(requireContext(), "Event created!", Toast.LENGTH_SHORT).show();
                }

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

            if (event.getLocationRequirement() != null && event.getLocationRequirement().getEnabled()) {
                layoutGeolocationRequirement.setVisibility(View.VISIBLE);
                textGeolocationRequirementLocation.setText("Location: " + formatLocationRequirement(event.getLocationRequirement().getLocation()));
                textGeolocationRequirementRadius.setText(String.format("Radius: %.1f km", event.getLocationRequirement().getRadius() / 1000));
            } else {
                layoutGeolocationRequirement.setVisibility(View.GONE);
            }

            String maxAttendees = "Max Attendees: " + (event.getCapacity() != null ? event.getCapacity().toString() : "None");
            String maxWaitlistSize = "Max Waitlist: " + (event.getMaxWaitlistSize() != null ? event.getMaxWaitlistSize().toString() : "None");
            String numEntries = "Entrants on Waitlist: " + (event.countEntries());



            textMaxAttendees.setText(maxAttendees);
            textMaxWaitlistSize.setText(maxWaitlistSize);
            textWaitlistCount.setText(numEntries);
        });

        vm.getImageUri().observe(getViewLifecycleOwner(), imageUri -> {
            if (imageUri == null) {
                eventPosterImage.setVisibility(View.GONE);
            } else {
                eventPosterImage.setVisibility(View.VISIBLE);
                eventPosterImage.setImageURI(imageUri);
            }
        });

        return view;
    }

    private String formatLocationRequirement(GeoPoint geopoint) {

        if (geopoint == null) return "";

        double lat = geopoint.getLatitude();
        double lng = geopoint.getLongitude();

        String ns = lat >= 0 ? "N" : "S";
        String ew = lng >= 0 ? "E" : "W";

        double latAbs = Math.abs(lat);
        double lngAbs = Math.abs(lng);

        return String.format("%.6f° %s, %.6f° %s", latAbs, ns, lngAbs, ew);
    }

    private CreateEventViewModel initViewModel() {
        // Create the view model, but scope it to the create event navigation graph so that it
        // only lives the lifetime of the 3 views used to create or edit the event.
        ViewModelStoreOwner vmOwner = NavHostFragment.findNavController(this)
                .getViewModelStoreOwner(R.id.create_event_graph);

        return new ViewModelProvider(vmOwner).get(CreateEventViewModel.class);
    }
}
