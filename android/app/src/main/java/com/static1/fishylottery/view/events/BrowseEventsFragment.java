package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;


import java.util.ArrayList;
import java.util.List;

/** Shows all public events. Tapping a row opens EventDetailsFragment. */
public class BrowseEventsFragment extends Fragment {

    private final EventRepository repo = new EventRepository();

    private final EventsAdapter adapter = new EventsAdapter(new ArrayList<>(), event -> {
        Bundle b = new Bundle();
        b.putSerializable(
                com.static1.fishylottery.view.events.EventDetailsFragment.ARG_EVENT,
                event
        );
        Navigation.findNavController(requireView())
                .navigate(R.id.navigation_event_details, b);
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_events, container, false);

        RecyclerView rv = view.findViewById(R.id.recycler_browse_events);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        repo.getAll()
                .addOnSuccessListener(qs -> {
                    List<Event> items = new ArrayList<>();
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        Event e = d.toObject(Event.class);
                        if (e != null) {
                            // keep firestore doc id around, helpful for navigation/updates
                            e.setEventId(d.getId());
                            items.add(e);
                        }
                    }
                    adapter.submit(items);
                })
                .addOnFailureListener(err ->
                        Snackbar.make(view, "Failed to load events: " + err.getMessage(),
                                Snackbar.LENGTH_LONG).show());

        return view;
    }

    // --- RecyclerView adapter  ---
    private static class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {
        interface OnClick { void onItem(Event e); }

        private final List<Event> data;
        private final OnClick onClick;

        EventsAdapter(List<Event> data, OnClick onClick) {
            this.data = data;
            this.onClick = onClick;
        }

        void submit(List<Event> items) {
            data.clear();
            data.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_selectable_row, parent, false);
            return new VH(row);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Event e = data.get(pos);
            h.title.setText(e.getTitle() != null ? e.getTitle() : "(untitled)");
            h.itemView.setOnClickListener(v -> onClick.onItem(e));
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView title;
            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}


