package com.static1.fishylottery.view.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.AppNotification;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface OnNotificationClick {

        /**
         * Called when the user taps a notification item.
         *
         * @param n The clicked {@link AppNotification}.
         */
        void onClick(AppNotification n);
    }

    /** Listener triggered when an item is clicked. */
    private OnNotificationClick clickListener;

    /** Internal list of notifications displayed by the adapter. */
    private final List<AppNotification> items = new ArrayList<>();

    /** Formats notification dates based on system locale. */
    private final DateFormat fmt = DateFormat.getDateTimeInstance();

    // 🐟 Random fish images
    /** Array of drawable resources for random fish images. */
    private final int[] fishImages = {
            R.drawable.fish_1,
            R.drawable.fish_2,
            R.drawable.fish_3,
            R.drawable.fish_4,
            R.drawable.fish_5,
            R.drawable.fish_6,
            R.drawable.fish_7,
            R.drawable.fish_8,
            R.drawable.fish_9,
            R.drawable.fish_10,
            R.drawable.fish_11,
            R.drawable.fish_12,
            R.drawable.fish_13,
            R.drawable.fish_14,
            R.drawable.fish_15,
            R.drawable.fish_16,
            R.drawable.fish_17,
            R.drawable.fish_18,
            R.drawable.fish_19
    };


    private final Random random = new Random();

    /**
     * Returns the current click listener. (Required for automated tests.)
     *
     * @return The notification click listener, or null if not set.
     */
    // Required by test
    public OnNotificationClick getClickListener() {
        return clickListener;
    }

    /**
     * Assigns a click listener for notification item click events.
     *
     * @param l The listener to set.
     */
    public void setOnNotificationClick(OnNotificationClick l) {
        this.clickListener = l;
    }

    /**
     * Replaces the current list of notifications with a new one.
     * The adapter will immediately refresh the list.
     *
     * @param newItems List of new notifications. Can be null.
     */
    public void submit(List<AppNotification> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AppNotification n = items.get(pos);

        // Set text
        h.title.setText(n.getTitle());
        h.message.setText(n.getMessage());
        h.date.setText(n.getCreatedAt() != null ? fmt.format(n.getCreatedAt()) : "");

        // 🐟 Set a random fish image
        int randomFish = fishImages[random.nextInt(fishImages.length)];
        h.randomFish.setImageResource(randomFish);

        // Click listener
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(n);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class that stores references to views inside each
     * notification list item for performance.
     */

    static class VH extends RecyclerView.ViewHolder {

        TextView title, message, date;
        ImageView randomFish;   // <-- added ImageView reference

        /**
         * Creates a new ViewHolder instance and binds its child views.
         *
         * @param v The inflated item view.
         */

        VH(@NonNull View v) {
            super(v);

            title = v.findViewById(R.id.tvTitle);
            message = v.findViewById(R.id.tvMessage);
            date = v.findViewById(R.id.tvDate);
            randomFish = v.findViewById(R.id.random_fish); // <-- connect it to XML
        }
    }
}
