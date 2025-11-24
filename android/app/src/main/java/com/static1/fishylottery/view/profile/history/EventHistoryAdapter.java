package com.static1.fishylottery.view.profile.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;

import java.util.List;

public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.ViewHolder> {

    private final List<EventHistoryItem> items;
    private OnEventClickListener onEventClickListener;

    public EventHistoryAdapter(List<EventHistoryItem> items) {
        this.items = items;
    }

    public interface OnEventClickListener {
        void onEventClick(EventHistoryItem item);
    }

    public void setOnEventClickListener(EventHistoryAdapter.OnEventClickListener l) {
        this.onEventClickListener = l;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, when, where, status;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_event_title);
            when = itemView.findViewById(R.id.text_event_when);
            where = itemView.findViewById(R.id.text_event_where);
            status = itemView.findViewById(R.id.text_waitlist_status);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && onEventClickListener != null) {
                    onEventClickListener.onEventClick(items.get(pos));
                }
            });
        }
    }

    @NonNull
    @Override
    public EventHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EventHistoryItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.when.setText(item.getWhen());
        holder.where.setText(item.getWhere());
        holder.status.setText(item.getStatus());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
