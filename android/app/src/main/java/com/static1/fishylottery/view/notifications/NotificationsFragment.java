package com.static1.fishylottery.view.notifications;

import android.os.Bundle;
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
import com.static1.fishylottery.viewmodel.NotificationsViewModel;

public class NotificationsFragment extends Fragment {

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
        if (uid != null) vm.start(uid);
    }

    @Override
    public void onStop() {
        vm.stop();
        super.onStop();
    }
}
