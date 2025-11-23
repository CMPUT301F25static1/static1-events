


package com.static1.fishylottery.view.events.hosted;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;


import com.bumptech.glide.Glide;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.services.DateUtils;
import com.static1.fishylottery.viewmodel.HostedEventDetailsViewModel;

public class HostedEventDetailsFragment extends Fragment {
    private Event event;
    private HostedEventDetailsViewModel viewModel;
    private final ActivityResultLauncher<Intent> createFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        viewModel.
                                exportCsv(requireContext(), uri);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(HostedEventDetailsViewModel.class);

        if (getArguments() != null) {
            Object arg = getArguments().getSerializable("event");
            if (arg instanceof Event) {
                event = (Event) arg;
                viewModel.setEvent(event);
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
        buttonViewWaitlist.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_hostedEventDetails_to_hostedEventDetailsWaitlist));

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


        buttonViewMap.setOnClickListener(v -> Navigation.findNavController(v)
                        .navigate(R.id.action_hostedEventDetails_to_signupMap)
        );


        buttonExportEnrolled.setOnClickListener(v -> startCsvExport());

        buttonViewCancelledEntrants.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putSerializable("event", event);
            Navigation.findNavController(v)
                    .navigate(R.id.action_hostedEventDetails_to_cancelledEntrants, b);
        });
        buttonRunLottery.setOnClickListener(v -> viewModel.runLottery());

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message.isEmpty()) return;
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            buttonRunLottery.setEnabled(!loading);
            buttonExportEnrolled.setEnabled(!loading);
        });

        viewModel.fetchWaitlist(event);
        viewModel.resetMessage();

        return view;
    }

    private void startCsvExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "accepted_entrants.csv");
        createFileLauncher.launch(intent);
    }
}
