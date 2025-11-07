package com.static1.fishylottery.view.events.hosted;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.WaitlistRepository;
import com.static1.fishylottery.view.events.EntrantAdapter;

import java.util.ArrayList;
import java.util.List;

public class HostedEventDetailsWaitlistFragment extends Fragment {
    private RecyclerView recyclerWaitingList;
    private List<WaitlistEntry> waitlist;
    private WaitlistRepository waitlistRepo;
    private Event event;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        View view = inflater.inflate(R.layout.fragment_hosted_event_details_waitlist, container, false);

        recyclerWaitingList = view.findViewById(R.id.recycler_waiting_list);
        recyclerWaitingList.setLayoutManager(new LinearLayoutManager(requireContext()));
        waitlistRepo = new WaitlistRepository();

        waitlist = new ArrayList<>();

        WaitlistEntryAdapter adapter = new WaitlistEntryAdapter(waitlist, l -> {
            Log.d("Waitlist", "Clicked: " + l.getProfile().getFullName());
        });
        recyclerWaitingList.setAdapter(adapter);

        if (event != null) {
            waitlistRepo.getWaitlist(event).addOnSuccessListener(ww -> {
                waitlist.addAll(ww);
                Log.d("Waitlist", String.valueOf(ww.size()));

                adapter.notifyDataSetChanged();
            });
        }

        return view;
    }
}
