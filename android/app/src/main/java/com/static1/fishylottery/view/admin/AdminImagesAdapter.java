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
 * RecyclerView adapter for displaying event poster images for admin.
 */
public class AdminImagesAdapter extends RecyclerView.Adapter<AdminImagesAdapter.ImageViewHolder> {

    public interface OnImageActionListener {
        void onDeleteImageClicked(Event event);
    }

    private final List<Event> items;
    private final OnImageActionListener listener;

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

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Event event = items.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle;
        ImageButton btnDelete;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.admin_image_poster);
            tvTitle = itemView.findViewById(R.id.admin_image_title);
            btnDelete = itemView.findViewById(R.id.admin_image_delete);
        }

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
