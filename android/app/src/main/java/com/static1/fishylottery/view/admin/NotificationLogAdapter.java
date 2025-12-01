package com.static1.fishylottery.view.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A {@link RecyclerView.Adapter} that displays a list of notification logs in the admin panel.
 * Each item shows recipient information, notification title, message, type, and timestamp.
 */
public class NotificationLogAdapter extends RecyclerView.Adapter<NotificationLogAdapter.ViewHolder> {

    /**
     * Represents a single notification log entry.
     */
    public static class NotificationLog {
        /** The name of the recipient */
        public String recipientName;
        /** The email address of the recipient */
        public String recipientEmail;
        /** The title of the notification */
        public String title;
        /** The body message of the notification */
        public String message;
        /** The type of notification (e.g., "email", "push", "system") */
        public String type;
        /** The timestamp when the notification was sent (in milliseconds since epoch) */
        public long timestamp;

        /**
         * Creates a new notification log entry.
         *
         * @param recipientName  name of the recipient
         * @param recipientEmail email of the recipient
         * @param title          notification title
         * @param message        notification message body
         * @param type           type of notification
         * @param timestamp      time the notification was sent
         */

        public NotificationLog(String recipientName, String recipientEmail, String title,
                               String message, String type, long timestamp) {
            this.recipientName = recipientName;
            this.recipientEmail = recipientEmail;
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
        }
    }

    private List<NotificationLog> logs = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

    /**
     * Replaces the current list of notification logs with a new list and notifies the adapter.
     *
     * @param newLogs the new list of notification logs; may be null to clear the list
     */
    public void submitList(List<NotificationLog> newLogs) {
        logs.clear();
        if (newLogs != null) {
            logs.addAll(newLogs);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationLog log = logs.get(position);
        holder.bind(log);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    /**
     * ViewHolder for displaying a single notification log item.
     */

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textRecipient, textTitle, textMessage, textType, textDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textRecipient = itemView.findViewById(R.id.text_recipient);
            textTitle = itemView.findViewById(R.id.text_title);
            textMessage = itemView.findViewById(R.id.text_message);
            textType = itemView.findViewById(R.id.text_type);
            textDate = itemView.findViewById(R.id.text_date);
        }

        /**
         * Binds the notification log data to the view elements.
         *
         * @param log the notification log to display
         */

        void bind(NotificationLog log) {
            textRecipient.setText(log.recipientName + " (" + log.recipientEmail + ")");
            textTitle.setText(log.title);
            textMessage.setText(log.message);
            textType.setText("Type: " + log.type);
            textDate.setText(dateFormat.format(log.timestamp));
        }
    }
}
