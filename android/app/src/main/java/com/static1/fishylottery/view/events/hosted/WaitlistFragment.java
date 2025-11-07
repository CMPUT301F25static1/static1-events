package com.static1.fishylottery.view.events.hosted;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.WaitlistRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WaitlistFragment extends Fragment {
    private List<WaitlistEntry> waitlist;
    private WaitlistRepository waitlistRepo;
    private Event event;
    private WaitlistEntryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        View view = inflater.inflate(R.layout.fragment_waitlist, container, false);

        RecyclerView recyclerWaitingList = view.findViewById(R.id.recycler_waitlist);
        recyclerWaitingList.setLayoutManager(new LinearLayoutManager(requireContext()));
        waitlistRepo = new WaitlistRepository();

        // Create a new waitlist
        waitlist = new ArrayList<>();

        adapter = new WaitlistEntryAdapter(waitlist, l -> {
            Log.d("Waitlist", "Clicked: " + l.getProfile().getFullName());
        });

        recyclerWaitingList.setAdapter(adapter);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.filter_toggle_group);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            String filter;
            if (checkedId == R.id.button_waiting_entrants) filter = "waiting";
            else if (checkedId == R.id.button_invited_entrants) filter = "invited";
            else filter = "all";

            filterWaitlist(filter);
        });
        toggleGroup.check(R.id.button_all_entrants);

        // Fetch the waitlist
        fetchWaitlist(event);

        return view;
    }

    private void fetchWaitlist(Event event) {
        if (event == null) return;

        waitlistRepo.getWaitlist(event)
                .addOnSuccessListener(fetchedWaitllist -> {
                    waitlist.addAll(fetchedWaitllist);
                    filterWaitlist("all");
                })
                .addOnFailureListener(e -> {
                    Log.e("Waitlist", "Failed to get waitlist for event", e);
                });
    }

    private void filterWaitlist(String filter) {
        if (filter.equals("all")) {
            adapter.updateData(waitlist);
        } else {
            List<WaitlistEntry> filteredList = waitlist
                    .stream()
                    .filter(i -> i.getStatus().equals(filter))
                    .collect(Collectors.toList());

            adapter.updateData(filteredList);
        }
    }
}
