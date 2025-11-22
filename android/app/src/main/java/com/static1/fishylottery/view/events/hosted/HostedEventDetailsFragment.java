


package com.static1.fishylottery.view.events.hosted;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.services.DateUtils;


public class HostedEventDetailsFragment extends Fragment {
    private Event event;
    private final EventRepository eventRepository = new EventRepository();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        if (getArguments() != null) {
            Object arg = getArguments().getSerializable("event");
            if (arg instanceof Event) {
                event = (Event) arg;
            }
        }


        View view = inflater.inflate(R.layout.fragment_hosted_event_details, container, false);


        Button buttonViewWaitlist   = view.findViewById(R.id.button_view_waitlist);
        Button buttonRunLottery     = view.findViewById(R.id.button_run_lottery);
        Button buttonViewMap        = view.findViewById(R.id.button_view_map);
        Button buttonExportEnrolled = view.findViewById(R.id.button_export_enrolled);
        Button buttonSendNotifications = view.findViewById(R.id.button_send_notifications);
        Button buttonViewQrCode     = view.findViewById(R.id.button_view_qr_code);
        Button buttonViewCancelledEntrants = view.findViewById(R.id.button_view_cancelled_entrants);

        // Event details card
        View eventDetailsCard   = view.findViewById(R.id.event_details_info);
        TextView textEventTitle = eventDetailsCard.findViewById(R.id.eventTitle);
        TextView textEventDate  = eventDetailsCard.findViewById(R.id.eventDate);
        TextView textEventTime  = eventDetailsCard.findViewById(R.id.eventTime);
        TextView textEventLocation = eventDetailsCard.findViewById(R.id.eventLocation);
        ImageView imageView     = eventDetailsCard.findViewById(R.id.eventImage);


        String imageUrl = (event != null) ? event.getImageUrl() : null;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imageView);
        }


        if (event != null) {
            textEventTitle.setText(event.getTitle());
            textEventLocation.setText(event.getLocation());
            textEventDate.setText(
                    DateUtils.formatDateRange(event.getEventStartDate(), event.getEventEndDate()));
            textEventTime.setText(
                    DateUtils.formatTimeRange(event.getEventStartDate(), event.getEventEndDate()));
        }


        // Nav hooks (pass event where needed)
        buttonViewWaitlist.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putSerializable("event", event);
            Navigation.findNavController(v)
                    .navigate(R.id.action_hostedEventDetails_to_hostedEventDetailsWaitlist, b);
        });


        buttonSendNotifications.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putSerializable("event", event);
            Navigation.findNavController(v)
                    .navigate(R.id.action_hostedEventsDetails_to_hostedEventDetailsSendNotifications, b);
        });


        buttonViewQrCode.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putSerializable("event", event);
            Navigation.findNavController(v)
                    .navigate(R.id.action_hostedEventDetails_to_viewQrCode, b);
        });


        buttonViewMap.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_hostedEventDetails_to_signupMap)
        );


        buttonExportEnrolled.setOnClickListener(v ->
                Snackbar.make(v, "Export not implemented yet.", Snackbar.LENGTH_LONG).show()
        );

        buttonViewCancelledEntrants.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putSerializable("event", event);
            Navigation.findNavController(v)
                    .navigate(R.id.action_hostedEventDetails_to_cancelledEntrants, b);
        });

        setupRunLotteryButton(buttonRunLottery);

        return view;
    }

    private void setupRunLotteryButton(Button button) {
        if (event == null || event.getEventId() == null) {
            button.setEnabled(false);
            button.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "No event loaded.", Toast.LENGTH_LONG).show()
            );
        } else {
            Integer n = event.getCapacity();
            if (n == null || n <= 0) {
                button.setEnabled(false);
                button.setOnClickListener(v ->
                        Toast.makeText(requireContext(), "Set a valid number of entrants to select.", Toast.LENGTH_LONG).show()
                );
            } else {
                button.setOnClickListener(v -> {
                    button.setEnabled(false);
                    eventRepository.drawEntrants(event.getEventId())
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(requireContext(), "Draw complete. Selected entrants recorded.", Toast.LENGTH_LONG).show();
                                button.setEnabled(true);
                            })
                            .addOnFailureListener(err -> {
                                String msg = (err != null && err.getMessage() != null)
                                        ? err.getMessage()
                                        : "Draw failed.";
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                                button.setEnabled(true);
                            });
                });
            }
        }
    }
}
