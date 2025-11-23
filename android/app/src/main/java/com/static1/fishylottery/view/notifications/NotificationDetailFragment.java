package com.static1.fishylottery.view.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.AppNotification;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.viewmodel.NotificationsViewModel;
import com.static1.fishylottery.services.NotificationSettings;

import java.text.DateFormat;
import java.util.Date;

public class NotificationDetailFragment extends Fragment {

    private NotificationsViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        vm = new ViewModelProvider(this).get(NotificationsViewModel.class);

        TextView tvTitle = v.findViewById(R.id.tvDetailTitle);
        TextView tvDate = v.findViewById(R.id.tvDetailDate);
        TextView tvMessage = v.findViewById(R.id.tvDetailMessage);
        TextView buttonViewEvent = v.findViewById(R.id.button_view_event);

        Bundle args = getArguments();
        if (args == null) return;

        AppNotification notification = (AppNotification) args.getSerializable("notification");

        if (notification == null) return;

        buttonViewEvent.setOnClickListener(button -> {
            String eventId = notification.getEventId();

            if (eventId == null) return;

            navigateToEventDetail(eventId);
        });

        tvTitle.setText(notification.getTitle());
        tvMessage.setText(notification.getMessage());

        Date createdAt = notification.getCreatedAt();

        if (createdAt != null) {
            tvDate.setText(DateFormat.getDateTimeInstance().format(createdAt));
        }
    }

    private void navigateToEventDetail(@NonNull String eventId) {
        EventRepository repo = new EventRepository();
        repo.getEventById(eventId)
            .addOnSuccessListener(event -> {
                Bundle b = new Bundle();
                b.putSerializable("event", event);
                Navigation.findNavController(
                        requireActivity(),
                        R.id.nav_host_fragment_activity_main
                ).navigate(R.id.navigation_event_details, b);
            })
            .addOnFailureListener(e -> {
                Log.e("Notifications", "Could not event event", e);
                Toast.makeText(requireContext(), "Could not get event", Toast.LENGTH_SHORT).show();
            });
    }
}
