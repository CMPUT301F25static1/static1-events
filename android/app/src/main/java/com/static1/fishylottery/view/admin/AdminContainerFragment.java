package com.static1.fishylottery.view.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.logic.AdminSession;

/**
 * Fragment acting as a dynamic container for all admin-related screens.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Decide whether to show the admin login screen or the admin dashboard based on the session state.</li>
 *     <li>Load child fragments inside its container layout via fragment transactions.</li>
 * </ul>
 * <p>
 * This fragment serves as the entry point for administrative navigation.
 */
public class AdminContainerFragment extends Fragment {

    /**
     * Inflates the admin container layout and determines whether the login screen
     * or dashboard should be displayed depending on the {@link AdminSession#isLoggedIn} flag.
     *
     * @param inflater  the LayoutInflater used to inflate views
     * @param container optional parent view
     * @param savedInstanceState saved state bundle (unused)
     * @return the fully constructed root view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_container, container, false);

        // Show login or dashboard based on session flag
        if (AdminSession.isLoggedIn) {
            showDashboard();
        } else {
            showLogin();
        }

        return view;
    }

    /**
     * Displays the admin login fragment inside the container.
     * This is shown when no valid admin session is active.
     */
    public void showLogin() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.admin_container, new AdminLoginFragment());
        ft.commit();
    }

    /**
     * Displays the admin dashboard fragment inside the container.
     * This is shown when an admin user is already authenticated.
     */
    public void showDashboard() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.admin_container, new AdminDashboardFragment());
        ft.commit();
    }
}
