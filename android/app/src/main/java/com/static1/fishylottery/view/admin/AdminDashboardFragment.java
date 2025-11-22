package com.static1.fishylottery.view.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;

public class AdminDashboardFragment extends Fragment {
    private View rowAllEvents, rowProfiles, rowOrganizers, rowImages, rowNotificationLogs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        rowAllEvents = view.findViewById(R.id.row_all_events);
        rowProfiles = view.findViewById(R.id.row_profiles);
        rowOrganizers = view.findViewById(R.id.row_organizers);
        rowImages = view.findViewById(R.id.row_images);
        rowNotificationLogs = view.findViewById(R.id.row_notification_logs);

        setupRowLabels();
        setupListeners();

        return view;
    }

    private void setupRowLabels() {
        setRowTitle(rowAllEvents, "All Events");
        setRowTitle(rowProfiles, "Profiles");
        setRowTitle(rowOrganizers, "Organizers");
        setRowTitle(rowImages, "Uploaded Images");
        setRowTitle(rowNotificationLogs, "Notification Logs");
    }

    private void setRowTitle(View view, String title) {
        TextView textRowTitle = view.findViewById(R.id.text_row_title);
        textRowTitle.setText(title);
    }

    private void setupListeners() {
        rowAllEvents.setOnClickListener(v -> navigateTo(v, R.id.action_adminDashboard_to_allEvents));
        rowProfiles.setOnClickListener(v -> navigateTo(v, R.id.action_adminDashboard_to_profiles));
        rowOrganizers.setOnClickListener(v -> navigateTo(v, R.id.action_adminDashboard_to_organizers));
        rowImages.setOnClickListener(v -> navigateTo(v, R.id.action_adminDashboard_to_images));
        rowNotificationLogs.setOnClickListener(v -> navigateTo(v, R.id.action_adminDashboard_to_notificationLogs));
    }

    private void navigateTo(View view, @IdRes int id) {
        Navigation.findNavController(view).navigate(id);
    }
}
