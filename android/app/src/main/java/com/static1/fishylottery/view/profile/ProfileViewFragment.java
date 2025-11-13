package com.static1.fishylottery.view.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;
import com.static1.fishylottery.viewmodel.ProfileViewModel;

public class ProfileViewFragment extends Fragment {
    private TextView textName, textEmail, textInitials, textPhone;
    private View rowEditProfile, rowNotifications, rowEventsHistory, rowAdminLogin;
    private ProfileViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_view, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        textName = view.findViewById(R.id.text_name);
        textEmail = view.findViewById(R.id.text_email);
        textInitials = view.findViewById(R.id.text_initials);
        textPhone = view.findViewById(R.id.text_phone);

        rowEditProfile = view.findViewById(R.id.row_edit_profile);
        rowNotifications = view.findViewById(R.id.row_notifications);
        rowEventsHistory = view.findViewById(R.id.row_events_history);
        rowAdminLogin = view.findViewById(R.id.row_admin_login);

        setupRowLabels(view);
        setupListeners(view);

        viewModel.loadProfile();

        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            textName.setText(profile.getFullName());
            textEmail.setText(profile.getEmail());
            textInitials.setText(profile.getInitials());
            textPhone.setText(profile.getFormattedPhone());
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
        rowEditProfile.setOnClickListener(v ->navigateWithAction(view, R.id.action_profile_to_edit_profile));
        rowNotifications.setOnClickListener(v -> navigateWithAction(view, R.id.action_profile_to_notification_settings));
        rowEventsHistory.setOnClickListener(v -> navigateWithAction(view, R.id.action_profile_to_event_history));
        rowAdminLogin.setOnClickListener(v -> navigateWithAction(view, R.id.action_profile_to_admin_dashboard));
    }

    private void navigateWithAction(View view, @IdRes int action) {
        Navigation.findNavController(view).navigate(action);
    }
}
