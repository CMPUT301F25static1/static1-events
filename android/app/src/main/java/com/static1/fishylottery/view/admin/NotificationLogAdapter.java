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

public class NotificationLogAdapter extends RecyclerView.Adapter<NotificationLogAdapter.ViewHolder> {

    public static class NotificationLog {
        public String recipientName;
        public String recipientEmail;
        public String title;
        public String message;
        public String type;
        public long timestamp;

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

        void bind(NotificationLog log) {
            textRecipient.setText(log.recipientName + " (" + log.recipientEmail + ")");
            textTitle.setText(log.title);
            textMessage.setText(log.message);
            textType.setText("Type: " + log.type);
            textDate.setText(dateFormat.format(log.timestamp));
        }
    }
}
