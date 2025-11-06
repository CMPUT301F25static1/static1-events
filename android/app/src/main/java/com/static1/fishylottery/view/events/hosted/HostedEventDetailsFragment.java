package com.static1.fishylottery.view.events.hosted;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;

public class HostedEventDetailsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_hosted_event_details, container, false);

        Button buttonViewWaitlist = view.findViewById(R.id.button_view_waitlist);
        Button buttonRunLottery = view.findViewById(R.id.button_run_lottery);
        Button buttonViewMap = view.findViewById(R.id.button_view_map);
        Button buttonExportEnrolled = view.findViewById(R.id.button_export_enrolled);
        Button buttonSendNotifications = view.findViewById(R.id.button_send_notifications);

        buttonViewWaitlist.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_hostedEventDetails_to_hostedEventDetailsWaitlist);
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

        buttonViewMap.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_hostedEventDetails_to_signupMap);
        });

        return view;
    }
}
