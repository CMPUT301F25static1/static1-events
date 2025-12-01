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

/**
 * Fragment representing the main dashboard for administrators.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Display administrative navigation options such as Events, Profiles, Organizers, Images, and Notification Logs.</li>
 *     <li>Handle user interactions and navigate to appropriate admin screens.</li>
 *     <li>Configure UI labels and icons for dashboard rows.</li>
 * </ul>
 * <p>
 * This fragment functions as the central hub for all admin-level actions.
 */
public class AdminDashboardFragment extends Fragment {

    private View rowAllEvents, rowProfiles, rowOrganizers, rowImages, rowNotificationLogs;

    /**
     * Inflates the dashboard layout, initializes UI rows, assigns their labels/icons,
     * and attaches click listeners for navigation.
     *
     * @param inflater  inflater used to inflate the fragment layout
     * @param container parent view container
     * @param savedInstanceState saved fragment state (unused)
     * @return the root view for the admin dashboard
     */
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

    /**
     * Sets the display text and icon for each dashboard option row.
     * This helps visually distinguish the available admin actions.
     */
    private void setupRowLabels() {
        setRowTitle(rowAllEvents, "All Events", R.drawable.ic_all_events);
        setRowTitle(rowProfiles, "Profiles", R.drawable.ic_profiles);
        setRowTitle(rowOrganizers, "Organizers", R.drawable.ic_organizers);
        setRowTitle(rowImages, "Uploaded Images", R.drawable.ic_images);
        setRowTitle(rowNotificationLogs, "Notification Logs", R.drawable.ic_notification_logs);
    }

    /**
     * Helper method to assign a title and icon to a dashboard row.
     *
     * @param row         the dashboard row view container
     * @param title       the title displayed for that row
     * @param drawableRes the icon resource representing the row’s purpose
     */
    private void setRowTitle(View row, String title, int drawableRes) {
        TextView textRowTitle = row.findViewById(R.id.text_row_title);
        textRowTitle.setText(title);

        android.widget.ImageView imageIcon = row.findViewById(R.id.item_icon);
        imageIcon.setImageResource(drawableRes);
    }

    /**
     * Registers click listeners for each dashboard option.
     * When a row is clicked, it navigates to the corresponding admin screen.
     */
    private void setupListeners() {
        rowAllEvents.setOnClickListener(v -> navigateTo(v, R.id.action_adminDashboard_to_allEvents));
        rowProfiles.setOnClickListener(v -> navigateTo(v, R.id.action_adminDashboard_to_profiles));
        rowOrganizers.setOnClickListener(v -> navigateTo(v, R.id.action_adminDashboard_to_organizers));
        rowImages.setOnClickListener(v -> navigateTo(v, R.id.action_adminDashboard_to_images));
        rowNotificationLogs.setOnClickListener(v -> navigateTo(v, R.id.action_adminDashboard_to_notificationLogs));
    }

    /**
     * Navigates to a target destination defined in the Navigation Graph.
     *
     * @param view the clicked row view initiating navigation
     * @param id   the Navigation Component action ID to navigate to
     */
    private void navigateTo(View view, @IdRes int id) {
        Navigation.findNavController(view).navigate(id);
    }

}
