package com.static1.fishylottery.view.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.AppNotification;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of {@link AppNotification} objects in a RecyclerView.
 * Handles the creation and binding of notification items to their respective views,
 * and provides click handling for individual notifications.
 *
 * <p>This adapter manages the display of notification title, message, and creation date
 * in a formatted manner using the system's default date-time format.</p>
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    /**
     * Interface definition for a callback to be invoked when a notification item is clicked.
     */
    public interface OnNotificationClick {
        /**
         * Called when a notification item has been clicked.
         *
         * @param n The {@link AppNotification} that was clicked
         */
        void onClick(AppNotification n);
    }

    private OnNotificationClick clickListener;
    private final List<AppNotification> items = new ArrayList<>();
    private final DateFormat fmt = DateFormat.getDateTimeInstance();

    /**
     * Returns the current click listener for notification items.
     *
     * @return The current {@link OnNotificationClick} listener, or null if not set
     */
    public OnNotificationClick getClickListener() {
        return clickListener;
    }

    /**
     * Sets a click listener for notification items.
     *
     * @param l The {@link OnNotificationClick} listener to be set
     */
    public void setOnNotificationClick(OnNotificationClick l) {
        this.clickListener = l;
    }

    /**
     * Replaces the current list of notifications with the provided list and refreshes the UI.
     * If the provided list is null, the adapter will be cleared.
     *
     * @param newItems The new list of {@link AppNotification} objects to display,
     *                 or null to clear the adapter
     */
    public void submit(List<AppNotification> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new {@link VH} of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the {@link VH#itemView} to reflect the item at the given position.
     *
     * @param h The ViewHolder which should be updated to represent the contents of the item at the given position
     * @param pos The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AppNotification n = items.get(pos);

        h.title.setText(n.getTitle());
        h.message.setText(n.getMessage());
        h.date.setText(n.getCreatedAt() != null ? fmt.format(n.getCreatedAt()) : "");

        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(n);
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class that describes the notification item view and metadata about its place within the RecyclerView.
     * Contains references to the title, message, and date TextViews for efficient view access.
     */
    static class VH extends RecyclerView.ViewHolder {
        /** TextView displaying the notification title */
        final TextView title;
        /** TextView displaying the notification message */
        final TextView message;
        /** TextView displaying the formatted creation date of the notification */
        final TextView date;

        /**
         * Initializes the ViewHolder and finds all required views within the item layout.
         *
         * @param v The root view of the notification item layout
         */
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            message = v.findViewById(R.id.tvMessage);
            date = v.findViewById(R.id.tvDate);
        }
    }
}