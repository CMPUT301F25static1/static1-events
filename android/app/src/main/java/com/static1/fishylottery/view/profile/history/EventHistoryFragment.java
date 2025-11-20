package com.static1.fishylottery.view.profile.history;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.model.repositories.WaitlistRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.services.DateUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView textNoEventHistory;
    private EventHistoryAdapter adapter;
    private List<EventHistoryItem> eventHistoryItems = new ArrayList<>();
    private WaitlistRepository waitlistRepo;
    private EventRepository eventsRepo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_history, container, false);

        eventsRepo = new EventRepository();
        waitlistRepo = new WaitlistRepository();

        textNoEventHistory = view.findViewById(R.id.text_no_history);
        recyclerView = view.findViewById(R.id.recycler_event_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));;
        DividerItemDecoration divider =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);
        adapter = new EventHistoryAdapter(eventHistoryItems);

        adapter.setOnEventClickListener(event -> {
            Log.d("EventHistory", "Selected event: " + event.getTitle());
        });

        recyclerView.setAdapter(adapter);

        loadHistory(); // load

        return view;
    }

    private void loadHistory() {
        String uid = AuthManager.getInstance().getUserId();

        if (uid == null) {
            textNoEventHistory.setVisibility(View.VISIBLE);
            return;
        };

        Task<List<WaitlistEntry>> myWaitlistsTask = waitlistRepo.getEventWaitlistEntriesByUser(uid);
        Task<List<Event>> allEventsTask = eventsRepo.fetchAllEvents();

        Tasks.whenAllSuccess(myWaitlistsTask, allEventsTask)
                .addOnSuccessListener(results -> {
                    List<WaitlistEntry> myWaitlists = (List<WaitlistEntry>) results.get(0);
                    List<Event> allEvents = (List<Event>) results.get(1);

                    eventHistoryItems.clear();

                    for (Event event : allEvents) {
                        WaitlistEntry w = myWaitlists.stream()
                                .filter(x -> x.getEventId().equals(event.getEventId()))
                                .findFirst().orElse(null);

                        if (w != null) {
                            String title = event.getTitle();
                            String when =
                                    DateUtils.formatDateTime(event.getEventStartDate())
                                            + " - "
                                            + DateUtils.formatDateTime(event.getEventEndDate());
                            String where = event.getLocation();
                            String status = w.getStatus();

                            EventHistoryItem item = new EventHistoryItem(
                                    title,
                                    when,
                                    where,
                                    status
                            );

                            eventHistoryItems.add(item);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    textNoEventHistory.setVisibility(eventHistoryItems.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("MyEvents", "Unable to get my events", e);
                    Toast.makeText(requireContext(), "Unable to get my events", Toast.LENGTH_LONG).show();
                });
    }

}