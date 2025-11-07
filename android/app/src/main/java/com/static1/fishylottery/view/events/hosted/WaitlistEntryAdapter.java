package com.static1.fishylottery.view.events.hosted;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.WaitlistEntry;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WaitlistEntryAdapter extends RecyclerView.Adapter<WaitlistEntryAdapter.ViewHolder> {

    private List<WaitlistEntry> waitlistEntries;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WaitlistEntry entry);
    }

    public WaitlistEntryAdapter(List<WaitlistEntry> waitlistEntries, OnItemClickListener listener) {
        this.waitlistEntries = waitlistEntries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaitlistEntry entry = waitlistEntries.get(position);
        holder.bind(entry, listener);
    }

    @Override
    public int getItemCount() {
        return waitlistEntries != null ? waitlistEntries.size() : 0;
    }

    public void updateData(List<WaitlistEntry> newEntries) {
        this.waitlistEntries = newEntries;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView statusTextView;
        private TextView joinedTextView;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_entrant_name);
            statusTextView = itemView.findViewById(R.id.text_status);
            joinedTextView = itemView.findViewById(R.id.text_joined);
        }

        void bind(final WaitlistEntry entry, final OnItemClickListener listener) {
            if (entry.getProfile() != null && entry.getProfile().getFullName() != null) {
                nameTextView.setText(entry.getProfile().getFullName());
            } else {
                nameTextView.setText("Unknown");
            }

            statusTextView.setText("Status: " + (entry.getStatus() != null ? entry.getStatus() : "waiting"));

            if (entry.getJoinedAt() != null) {
                joinedTextView.setText("Joined: " + dateFormat.format(entry.getJoinedAt()));
            } else {
                joinedTextView.setText("Joined: —");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(entry);
            });
        }
    }
}
