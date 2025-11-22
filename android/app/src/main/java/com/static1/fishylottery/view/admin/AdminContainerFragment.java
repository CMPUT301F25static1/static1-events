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

public class AdminContainerFragment extends Fragment {
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

    public void showLogin() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.admin_container, new AdminLoginFragment());
        ft.commit();
    }

    public void showDashboard() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.admin_container, new AdminDashboardFragment());
        ft.commit();
    }
}
