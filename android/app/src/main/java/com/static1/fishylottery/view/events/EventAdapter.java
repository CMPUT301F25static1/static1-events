package com.static1.fishylottery.view.events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying a list of events in a RecyclerView.
 * Supports event click and delete actions, with optional delete button visibility.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    /** List of events to display. */
    private final List<Event> items = new ArrayList<>();

    /** LayoutInflater for creating view holders. */
    private final LayoutInflater inflater;

    /** Listener for event click events. */
    private OnEventClickListener onEventClickListener;

    /** Listener for delete button click events. */
    private OnDeleteClickListener onDeleteClickListener;

    /** Flag to show or hide the delete button. */
    private final boolean showDeleteButton;

    /** Date format for displaying event dates. */
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());

    /** Time format for displaying event times. */
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.getDefault());

    /** Context for accessing resources. */
    private Context context;

    /**
     * Constructor that initializes the adapter with a context and default delete button visibility.
     *
     * @param context the context for accessing resources
     */
    public EventAdapter(Context context) {
        this(context, false);
    }

    /**
     * Constructor that initializes the adapter with a context and delete button visibility.
     *
     * @param context         the context for accessing resources
     * @param showDeleteButton whether to show the delete button for each event
     */
    public EventAdapter(Context context, boolean showDeleteButton) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.showDeleteButton = showDeleteButton;
    }

    /**
     * Interface for handling event click events.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event is clicked.
         *
         * @param event the clicked event
         */
        void onEventClick(Event event);
    }

    /**
     * Interface for handling delete button click events.
     */
    public interface OnDeleteClickListener {
        /**
         * Called when the delete button for an event is clicked.
         *
         * @param event the event to delete
         */
        void onDeleteClick(Event event);
    }

    /**
     * Sets the listener for event click events.
     *
     * @param l the listener to set
     */
    public void setOnEventClickListener(OnEventClickListener l) {
        this.onEventClickListener = l;
    }

    /**
     * Sets the listener for delete button click events.
     *
     * @param l the listener to set
     */
    public void setOnDeleteClickListener(OnDeleteClickListener l) {
        this.onDeleteClickListener = l;
    }

    /**
     * Updates the adapter with a new list of events.
     *
     * @param newItems the new list of events to display
     */
    public void submitList(List<Event> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Retrieves the event at the specified position.
     *
     * @param position the position of the event
     * @return the event at the specified position
     */
    public Event getItem(int position) {
        return items.get(position);
    }

    /**
     * Creates a new ViewHolder for an event item.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type
     * @return a new ViewHolder
     */
    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.content_event, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the event data to the ViewHolder at the specified position.
     *
     * @param holder   the ViewHolder to bind
     * @param position the position of the event
     */
    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
        Event ev = items.get(position);
        holder.bind(ev);
    }

    /**
     * Returns the total number of events in the adapter.
     *
     * @return the number of events
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder for displaying an event item in the RecyclerView.
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        /** ImageView for the event image. */
        ImageView ivImage;

        /** TextView for the event date. */
        TextView tvDate;

        /** TextView for the event title. */
        TextView tvTitle;

        /** TextView for the event time. */
        TextView tvTime;

        /** TextView for the event location. */
        TextView tvLocation;

        /** ImageButton for deleting the event. */
        ImageButton buttonDelete;

        /**
         * Constructs a ViewHolder for an event item.
         *
         * @param itemView the view for the event item
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.eventImage);
            tvDate = itemView.findViewById(R.id.eventDate);
            tvTitle = itemView.findViewById(R.id.eventTitle);
            tvTime = itemView.findViewById(R.id.eventTime);
            tvLocation = itemView.findViewById(R.id.eventLocation);
            buttonDelete = itemView.findViewById(R.id.button_event_delete);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && onEventClickListener != null) {
                    onEventClickListener.onEventClick(items.get(pos));
                }
            });

            buttonDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(items.get(pos));
                }
            });
        }

        /**
         * Binds the event data to the ViewHolder's views.
         *
         * @param ev the event to bind
         */
        void bind(Event ev) {
            buttonDelete.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);

            // Title
            tvTitle.setText(ev != null && ev.getTitle() != null ? ev.getTitle() : "Untitled event");

            // Date & Time
            Date start = ev != null ? ev.getEventStartDate() : null;
            Date end = ev != null ? ev.getEventEndDate() : null;

            if (start != null) {
                tvDate.setText(formatDateRange(start, end));
                tvTime.setText(formatTimeRange(start, end));
            } else {
                tvDate.setText("Date not set");
                tvTime.setText("Time not set");
            }

            // Location
            tvLocation.setText(ev != null && ev.getLocation() != null ? ev.getLocation() : "Location not set");

            // Image: try to load with Glide; fallback to placeholder
            String imageUrl = ev != null ? ev.getImageUrl() : null;
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    // Glide recommended. Ensure dependency added (see note below).
                    com.bumptech.glide.Glide.with(ivImage.getContext())
                            .load(imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.circle_background_light_blue) // create a placeholder drawable
                            .into(ivImage);
                } catch (NoClassDefFoundError e) {
                    // Glide not present - ignore and set placeholder
                    ivImage.setImageResource(R.drawable.circle_background_light_blue);
                }
            } else {
                ivImage.setImageResource(R.drawable.circle_background_light_blue);
            }
        }

        /**
         * Formats the date range for the event.
         *
         * @param start the event start date
         * @param end   the event end date
         * @return the formatted date range string
         */
        private String formatDateRange(Date start, Date end) {
            if (start == null) return "Date not set";
            if (end == null) return dateFmt.format(start);
            // if same day, show single day
            boolean sameDay = start.getYear() == end.getYear() &&
                    start.getMonth() == end.getMonth() &&
                    start.getDate() == end.getDate();
            return sameDay ? dateFmt.format(start) : dateFmt.format(start) + " - " + dateFmt.format(end);
        }

        /**
         * Formats the time range for the event.
         *
         * @param start the event start date
         * @param end   the event end date
         * @return the formatted time range string
         */
        private String formatTimeRange(Date start, Date end) {
            if (start == null) return "Time not set";
            if (end == null) return timeFmt.format(start);
            boolean sameDay = start.getYear() == end.getYear() &&
                    start.getMonth() == end.getMonth() &&
                    start.getDate() == end.getDate();
            return sameDay ? timeFmt.format(start) : timeFmt.format(start) + " - " + timeFmt.format(end);
        }
    }
}