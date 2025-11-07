package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.WaitlistRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Shows one event and lets an entrant join the waitlist. */
public class EventDetailsFragment extends Fragment {

    public static final String ARG_EVENT = "event";

    private final SimpleDateFormat df =
            new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    private TextView tvTitle, tvDesc, tvWhere, tvWhen;
    private ImageView ivEventPoster;
    private Button btnJoin;

    private final WaitlistRepository waitlists = new WaitlistRepository();
    private Event event;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_details, container, false);

        ivEventPoster = v.findViewById(R.id.image_event_poster);
        tvTitle = v.findViewById(R.id.text_title);
        tvDesc  = v.findViewById(R.id.text_desc);
        tvWhere = v.findViewById(R.id.text_where);
        tvWhen  = v.findViewById(R.id.text_when);
        btnJoin = v.findViewById(R.id.button_join_waitlist);

        // Receive event from args
        Bundle args = getArguments();
        if (args != null) {
            Object obj = args.getSerializable(ARG_EVENT);
            if (obj instanceof Event) {
                event = (Event) obj;
            }
        }

        bindUi();
        btnJoin.setOnClickListener(this::joinWaitlist);

        return v;
    }

    private void bindUi() {
        if (event == null) {
            btnJoin.setEnabled(false);
            return;
        }
        tvTitle.setText(nullTo(event.getTitle(), "(untitled)"));
        tvDesc.setText(nullTo(event.getDescription(), "—"));
        tvWhere.setText(nullTo(event.getLocation(), "—"));
        tvWhen.setText(formatWindow(
                event.getEventStartDate(),
                event.getEventEndDate(),
                event.getRegistrationCloses()));

        String imageUrl = event.getImageUrl();

        Log.d("EventDetails", "The image URL is: " + imageUrl);

        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(ivEventPoster);
        }

        // Enable only if registration deadline is in the future (or not set)
        Date deadline = event.getRegistrationCloses();
        boolean canJoin = (deadline == null) || deadline.after(new Date());
        btnJoin.setEnabled(canJoin);
        if (!canJoin) btnJoin.setText("Registration closed");
    }

    private void joinWaitlist(View anchor) {
        if (event == null || event.getEventId() == null) {
            Snackbar.make(anchor, "Missing event id.", Snackbar.LENGTH_LONG).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid == null) {
            Snackbar.make(anchor, "Please sign in first.", Snackbar.LENGTH_LONG).show();
            return;
        }

        WaitlistEntry entry = new WaitlistEntry();
        Profile profile = new Profile(); // TODO: We need to get the full profile with the name and stuff
        profile.setUid(uid);

        entry.setJoinedAt(new Date());
        entry.setProfile(profile);
        entry.setStatus("waiting");

        btnJoin.setEnabled(false);
        waitlists.joinWaitlist(event, entry)
                .addOnSuccessListener(unused ->
                        Snackbar.make(anchor, "Joined waitlist!", Snackbar.LENGTH_LONG).show())
                .addOnFailureListener(e -> {
                    btnJoin.setEnabled(true);
                    Snackbar.make(anchor, "Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private String formatWindow(Date start, Date end, Date regClose) {
        String s = (start == null) ? "—" : df.format(start);
        String e = (end   == null) ? "—" : df.format(end);
        String r = (regClose == null) ? "—" : df.format(regClose);
        return "Start: " + s + "\nEnd: " + e + "\nRegistration closes: " + r;
    }

    private static String nullTo(String s, String fallback) {
        return s == null ? fallback : s;
    }
}

