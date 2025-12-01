package com.static1.fishylottery.view.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter used on the admin screen to display a list of all events.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Bind {@link Event} data (title, date, time, location) to the admin event item layout.</li>
 *     <li>Optionally show a delete button that allows admins to remove events.</li>
 * </ul>
 */
public class AdminAllEventsAdapter extends RecyclerView.Adapter<AdminAllEventsAdapter.ViewHolder> {

    private final List<Event> eventList;
    private OnDeleteClickListener onDeleteClickListener;
    private final boolean showDeleteButton;

    /**
     * Creates a new adapter for displaying events in the admin view.
     *
     * @param eventList        the list of events to display.
     * @param showDeleteButton whether the delete button should be visible for each item.
     */
    public AdminAllEventsAdapter(List<Event> eventList, boolean showDeleteButton) {
        this.eventList = eventList;
        this.showDeleteButton = showDeleteButton;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event, onDeleteClickListener, showDeleteButton);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Sets the listener that will be notified when the delete button is clicked for an event.
     *
     * @param listener callback invoked when an event delete button is pressed.
     */
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    /**
     * Listener interface for handling delete click events on admin event items.
     */
    public interface OnDeleteClickListener {
        /**
         * Called when the delete button is clicked for a specific event.
         *
         * @param event the event that is requested to be deleted.
         */
        void onDeleteClick(Event event);
    }

    /**
     * ViewHolder representing a single admin event item row in the RecyclerView.
     * <p>
     * It displays the event image placeholder, title, date, time, location,
     * and optionally a delete button.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView eventImage;
        TextView eventTitle, eventDate, eventTime, eventLocation;
        ImageButton buttonDelete;

        /**
         * Creates a new ViewHolder for an admin event item.
         *
         * @param itemView the root view of the item layout.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTime = itemView.findViewById(R.id.eventTime);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            buttonDelete = itemView.findViewById(R.id.button_event_delete);
        }

        /**
         * Binds the given {@link Event} data to the item views and configures
         * the delete button visibility and click behaviour.
         *
         * @param event             the event whose data should be displayed.
         * @param listener          callback to invoke when the delete button is clicked, may be {@code null}.
         * @param showDeleteButton  whether the delete button should be visible for this item.
         */
        void bind(Event event, OnDeleteClickListener listener, boolean showDeleteButton) {
            eventTitle.setText(event.getTitle());

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            eventDate.setText(dateFormat.format(event.getEventStartDate()));

            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            eventTime.setText(timeFormat.format(event.getEventStartDate()));

            eventLocation.setText(event.getLocation());

            if (showDeleteButton) {
                buttonDelete.setVisibility(View.VISIBLE);
                buttonDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClick(event);
                    }
                });
            } else {
                buttonDelete.setVisibility(View.GONE);
            }
        }
    }
}
