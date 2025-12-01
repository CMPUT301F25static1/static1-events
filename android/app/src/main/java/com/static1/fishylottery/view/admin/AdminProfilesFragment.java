package com.static1.fishylottery.view.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.static1.fishylottery.R;
import com.static1.fishylottery.viewmodel.AdminProfilesViewModel;

/**
 * A {@link Fragment} that displays a list of all user profiles in the admin panel.
 * Allows administrators to view and delete individual user profiles with confirmation.
 * Observes the {@link AdminProfilesViewModel} for data and loading state changes.
 */

public class AdminProfilesFragment extends Fragment {
    private AdminProfilesViewModel viewModel;
    private RecyclerView recyclerView;
    private AdminProfileAdapter adapter;
    private ProgressBar progressBar;
    private TextView textEmpty;

    /**
     * Default constructor required by the Android Fragment system.
     * Used when the fragment is instantiated via reflection.
     */
    public AdminProfilesFragment() {
    }

    /**
     * Constructor used for dependency injection during testing.
     * Allows injecting a mock or test {@link AdminProfilesViewModel}.
     *
     * @param viewModel the ViewModel to be used instead of creating a new one
     */
    public AdminProfilesFragment(AdminProfilesViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profiles, container, false);

        // Only create ViewModel if not already provided (for testing)
        if (viewModel == null) {
            viewModel = new ViewModelProvider(this).get(AdminProfilesViewModel.class);
        }

        recyclerView = view.findViewById(R.id.recycler_view_all_profiles);
        progressBar = view.findViewById(R.id.progress_bar);
        textEmpty = view.findViewById(R.id.text_empty);

        FragmentActivity activity = getActivity();

        if (activity != null) {
            BottomNavigationView navView = activity.findViewById(R.id.nav_view);
            if (navView != null) {
                navView.post(() -> {
                    recyclerView.setPadding(0, 0, 0, navView.getHeight());
                });
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminProfileAdapter(profile -> {
            // Show confirmation dialog
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete " + profile.getFullName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteProfile(profile);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        recyclerView.setAdapter(adapter);

        viewModel.getProfiles().observe(getViewLifecycleOwner(), profiles -> {
            adapter.submitList(profiles);
            textEmpty.setVisibility(profiles.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadProfiles();

        return view;
    }
}