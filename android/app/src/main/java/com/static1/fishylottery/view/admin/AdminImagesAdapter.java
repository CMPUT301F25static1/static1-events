package com.static1.fishylottery.view.admin;

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

import java.util.List;

/**
 * RecyclerView adapter used on the admin screen to display event posters.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Display a grid/list of event posters that have associated image URLs.</li>
 *     <li>Show each event’s title and image thumbnail.</li>
 *     <li>Provide a delete button that allows administrators to remove an event's poster.</li>
 * </ul>
 */
public class AdminImagesAdapter extends RecyclerView.Adapter<AdminImagesAdapter.ImageViewHolder> {

    /**
     * Listener interface used to notify when the admin clicks the delete icon for an event.
     */
    public interface OnImageActionListener {
        /**
         * Called when the delete image button is pressed for a specific event.
         *
         * @param event the event whose poster should be deleted.
         */
        void onDeleteImageClicked(Event event);
    }

    private final List<Event> items;
    private final OnImageActionListener listener;

    /**
     * Creates an adapter for displaying admin poster items.
     *
     * @param items    list of events that contain poster images
     * @param listener listener handling delete actions for each event
     */
    public AdminImagesAdapter(List<Event> items, OnImageActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds a single event to the ViewHolder at the given position.
     *
     * @param holder   holder containing UI references
     * @param position position in the adapter list
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Event event = items.get(position);
        holder.bind(event, listener);
    }

    /**
     * @return the number of poster items displayed in the list.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder representing a single event poster item.
     * <p>
     * Contains:
     * <ul>
     *     <li>Poster thumbnail</li>
     *     <li>Event title</li>
     *     <li>Delete button</li>
     * </ul>
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPoster;
        TextView tvTitle;
        ImageButton btnDelete;

        /**
         * Constructs a ViewHolder for an admin poster item.
         *
         * @param itemView root view of the poster item layout
         */
        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.admin_image_poster);
            tvTitle = itemView.findViewById(R.id.admin_image_title);
            btnDelete = itemView.findViewById(R.id.admin_image_delete);
        }

        /**
         * Binds the UI components to the given {@link Event}, including:
         * <ul>
         *     <li>Loading the image via Glide</li>
         *     <li>Displaying title text</li>
         *     <li>Setting delete button behavior</li>
         * </ul>
         *
         * @param event    the event being displayed
         * @param listener the listener handling delete button presses
         */
        void bind(Event event, OnImageActionListener listener) {
            String title = event != null && event.getTitle() != null
                    ? event.getTitle()
                    : "Untitled event";
            tvTitle.setText(title);

            String imageUrl = event != null ? event.getImageUrl() : null;

            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    Glide.with(ivPoster.getContext())
                            .load(imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.circle_background_light_blue)
                            .into(ivPoster);
                } catch (NoClassDefFoundError e) {
                    ivPoster.setImageResource(R.drawable.circle_background_light_blue);
                }
            } else {
                ivPoster.setImageResource(R.drawable.circle_background_light_blue);
            }

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteImageClicked(event);
                }
            });
        }
    }
}
