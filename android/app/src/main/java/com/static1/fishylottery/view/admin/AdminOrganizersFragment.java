package com.static1.fishylottery.view.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.static1.fishylottery.R;
import com.static1.fishylottery.viewmodel.AdminOrganizersViewModel;

public class AdminOrganizersFragment extends Fragment {
    private AdminOrganizersViewModel viewModel;
    private RecyclerView recyclerView;
    private AdminOrganizerAdapter adapter;
    private ProgressBar progressBar;
    private TextView textEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_organizers, container, false);

        viewModel = new ViewModelProvider(this).get(AdminOrganizersViewModel.class);

        recyclerView = view.findViewById(R.id.recycler_organizers);
        progressBar = view.findViewById(R.id.progress_bar);
        textEmpty = view.findViewById(R.id.text_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminOrganizerAdapter(organizer -> {
            // Show confirmation dialog
            new AlertDialog.Builder(requireContext())
                    .setTitle("Remove Organizer")
                    .setMessage("Are you sure you want to remove " + organizer.organizerName +
                            "? This will also delete all their events.")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        viewModel.removeOrganizer(organizer.organizerId);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        recyclerView.setAdapter(adapter);

        BottomNavigationView navView = requireActivity().findViewById(R.id.nav_view);
        navView.post(() -> {
            recyclerView.setPadding(0, 0, 0, navView.getHeight());
        });

        viewModel.getOrganizers().observe(getViewLifecycleOwner(), organizers -> {
            adapter.submitList(organizers);
            textEmpty.setVisibility(organizers.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadOrganizers();

        return view;
    }
}