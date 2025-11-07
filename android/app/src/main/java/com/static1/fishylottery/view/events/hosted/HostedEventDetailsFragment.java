package com.static1.fishylottery.view.events.hosted;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import com.google.android.material.snackbar.Snackbar;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.services.DateUtils;


public class HostedEventDetailsFragment extends Fragment {


    private Event event;
    private final EventRepository eventRepository = new EventRepository();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            Object arg = getArguments().getSerializable("event");
            if (arg instanceof Event) {
                event = (Event) arg;
            }
        }


        View view = inflater.inflate(R.layout.fragment_hosted_event_details, container, false);


        Button buttonViewWaitlist      = view.findViewById(R.id.button_view_waitlist);
        Button buttonRunLottery        = view.findViewById(R.id.button_run_lottery);
        Button buttonViewMap           = view.findViewById(R.id.button_view_map);
        Button buttonExportEnrolled    = view.findViewById(R.id.button_export_enrolled);
        Button buttonSendNotifications = view.findViewById(R.id.button_send_notifications);
        Button buttonViewQrCode        = view.findViewById(R.id.button_view_qr_code);
        Button buttonRunDraw           = view.findViewById(R.id.button_run_draw); // new


        // ---- Populate event card (kept from main) ----
        View eventDetailsCard      = view.findViewById(R.id.event_details_info);
        TextView textEventTitle    = eventDetailsCard.findViewById(R.id.eventTitle);
        TextView textEventDate     = eventDetailsCard.findViewById(R.id.eventDate);
        TextView textEventTime     = eventDetailsCard.findViewById(R.id.eventTime);
        TextView textEventLocation = eventDetailsCard.findViewById(R.id.eventLocation);


        if (event != null) {
            textEventTitle.setText(event.getTitle());
            textEventLocation.setText(event.getLocation());
            textEventDate.setText(
                    DateUtils.formatDateRange(event.getEventStartDate(), event.getEventEndDate()));
            textEventTime.setText(
                    DateUtils.formatTimeRange(event.getEventStartDate(), event.getEventEndDate()));
        }


        // ---- Clicks kept from main (pass event bundle where needed) ----
        buttonViewWaitlist.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            Navigation.findNavController(view)
                    .navigate(R.id.action_hostedEventDetails_to_hostedEventDetailsWaitlist, bundle);
        });


        buttonSendNotifications.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_hostedEventsDetails_to_hostedEventDetailsSendNotifications));


        // Placeholder “Run Lottery” (separate from draw)
        buttonRunLottery.setOnClickListener(v ->
                Snackbar.make(v, "Run Lottery not implemented.", Snackbar.LENGTH_LONG).show());


        buttonExportEnrolled.setOnClickListener(v ->
                Snackbar.make(v, "Export not implemented yet.", Snackbar.LENGTH_LONG).show());


        buttonViewQrCode.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            Navigation.findNavController(view)
                    .navigate(R.id.action_hostedEventDetails_to_viewQrCode, bundle);
        });


        buttonViewMap.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_hostedEventDetails_to_signupMap));


        // ---- New: Run Draw logic ----
        if (buttonRunDraw != null) {
            if (event == null || event.getEventId() == null) {
                buttonRunDraw.setEnabled(false);
                buttonRunDraw.setOnClickListener(v ->
                        Snackbar.make(v, "No event loaded.", Snackbar.LENGTH_LONG).show());
            } else if (event.getSelectCount() == null || event.getSelectCount() <= 0) {
                buttonRunDraw.setEnabled(false);
                buttonRunDraw.setOnClickListener(v ->
                        Snackbar.make(v, "Set a valid number of entrants to select.", Snackbar.LENGTH_LONG).show());
            } else {
                buttonRunDraw.setOnClickListener(v -> {
                    buttonRunDraw.setEnabled(false);
                    eventRepository
                            .drawEntrants(event.getEventId(), event.getSelectCount())
                            .addOnSuccessListener(unused ->
                                    Snackbar.make(v, "Draw complete. Selected entrants recorded.",
                                            Snackbar.LENGTH_LONG).show())
                            .addOnFailureListener(err -> {
                                String msg = (err != null && err.getMessage() != null)
                                        ? err.getMessage() : "Draw failed.";
                                Snackbar.make(v, msg, Snackbar.LENGTH_LONG).show();
                                buttonRunDraw.setEnabled(true);
                            });
                });
            }
        }


        return view;
    }
}

