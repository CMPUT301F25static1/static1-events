package com.static1.fishylottery.view.events.hosted;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import com.google.android.material.snackbar.Snackbar;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;


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


        Button buttonViewWaitlist = view.findViewById(R.id.button_view_waitlist);
        Button buttonRunLottery = view.findViewById(R.id.button_run_lottery);
        Button buttonViewMap = view.findViewById(R.id.button_view_map);
        Button buttonExportEnrolled = view.findViewById(R.id.button_export_enrolled);
        Button buttonSendNotifications = view.findViewById(R.id.button_send_notifications);
        Button buttonViewQrCode = view.findViewById(R.id.button_view_qr_code);
        Button buttonRunDraw = view.findViewById(R.id.button_run_draw);


        // View Waitlist
        buttonViewWaitlist.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_hostedEventDetails_to_hostedEventDetailsWaitlist));


        // Send Notifications
        buttonSendNotifications.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_hostedEventsDetails_to_hostedEventDetailsSendNotifications));


        // Run Lottery (still separate / future behavior if needed)
        buttonRunLottery.setOnClickListener(v -> {
            // TODO: Hook this up if you differentiate "lottery" vs "drawEntrants"
            Snackbar.make(v, "Run Lottery not implemented yet.", Snackbar.LENGTH_SHORT).show();
        });


        // Export Enrolled (placeholder)
        buttonExportEnrolled.setOnClickListener(v -> {
            // TODO: Export the CSV of enrolled
            Snackbar.make(v, "Export not implemented yet.", Snackbar.LENGTH_SHORT).show();
        });


        // View QR Code
        buttonViewQrCode.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            Navigation.findNavController(view)
                    .navigate(R.id.action_hostedEventDetails_to_viewQrCode, bundle);
        });


        // View Map
        buttonViewMap.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_hostedEventDetails_to_signupMap));


        // Run Draw
        if (event == null || event.getEventId() == null) {
            // No event info: disable draw
            buttonRunDraw.setEnabled(false);
            buttonRunDraw.setOnClickListener(v ->
                    Snackbar.make(v, "No event loaded.", Snackbar.LENGTH_SHORT).show());
        } else if (event.getSelectCount() == null || event.getSelectCount() <= 0) {
            // selectCount not configured
            buttonRunDraw.setEnabled(false);
            buttonRunDraw.setOnClickListener(v ->
                    Snackbar.make(v, "Set a valid number of entrants to select before running the draw.", Snackbar.LENGTH_LONG).show());
        } else {
            buttonRunDraw.setOnClickListener(v -> {
                buttonRunDraw.setEnabled(false);


                eventRepository.drawEntrants(event.getEventId())
                        .addOnSuccessListener(unused ->
                                Snackbar.make(v,
                                        "Draw complete. Selected entrants have been recorded.",
                                        Snackbar.LENGTH_LONG).show())
                        .addOnFailureListener(err -> {
                            String msg = (err != null && err.getMessage() != null)
                                    ? err.getMessage()
                                    : "Draw failed.";
                            Snackbar.make(v, msg, Snackbar.LENGTH_LONG).show();
                            buttonRunDraw.setEnabled(true);
                        });
            });
        }


        return view;
    }
}

