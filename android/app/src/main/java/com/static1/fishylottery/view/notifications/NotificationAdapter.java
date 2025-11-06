package com.static1.fishylottery.view.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.AppNotification;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
    private final List<AppNotification> items = new ArrayList<>();
    private final DateFormat fmt = DateFormat.getDateTimeInstance();

    public void submit(List<AppNotification> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AppNotification n = items.get(pos);
        h.title.setText(n.getTitle());
        h.message.setText(n.getMessage());
        h.date.setText(n.getCreatedAt() != null ? fmt.format(n.getCreatedAt()) : "");
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, message, date;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            message = v.findViewById(R.id.tvMessage);
            date = v.findViewById(R.id.tvDate);
        }
    }
}
