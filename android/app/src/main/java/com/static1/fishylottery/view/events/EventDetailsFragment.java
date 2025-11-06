package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.EventDetailsViewModel;
import com.static1.fishylottery.model.entities.Event;

import java.text.SimpleDateFormat;
import java.util.Locale;
public class EventDetailsFragment extends Fragment {
    public static final String ARG_EVENT = "arg_event";

    private EventDetailsViewModel vm;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_details, container, false);

        vm = new ViewModelProvider(this).get(EventDetailsViewModel.class);

        // Receive the Event passed via Bundle
        Bundle args = getArguments();
        if (args != null) {
            Object obj = args.getSerializable(ARG_EVENT);
            if (obj instanceof Event) vm.setEvent((Event) obj);
        }

        TextView tvTitle = v.findViewById(R.id.text_title);
        TextView tvDesc  = v.findViewById(R.id.text_desc);
        TextView tvWhere = v.findViewById(R.id.text_where);
        TextView tvWhen  = v.findViewById(R.id.text_when);
        Button   btnJoin = v.findViewById(R.id.button_join_waitlist);

        SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

        vm.getEvent().observe(getViewLifecycleOwner(), e -> {
            if (e == null) return;
            tvTitle.setText(e.getTitle());
            tvDesc.setText(e.getDescription());
            tvWhere.setText(e.getLocation());
            String window = "";
            if (e.getEventStartDate() != null) window += df.format(e.getEventStartDate());
            if (e.getEventEndDate() != null)   window += " – " + df.format(e.getEventEndDate());
            tvWhen.setText(window);

            btnJoin.setEnabled(vm.canJoinNow());
        });

        vm.getError().observe(getViewLifecycleOwner(),
                msg -> { if (msg != null) Snackbar.make(v, msg, Snackbar.LENGTH_LONG).show(); });

        btnJoin.setOnClickListener(click -> {
            // TODO replace with your real profile id; for now we use a stable placeholder
            String profileId = "demo-profile";  // e.g., FirebaseAuth.getInstance().getUid()
            var task = vm.joinWaitlist(profileId);
            if (task != null) {
                task.addOnSuccessListener(unused ->
                        Snackbar.make(v, "Joined waitlist!", Snackbar.LENGTH_LONG).show()
                ).addOnFailureListener(err ->
                        Snackbar.make(v, "Failed: " + err.getMessage(), Snackbar.LENGTH_LONG).show()
                );
            }
        });

        return v;
    }
}

