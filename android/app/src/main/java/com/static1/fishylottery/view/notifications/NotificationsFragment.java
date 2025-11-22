package com.static1.fishylottery.view.notifications;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.static1.fishylottery.R;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.services.NotificationSettings;
import com.static1.fishylottery.viewmodel.NotificationsViewModel;

public class NotificationsFragment extends Fragment {
    private static final String TAG = "NotificationsFragment";
    public NotificationsViewModel vm;
    public NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        RecyclerView rv = v.findViewById(R.id.rvNotifications);
        TextView textNoNotificationsMessage = v.findViewById(R.id.text_no_notifications_message);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new NotificationAdapter();
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(NotificationsViewModel.class);

        vm.getInbox().observe(getViewLifecycleOwner(), items -> {
            boolean notificationsEnabled = NotificationSettings.areNotificationsEnabled(requireContext());
            Log.d(TAG, "Observer triggered - Notifications enabled: " + notificationsEnabled + ", Items count: " + (items != null ? items.size() : 0));

            // Check if notifications are enabled before showing items
            if (!notificationsEnabled) {
                // Notifications disabled - show empty state
                textNoNotificationsMessage.setText("Notifications are disabled");
                textNoNotificationsMessage.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
                adapter.submit(null); // Clear the list
                return;
            }

            // Notifications enabled - show normally
            rv.setVisibility(View.VISIBLE);
            textNoNotificationsMessage.setText("No Notifications");
            textNoNotificationsMessage.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            adapter.submit(items);
        });

        adapter.setOnNotificationClick(n -> {
            NavController nav = Navigation.findNavController(v);

            Bundle b = new Bundle();
            b.putString("notificationId", n.getId());
            b.putString("title", n.getTitle());
            b.putString("message", n.getMessage());
            b.putLong("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().getTime() : 0L);
            b.putString("type", n.getType());
            b.putString("status", n.getStatus());

            nav.navigate(R.id.notificationDetailFragment, b);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        String uid = AuthManager.getInstance().getUserId();
        boolean enabled = NotificationSettings.areNotificationsEnabled(requireContext());
        Log.d(TAG, "onStart - Notifications enabled: " + enabled);
        // Pass context to ViewModel
        if (uid != null) vm.start(uid, requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean enabled = NotificationSettings.areNotificationsEnabled(requireContext());
        Log.d(TAG, "onResume - Notifications enabled: " + enabled);
        // Refresh when coming back from settings
        String uid = AuthManager.getInstance().getUserId();
        if (uid != null) {
            vm.stop();
            vm.start(uid, requireContext());
        }
    }

    @Override
    public void onStop() {
        vm.stop();
        super.onStop();
    }
}