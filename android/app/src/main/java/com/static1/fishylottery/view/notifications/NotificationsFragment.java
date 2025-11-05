package com.static1.fishylottery.view.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.NotificationsViewModel;

public class NotificationsFragment extends Fragment {
    private NotificationsViewModel vm;
    private NotificationAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        RecyclerView rv = v.findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter();
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(NotificationsViewModel.class);
        vm.getInbox().observe(getViewLifecycleOwner(), items -> adapter.submit(items));
        vm.getError().observe(getViewLifecycleOwner(), err -> { /* show Snackbar/log if needed */ });
    }

    @Override
    public void onStart() {
        super.onStart();
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null) vm.start(uid);
    }

    @Override
    public void onStop() {
        vm.stop();
        super.onStop();
    }
}
