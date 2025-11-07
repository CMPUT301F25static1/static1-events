package com.static1.fishylottery.view.events.hosted;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.services.DateUtils;

import java.time.format.DateTimeFormatter;

public class HostedEventDetailsFragment extends Fragment {
    private Event event;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        View view = inflater.inflate(R.layout.fragment_hosted_event_details, container, false);

        Button buttonViewWaitlist = view.findViewById(R.id.button_view_waitlist);
        Button buttonRunLottery = view.findViewById(R.id.button_run_lottery);
        Button buttonViewMap = view.findViewById(R.id.button_view_map);
        Button buttonExportEnrolled = view.findViewById(R.id.button_export_enrolled);
        Button buttonSendNotifications = view.findViewById(R.id.button_send_notifications);
        Button buttonViewQrCode = view.findViewById(R.id.button_view_qr_code);

        View eventDetailsCard = view.findViewById(R.id.event_details_info);
        TextView textEventTitle = eventDetailsCard.findViewById(R.id.eventTitle);
        TextView textEventDate = eventDetailsCard.findViewById(R.id.eventDate);
        TextView textEventTime = eventDetailsCard.findViewById(R.id.eventTime);
        TextView textEventLocation = eventDetailsCard.findViewById(R.id.eventLocation);
        ImageView imageView = eventDetailsCard.findViewById(R.id.eventImage);

        String imageUrl = event.getImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imageView);
        }

        textEventTitle.setText(event.getTitle());
        textEventLocation.setText(event.getLocation());
        textEventDate.setText(DateUtils.formatDateRange(event.getEventStartDate(), event.getEventEndDate()));
        textEventTime.setText(DateUtils.formatTimeRange(event.getEventStartDate(), event.getEventEndDate()));

        buttonViewWaitlist.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            Navigation.findNavController(view).navigate(R.id.action_hostedEventDetails_to_hostedEventDetailsWaitlist, bundle);
        });

        buttonSendNotifications.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_hostedEventsDetails_to_hostedEventDetailsSendNotifications);
        });

        buttonRunLottery.setOnClickListener(v -> {
            // TODO: Run the lottery function using some controller!
        });

        buttonExportEnrolled.setOnClickListener(v -> {
            // TODO: Export the CSV of enrolled
        });

        buttonViewQrCode.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            Navigation.findNavController(view).navigate(R.id.action_hostedEventDetails_to_viewQrCode, bundle);
        });

        buttonViewMap.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_hostedEventDetails_to_signupMap);
        });

        return view;
    }
}
