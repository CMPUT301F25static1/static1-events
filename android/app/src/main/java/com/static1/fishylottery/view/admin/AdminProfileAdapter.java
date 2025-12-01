package com.static1.fishylottery.view.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link RecyclerView.Adapter} that displays a list of user profiles in the admin panel.
 * Each item shows the user's name, email, phone, initials, and a delete button.
 */
public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ViewHolder> {
    /**
     * Listener interface for handling delete button clicks on profile items.
     */
    public interface OnDeleteClickListener {
        /**
         * Called when the delete button is clicked for a profile.
         *
         * @param profile the profile to be deleted
         */
        void onDeleteClick(Profile profile);
    }

    private List<Profile> profiles = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;

    /**
     * Creates a new adapter with the specified delete listener.
     *
     * @param deleteListener listener to be notified when delete is clicked
     */
    public AdminProfileAdapter(OnDeleteClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    /**
     * Replaces the current list of profiles with a new list and notifies the adapter.
     *
     * @param newProfiles the new list of profiles; may be null to clear the list
     */
    public void submitList(List<Profile> newProfiles) {
        profiles.clear();
        if (newProfiles != null) {
            profiles.addAll(newProfiles);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profile profile = profiles.get(position);
        holder.bind(profile, deleteListener);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /**
     * ViewHolder for displaying a single user profile with a delete action.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail, textPhone, textInitials;
        ImageButton buttonDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_profile_name);
            textEmail = itemView.findViewById(R.id.text_profile_email);
            textPhone = itemView.findViewById(R.id.text_profile_phone);
            textInitials = itemView.findViewById(R.id.text_profile_initials);
            buttonDelete = itemView.findViewById(R.id.button_profile_delete);
        }

        /**
         * Binds profile data to the views and sets up the delete button listener.
         *
         * @param profile  the user profile to display
         * @param listener the listener to notify when delete is clicked
         */
        void bind(Profile profile, OnDeleteClickListener listener) {
            textName.setText(profile.getFullName());
            textEmail.setText(profile.getEmail());
            textPhone.setText(profile.getFormattedPhone());
            textInitials.setText(profile.getInitials());

            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(profile);
                }
            });
        }
    }
}