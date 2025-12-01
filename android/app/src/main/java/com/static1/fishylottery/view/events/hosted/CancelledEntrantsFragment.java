package com.static1.fishylottery.view.events.hosted;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.IWaitlistRepository;
import com.static1.fishylottery.model.repositories.WaitlistRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CancelledEntrantsFragment extends Fragment {

    private Event event;
    private final IWaitlistRepository waitlistRepository;

    // Default constructor used by the real app
    public CancelledEntrantsFragment() {
        this(new WaitlistRepository());
    }

    // Testing constructor so we can inject a fake repo
    public CancelledEntrantsFragment(IWaitlistRepository waitlistRepository) {
        this.waitlistRepository = waitlistRepository;
    }

    private RecyclerView recycler;
    private View emptyView;
    private WaitlistEntryAdapter adapter;
    private List<WaitlistEntry> cancelledEntrants = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cancelled_entrants, container, false);

        if (getArguments() != null) {
            Object arg = getArguments().getSerializable("event");
            if (arg instanceof Event) {
                event = (Event) arg;
            }
        }

        recycler = v.findViewById(R.id.recycler_cancelled_entrants);
        emptyView = v.findViewById(R.id.text_empty);

        adapter = new WaitlistEntryAdapter(cancelledEntrants, new WaitlistEntryAdapter.OnItemClickListener() {
            @Override
            public void onDeleteEntrant(WaitlistEntry entry) {
                waitlistRepository.deleteFromWaitlist(event, entry.getProfile().getUid());
            }

            @Override
            public void onCancelEntrant(WaitlistEntry entry) {
                entry.setStatus("cancelled");
                waitlistRepository.addToWaitlist(event, entry);
            }

            @Override
            public void onAcceptEntrant(WaitlistEntry entry) {
                entry.setStatus("accepted");
                waitlistRepository.addToWaitlist(event, entry);
            }

            @Override
            public void onInviteEntrant(WaitlistEntry entry) {
                entry.setStatus("invited");
                waitlistRepository.addToWaitlist(event, entry);
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        if (event == null) {
            Snackbar.make(v, "No event provided.", Snackbar.LENGTH_LONG).show();
            return v;
        }

        waitlistRepository.getWaitlist(event)
            .addOnSuccessListener(waitlistEntries -> {
                cancelledEntrants.addAll(
                        waitlistEntries.stream()
                                .filter(entry -> "cancelled".equals(entry.getStatus()) || "declined".equals(entry.getStatus()))
                                .collect(Collectors.toList())
                );
                adapter.notifyDataSetChanged();
                emptyView.setVisibility(cancelledEntrants.isEmpty() ? View.VISIBLE : View.GONE);
            })
            .addOnFailureListener(err ->
                Snackbar.make(
                        v,
                        (err != null && err.getMessage() != null)
                                ? err.getMessage()
                                : "Failed to load cancelled entrants.",
                        Snackbar.LENGTH_LONG
                ).show()
            );
        return v;
    }
}