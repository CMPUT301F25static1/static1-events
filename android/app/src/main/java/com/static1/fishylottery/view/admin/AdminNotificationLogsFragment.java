package com.static1.fishylottery.view.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;
import com.static1.fishylottery.viewmodel.AdminNotificationLogsViewModel;

public class AdminNotificationLogsFragment extends Fragment {
    private AdminNotificationLogsViewModel viewModel;
    private RecyclerView recyclerView;
    private NotificationLogAdapter adapter;
    private ProgressBar progressBar;
    private TextView textEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_notification_logs, container, false);

        viewModel = new ViewModelProvider(this).get(AdminNotificationLogsViewModel.class);

        recyclerView = view.findViewById(R.id.recycler_notification_logs);
        progressBar = view.findViewById(R.id.progress_bar);
        textEmpty = view.findViewById(R.id.text_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationLogAdapter();
        recyclerView.setAdapter(adapter);

        viewModel.getNotificationLogs().observe(getViewLifecycleOwner(), logs -> {
            adapter.submitList(logs);
            textEmpty.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.loadAllNotifications();

        return view;
    }
}