package com.static1.fishylottery.view.events.hosted;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.model.repositories.IEventRepository;

import java.util.ArrayList;
import java.util.List;

public class CancelledEntrantsFragment extends Fragment {

    private Event event;
    private final IEventRepository eventRepository;

    // Default constructor used by the real app
    public CancelledEntrantsFragment() {
        this(new EventRepository());
    }

    // Testing constructor so we can inject a fake repo
    public CancelledEntrantsFragment(IEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    private RecyclerView recycler;
    private View emptyView;
    private IdAdapter adapter;

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

        adapter = new IdAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        if (event == null) {
            Snackbar.make(v, "No event provided.", Snackbar.LENGTH_LONG).show();
            return v;
        }

        eventRepository.fetchCancelledEntrantIds(event.getEventId())
                .addOnSuccessListener(ids -> {
                    adapter.setItems(ids != null ? ids : new ArrayList<>());
                    emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
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

    private static class IdAdapter extends RecyclerView.Adapter<IdAdapter.VH> {
        private final List<String> items = new ArrayList<>();

        void setItems(List<String> list) {
            items.clear();
            if (list != null) items.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new VH(row);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.text.setText(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView text;
            VH(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}