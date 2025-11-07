package com.static1.fishylottery.view.events.hosted;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        if (getArguments() != null) {
            Object arg = getArguments().getSerializable("event");
            if (arg instanceof Event) {
                event = (Event) arg;
            }
        }


        View view = inflater.inflate(R.layout.fragment_hosted_event_details, container, false);


        Button buttonViewWaitlist     = view.findViewById(R.id.button_view_waitlist);
        Button buttonRunDraw          = view.findViewById(R.id.button_run_draw);
        Button buttonViewMap          = view.findViewById(R.id.button_view_map);
        Button buttonExportEnrolled   = view.findViewById(R.id.button_export_enrolled);
        Button buttonSendNotifications= view.findViewById(R.id.button_send_notifications);
        Button buttonViewQrCode       = view.findViewById(R.id.button_view_qr_code);


        // Nav hooks
        buttonViewWaitlist.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_hostedEventDetails_to_hostedEventDetailsWaitlist));


        buttonSendNotifications.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_hostedEventsDetails_to_hostedEventDetailsSendNotifications));


        buttonViewQrCode.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putSerializable("event", event);
            Navigation.findNavController(view)
                    .navigate(R.id.action_hostedEventDetails_to_viewQrCode, b);
        });


        buttonViewMap.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_hostedEventDetails_to_signupMap));


        buttonExportEnrolled.setOnClickListener(v ->
                Snackbar.make(view, "Export not implemented yet.", Snackbar.LENGTH_LONG).show());


        // --- Run Draw button logic ---
        if (event == null || event.getEventId() == null) {
            buttonRunDraw.setEnabled(false);
            buttonRunDraw.setOnClickListener(v ->
                    Snackbar.make(view, "No event loaded.", Snackbar.LENGTH_LONG).show());
        } else {
            Integer n = event.getSelectCount();
            if (n == null || n <= 0) {
                buttonRunDraw.setEnabled(false);
                buttonRunDraw.setOnClickListener(v ->
                        Snackbar.make(view, "Set a valid number of entrants to select.", Snackbar.LENGTH_LONG).show());
            } else {
                buttonRunDraw.setOnClickListener(v -> {
                    buttonRunDraw.setEnabled(false);


                    // NOTE: repository expects only eventId
                    eventRepository.drawEntrants(event.getEventId())
                            .addOnSuccessListener(unused -> {
                                Snackbar.make(view, "Draw complete. Selected entrants recorded.", Snackbar.LENGTH_LONG).show();
                                buttonRunDraw.setEnabled(true);
                            })
                            .addOnFailureListener(err -> {
                                String msg = (err.getMessage() != null) ? err.getMessage() : "Draw failed.";
                                Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
                                buttonRunDraw.setEnabled(true);
                            });
                });
            }
        }


        return view;
    }
}


