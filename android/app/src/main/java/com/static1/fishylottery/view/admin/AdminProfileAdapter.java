package com.static1.fishylottery.view.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Profile;

import java.util.ArrayList;
import java.util.List;

public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(Profile profile);
    }

    private List<Profile> profiles = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;

    public AdminProfileAdapter(OnDeleteClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail, textPhone, textInitials;
        Button buttonDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            textEmail = itemView.findViewById(R.id.text_email);
            textPhone = itemView.findViewById(R.id.text_phone);
            textInitials = itemView.findViewById(R.id.text_initials);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }

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