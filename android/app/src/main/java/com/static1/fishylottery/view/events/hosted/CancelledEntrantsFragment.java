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
/**
 * Fragment that displays the list of cancelled entrants for a hosted event.
 *
 * <p>It loads cancelled entrant IDs from the {@link IEventRepository} and shows
 * them in a simple {@link RecyclerView}. If no event or no cancelled entrants
 * are available, a message is shown instead.</p>
 */
public class CancelledEntrantsFragment extends Fragment {

    private Event event;
    private final IEventRepository eventRepository;
    /**
     * Default constructor used by the production app.
     *
     * <p>Creates a fragment that uses a real {@link EventRepository} to fetch
     * cancelled entrant IDs.</p>
     */
    // Default constructor used by the real app
    public CancelledEntrantsFragment() {
        this(new EventRepository());
    }
    /**
     * Constructor used primarily for testing, allowing injection of a fake repository.
     *
     * <p>This makes it possible to provide a mock or stub {@link IEventRepository}
     * when unit testing the fragment behavior.</p>
     */
    // Testing constructor so we can inject a fake repo
    public CancelledEntrantsFragment(IEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    private RecyclerView recycler;
    private View emptyView;
    private IdAdapter adapter;
    /**
     * Inflates the cancelled entrants layout, initializes the recycler view, and
     * triggers loading of cancelled entrant IDs for the provided event.
     *
     * <p>If no event is passed in the fragment arguments, an error snackbar is shown.
     * Otherwise, the repository is queried and the list is updated when results arrive.</p>
     */
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

    static class IdAdapter extends RecyclerView.Adapter<IdAdapter.VH> {
        private final List<String> items = new ArrayList<>();
        /**
         * Replaces the current list of cancelled entrant IDs and refreshes the view.
         *
         * <p>Existing items are cleared, the new list is copied in, and the adapter
         * notifies that the data set has changed.</p>
         */
        public void setItems(List<String> list) {
            items.clear();
            if (list != null) items.addAll(list);
            notifyDataSetChanged();
        }
        /**
         * Creates a new {@link VH} using the built-in simple list item layout.
         *
         * <p>This is called by the RecyclerView when it needs a new row view
         * to display a cancelled entrant ID.</p>
         */
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new VH(row);
        }
        /**
         * Binds a cancelled entrant ID string to the given view holder.
         *
         * <p>The ID is displayed as the text of the row.</p>
         */
        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.text.setText(items.get(position));
        }
        /**
         * Returns the number of cancelled entrant IDs currently in the list.
         */
        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView text;
            /**
             * Creates a new view holder that wraps the provided row view.
             *
             * <p>The text view for displaying the ID is looked up from the
             * standard simple list item layout.</p>
             */
            VH(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}