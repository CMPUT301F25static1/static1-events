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

/**
 * A {@link RecyclerView.Adapter} that displays a list of event organizers in the admin panel.
 * Each item shows the organizer's name, email, number of events, and a remove button.
 */

public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.ViewHolder> {

    /**
     * Represents information about an event organizer from the admin perspective.
     */
    public static class OrganizerInfo {
        /** Unique identifier of the organizer */
        public String organizerId;
        /** Display name of the organizer */
        public String organizerName;
        /** Email address of the organizer */
        public String organizerEmail;
        /** Number of events created by this organizer */
        public int eventCount;

        /**
         * Creates a new OrganizerInfo instance.
         *
         * @param id          the unique ID of the organizer
         * @param name        the display name
         * @param email       the email address
         * @param eventCount  the number of events they have created
         */

        public OrganizerInfo(String id, String name, String email, int eventCount) {
            this.organizerId = id;
            this.organizerName = name;
            this.organizerEmail = email;
            this.eventCount = eventCount;
        }
    }

    /**
     * Listener interface for handling remove button clicks on organizer items.
     */
    public interface OnRemoveClickListener {
        /**
         * Called when the remove button is clicked for an organizer.
         *
         * @param organizer the organizer to be removed
         */
        void onRemoveClick(OrganizerInfo organizer);
    }

    private List<OrganizerInfo> organizers = new ArrayList<>();
    private final OnRemoveClickListener removeListener;

    /**
     * Creates a new adapter with the specified remove listener.
     *
     * @param removeListener listener to be notified when remove is clicked
     */
    public AdminOrganizerAdapter(OnRemoveClickListener removeListener) {
        this.removeListener = removeListener;
    }

    /**
     * Replaces the current list of organizers with a new list and notifies the adapter.
     *
     * @param newOrganizers the new list of organizers; may be null to clear
     */
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

    /**
     * ViewHolder for displaying a single organizer item with a remove button.
     */
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

        /**
         * Binds organizer data to the views and sets up the remove button listener.
         *
         * @param organizer the organizer information to display
         * @param listener  the listener to notify when remove is clicked
         */
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