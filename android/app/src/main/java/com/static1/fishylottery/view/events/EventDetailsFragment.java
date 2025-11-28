package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.util.Log;
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
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.viewmodel.EventDetailsViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Shows one event and lets an entrant join the waitlist. */
public class EventDetailsFragment extends Fragment {

    public static final String ARG_EVENT = "event";

    private final SimpleDateFormat df =
            new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    private TextView tvTitle,
            tvDesc,
            tvWhere,
            tvWhen,
            tvHostedBy,
            tvMaxAttendees,
            tvMaxWaitlist,
            tvWaitlistCount;
    private ImageView ivEventPoster;
    private Button buttonJoinWaitlist,
            buttonLeaveWaitlist,
            buttonAcceptInvite,
            buttonDeclineInvite;
    private EventDetailsViewModel viewModel;

    public EventDetailsFragment() {}
    public EventDetailsFragment(EventDetailsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_details, container, false);

        if (viewModel == null) {
            viewModel = new ViewModelProvider(this).get(EventDetailsViewModel.class);
        }

        ivEventPoster = v.findViewById(R.id.image_event_poster);
        tvTitle = v.findViewById(R.id.text_title);
        tvDesc  = v.findViewById(R.id.text_desc);
        tvWhere = v.findViewById(R.id.text_where);
        tvWhen  = v.findViewById(R.id.text_when);
        tvHostedBy = v.findViewById(R.id.text_hosted_by);
        tvMaxAttendees = v.findViewById(R.id.text_max_attendees);
        tvMaxWaitlist = v.findViewById(R.id.text_max_waitlist);
        tvWaitlistCount = v.findViewById(R.id.text_waitlist_count);

        buttonJoinWaitlist = v.findViewById(R.id.button_join_waitlist);
        buttonLeaveWaitlist = v.findViewById(R.id.button_leave_waitlist);
        buttonAcceptInvite = v.findViewById(R.id.button_accept_invite);
        buttonDeclineInvite = v.findViewById(R.id.button_decline_invite);

        // Receive event from args
        Bundle args = getArguments();
        if (args != null) {
            Object obj = args.getSerializable(ARG_EVENT);
            if (obj instanceof Event) {
                Event event = (Event) obj;
                viewModel.setEvent(event);
                viewModel.loadWaitlistEntry(event);
            }
        }

        buttonJoinWaitlist.setOnClickListener(l -> {
            viewModel.joinWaitlist(requireContext());
        });

        buttonLeaveWaitlist.setOnClickListener(l -> {
            viewModel.leaveWaitlist();
        });

        buttonAcceptInvite.setOnClickListener(l -> {
            viewModel.acceptInvite();
        });

        buttonDeclineInvite.setOnClickListener(l -> {
            viewModel.declineInvite();
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            buttonJoinWaitlist.setEnabled(!loading);
            buttonLeaveWaitlist.setEnabled(!loading);
            buttonAcceptInvite.setEnabled(!loading);
            buttonDeclineInvite.setEnabled(!loading);
        });

        viewModel.getWaitlistEntry().observe(getViewLifecycleOwner(), entry -> {
            String status = "no_waitlist";

            if (entry != null) {
                status = entry.getStatus();
            }

            updateButtons(status);
        });

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                buttonJoinWaitlist.setEnabled(false);
                return;
            }

            tvTitle.setText(nullTo(event.getTitle(), "(untitled)"));
            tvDesc.setText(nullTo(event.getDescription(), "—"));
            tvWhere.setText(nullTo(event.getLocation(), "—"));
            tvWhen.setText(formatWindow(
                    event.getEventStartDate(),
                    event.getEventEndDate(),
                    event.getRegistrationCloses()));
            tvHostedBy.setText(nullTo(event.getHostedBy(), ""));
            tvMaxAttendees.setText("Max Attendees: " + nullTo(event.getCapacity(), "None"));
            tvMaxWaitlist.setText("Max Waitlist: " + nullTo(event.getMaxWaitlistSize(), "None"));
            tvWaitlistCount.setText("Entrants on Waitlist: " + nullTo(event.countEntries(), "0"));


            String imageUrl = event.getImageUrl();

            Log.d("EventDetails", "The image URL is: " + imageUrl);

            if (imageUrl != null) {
                Glide.with(this).load(imageUrl).into(ivEventPoster);
                ivEventPoster.setVisibility(View.VISIBLE);
            } else {
                ivEventPoster.setVisibility(View.GONE);
            }

            // Enable only if registration deadline is in the future (or not set)
            Date deadline = event.getRegistrationCloses();
            boolean canJoin = (deadline == null) || deadline.after(new Date());
            buttonJoinWaitlist.setEnabled(canJoin);
            if (!canJoin) buttonJoinWaitlist.setText("Registration closed");
        });

        setupListeners();

        return v;
    }

    private String formatWindow(Date start, Date end, Date regClose) {
        String s = (start == null) ? "—" : df.format(start);
        String e = (end   == null) ? "—" : df.format(end);
        String r = (regClose == null) ? "—" : df.format(regClose);
        return "Start: " + s + "\nEnd: " + e + "\nRegistration closes: " + r;
    }

    private static <T> T nullTo(T s, T fallback) {
        return s == null ? fallback : s;
    }

    private void setupListeners() {
        buttonJoinWaitlist.setOnClickListener(v -> viewModel.joinWaitlist(requireContext()));
        buttonLeaveWaitlist.setOnClickListener(v -> viewModel.leaveWaitlist());
        buttonAcceptInvite.setOnClickListener(v -> viewModel.acceptInvite());
        buttonDeclineInvite.setOnClickListener(v -> viewModel.declineInvite());
    }

    private void updateButtons(String status) {
        buttonJoinWaitlist.setVisibility(View.GONE);
        buttonDeclineInvite.setVisibility(View.GONE);
        buttonLeaveWaitlist.setVisibility(View.GONE);
        buttonAcceptInvite.setVisibility(View.GONE);

        switch (status) {
            case "no_waitlist":
                buttonJoinWaitlist.setVisibility(View.VISIBLE);
                break;
            case "waiting":
                buttonLeaveWaitlist.setVisibility(View.VISIBLE);
                break;
            case "invited":
                buttonAcceptInvite.setVisibility(View.VISIBLE);
                buttonDeclineInvite.setVisibility(View.VISIBLE);
            default:
                break;
        }
    }
}

