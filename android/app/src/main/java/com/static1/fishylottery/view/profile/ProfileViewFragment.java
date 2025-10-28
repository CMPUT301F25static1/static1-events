package com.static1.fishylottery.view.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.ProfileController;
import com.static1.fishylottery.model.entities.Profile;

public class ProfileViewFragment extends Fragment {
    private TextView textName, textEmail;
    private View rowEditProfile, rowNotifications, rowEventsHistory, rowAdminLogin;
    private ProfileController controller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_view, container, false);

        controller = ((ProfileContainerFragment) getParentFragment()).getController();

        textName = view.findViewById(R.id.text_name);
        textEmail = view.findViewById(R.id.text_email);

        rowEditProfile = view.findViewById(R.id.row_edit_profile);
        rowNotifications = view.findViewById(R.id.row_notifications);
        rowEventsHistory = view.findViewById(R.id.row_events_history);
        rowAdminLogin = view.findViewById(R.id.row_admin_login);

        setupRowLabels(view);
        setupListeners(view);

        controller.loadProfile(new ProfileController.ProfileCallback() {
            @Override
            public void onProfileLoaded(Profile profile) {
                textName.setText(profile.getFullName());
                textEmail.setText(profile.getEmail());
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setupRowLabels(View root) {
        setRowTitle(root, R.id.row_edit_profile, "Edit Profile");
        setRowTitle(root, R.id.row_notifications, "Notifications");
        setRowTitle(root, R.id.row_events_history, "Events History");
        setRowTitle(root, R.id.row_admin_login, "Admin Login");
    }

    private void setRowTitle(View root, int rowId, String title) {
        View row = root.findViewById(rowId);
        TextView textRowTitle = row.findViewById(R.id.text_row_title);
        textRowTitle.setText(title);
    }

    private void setupListeners(View view) {
        rowEditProfile.setOnClickListener(v -> openEditProfile(view));
        rowNotifications.setOnClickListener(v -> openNotifications());
        rowEventsHistory.setOnClickListener(v -> openEventsHistory());
        rowAdminLogin.setOnClickListener(v -> openAdminLogin());
    }

    private void openEditProfile(View view) {
        Navigation.findNavController(view).navigate(R.id.action_profile_to_edit_profile);
    }



    private void openNotifications() {
        Toast.makeText(getContext(), "Notifications clicked", Toast.LENGTH_SHORT).show();
    }

    private void openEventsHistory() {
        Toast.makeText(getContext(), "Events History clicked", Toast.LENGTH_SHORT).show();
    }

    private void openAdminLogin() {
        Toast.makeText(getContext(), "Admin Login clicked", Toast.LENGTH_SHORT).show();
    }
}
