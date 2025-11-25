package com.static1.fishylottery.view.events.hosted;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
        void onDeleteEntrant(WaitlistEntry entry);
        void onCancelEntrant(WaitlistEntry entry);
        void onAcceptEntrant(WaitlistEntry entry);
        void onInviteEntrant(WaitlistEntry entry);
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
        private ImageButton menuButton;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_entrant_name);
            statusTextView = itemView.findViewById(R.id.text_status);
            joinedTextView = itemView.findViewById(R.id.text_joined);
            menuButton = itemView.findViewById(R.id.button_menu);
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

            menuButton.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.entrant_row_menu, popup.getMenu());

                String status = entry.getStatus();

                switch (status) {
                    case "waiting":
                        // Can invite or delete
                        popup.getMenu().findItem(R.id.action_delete_entrant).setVisible(true);
                        popup.getMenu().findItem(R.id.action_invite_entrant).setVisible(true);
                        popup.getMenu().findItem(R.id.action_cancel_entrant).setVisible(false);
                        popup.getMenu().findItem(R.id.action_accept_entrant).setVisible(false);

                        break;
                    case "invited":
                        // Can delete, cancel, or accept entrant
                        popup.getMenu().findItem(R.id.action_delete_entrant).setVisible(true);
                        popup.getMenu().findItem(R.id.action_invite_entrant).setVisible(false);
                        popup.getMenu().findItem(R.id.action_cancel_entrant).setVisible(true);
                        popup.getMenu().findItem(R.id.action_accept_entrant).setVisible(true);
                        break;
                    case "accepted":
                        // Can cancel or delete entrant
                        popup.getMenu().findItem(R.id.action_delete_entrant).setVisible(true);
                        popup.getMenu().findItem(R.id.action_invite_entrant).setVisible(false);
                        popup.getMenu().findItem(R.id.action_cancel_entrant).setVisible(true);
                        popup.getMenu().findItem(R.id.action_accept_entrant).setVisible(false);
                        break;
                    default:
                        // Can only delete
                        popup.getMenu().findItem(R.id.action_delete_entrant).setVisible(true);
                        popup.getMenu().findItem(R.id.action_invite_entrant).setVisible(false);
                        popup.getMenu().findItem(R.id.action_cancel_entrant).setVisible(false);
                        popup.getMenu().findItem(R.id.action_accept_entrant).setVisible(false);
                }

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_cancel_entrant) {
                        listener.onCancelEntrant(entry);
                        return true;
                    } else if (item.getItemId() == R.id.action_accept_entrant) {
                        listener.onAcceptEntrant(entry);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_entrant) {
                        listener.onDeleteEntrant(entry);
                        return true;
                    } else if (item.getItemId() == R.id.action_invite_entrant) {
                        listener.onInviteEntrant(entry);
                        return true;
                    } else {
                        return false;
                    }
                });

                popup.show();
            });
        }
    }
}
