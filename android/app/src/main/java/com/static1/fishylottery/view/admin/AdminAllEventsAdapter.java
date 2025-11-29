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

public class AdminAllEventsAdapter extends RecyclerView.Adapter<AdminAllEventsAdapter.ViewHolder> {
    private final List<Event> eventList;
    private OnDeleteClickListener onDeleteClickListener;
    private final boolean showDeleteButton;

    public AdminAllEventsAdapter(List<Event> eventList, boolean showDeleteButton) {
        this.eventList = eventList;
        this.showDeleteButton = showDeleteButton;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_event, parent, false);
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

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle, eventDate, eventTime, eventLocation;
        ImageButton buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTime = itemView.findViewById(R.id.eventTime);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            buttonDelete = itemView.findViewById(R.id.button_event_delete);
        }

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
