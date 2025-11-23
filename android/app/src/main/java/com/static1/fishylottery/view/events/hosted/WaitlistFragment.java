package com.static1.fishylottery.view.events.hosted;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.viewmodel.HostedEventDetailsViewModel;

import java.util.List;
import java.util.stream.Collectors;

public class WaitlistFragment extends Fragment {
    private WaitlistEntryAdapter adapter;
    private List<WaitlistEntry> localWaitlist;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waitlist, container, false);

        HostedEventDetailsViewModel viewModel = new ViewModelProvider(requireActivity()).get(HostedEventDetailsViewModel.class);

        RecyclerView recyclerWaitingList = view.findViewById(R.id.recycler_waitlist);
        recyclerWaitingList.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new WaitlistEntryAdapter(localWaitlist, new WaitlistEntryAdapter.OnItemClickListener() {
            @Override
            public void onDeleteEntrant(WaitlistEntry entry) {
                viewModel.deleteEntrant(entry);
            }

            @Override
            public void onAcceptEntrant(WaitlistEntry entry) {
                viewModel.updateEntrantStatus(entry, "accepted");
            }

            @Override
            public void onInviteEntrant(WaitlistEntry entry) {
                viewModel.updateEntrantStatus(entry, "invited");
            }

            @Override
            public void onCancelEntrant(WaitlistEntry entry) {
                viewModel.updateEntrantStatus(entry, "cancelled");
            }
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

        viewModel.getWaitlist().observe(getViewLifecycleOwner(), waitlist -> {
            localWaitlist = waitlist;
            adapter.updateData(localWaitlist);
        });

        return view;
    }

    private void filterWaitlist(String filter) {
        if (filter.equals("all")) {
            adapter.updateData(localWaitlist);
        } else {
            List<WaitlistEntry> filteredList = localWaitlist
                    .stream()
                    .filter(i -> i.getStatus().equals(filter))
                    .collect(Collectors.toList());

            adapter.updateData(filteredList);
        }
    }
}
