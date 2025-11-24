package com.static1.fishylottery.view.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;

import java.util.ArrayList;
import java.util.List;

public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.ViewHolder> {

    public static class OrganizerInfo {
        public String organizerId;
        public String organizerName;
        public String organizerEmail;
        public int eventCount;

        public OrganizerInfo(String id, String name, String email, int eventCount) {
            this.organizerId = id;
            this.organizerName = name;
            this.organizerEmail = email;
            this.eventCount = eventCount;
        }
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(OrganizerInfo organizer);
    }

    private List<OrganizerInfo> organizers = new ArrayList<>();
    private final OnRemoveClickListener removeListener;

    public AdminOrganizerAdapter(OnRemoveClickListener removeListener) {
        this.removeListener = removeListener;
    }

    public void submitList(List<OrganizerInfo> newOrganizers) {
        organizers.clear();
        if (newOrganizers != null) {
            organizers.addAll(newOrganizers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_organizer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrganizerInfo organizer = organizers.get(position);
        holder.bind(organizer, removeListener);
    }

    @Override
    public int getItemCount() {
        return organizers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail, textEventCount;
        Button buttonRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_organizer_name);
            textEmail = itemView.findViewById(R.id.text_organizer_email);
            textEventCount = itemView.findViewById(R.id.text_event_count);
            buttonRemove = itemView.findViewById(R.id.button_remove);
        }

        void bind(OrganizerInfo organizer, OnRemoveClickListener listener) {
            textName.setText(organizer.organizerName);
            textEmail.setText(organizer.organizerEmail);
            textEventCount.setText("Events: " + organizer.eventCount);

            buttonRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveClick(organizer);
                }
            });
        }
    }
}