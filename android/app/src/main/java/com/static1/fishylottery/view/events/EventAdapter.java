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

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private final List<Event> items = new ArrayList<>();
    private final LayoutInflater inflater;
    private OnEventClickListener onEventClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private final boolean showDeleteButton;

    // formatting
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private Context context;

    public EventAdapter(Context context) {
        this(context, false);
    }

    public EventAdapter(Context context, boolean showDeleteButton) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.showDeleteButton = showDeleteButton;
    }

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    public void setOnEventClickListener(OnEventClickListener l) {
        this.onEventClickListener = l;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener l) {
        this.onDeleteClickListener = l;
    }

    public void submitList(List<Event> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public Event getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.content_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
        Event ev = items.get(position);
        holder.bind(ev);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvDate, tvTitle, tvTime, tvLocation;
        ImageButton buttonDelete;

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

        private String formatDateRange(Date start, Date end) {
            if (start == null) return "Date not set";
            if (end == null) return dateFmt.format(start);
            // if same day, show single day
            boolean sameDay = start.getYear() == end.getYear() &&
                    start.getMonth() == end.getMonth() &&
                    start.getDate() == end.getDate();
            return sameDay ? dateFmt.format(start) : dateFmt.format(start) + " - " + dateFmt.format(end);
        }

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
