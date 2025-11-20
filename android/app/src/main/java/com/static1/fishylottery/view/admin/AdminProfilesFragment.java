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

import com.static1.fishylottery.R;
import com.static1.fishylottery.viewmodel.AdminProfilesViewModel;

public class AdminProfilesFragment extends Fragment {
    private AdminProfilesViewModel viewModel;
    private RecyclerView recyclerView;
    private AdminProfileAdapter adapter;
    private ProgressBar progressBar;
    private TextView textEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profiles, container, false);

        viewModel = new ViewModelProvider(this).get(AdminProfilesViewModel.class);

        recyclerView = view.findViewById(R.id.recycler_profiles);
        progressBar = view.findViewById(R.id.progress_bar);
        textEmpty = view.findViewById(R.id.text_empty);

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