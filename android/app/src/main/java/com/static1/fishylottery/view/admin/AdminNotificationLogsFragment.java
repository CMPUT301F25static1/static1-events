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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.static1.fishylottery.R;
import com.static1.fishylottery.viewmodel.AdminNotificationLogsViewModel;

/**
 * Fragment that displays a list of all notification logs for administrators.
 * <p>
 * Uses {@link AdminNotificationLogsViewModel} to load notifications from the
 * data layer and shows them in a {@link RecyclerView}. While data is loading,
 * a {@link ProgressBar} is shown. If there are no logs, an empty-state message
 * is displayed instead.
 */
public class AdminNotificationLogsFragment extends Fragment {
    private AdminNotificationLogsViewModel viewModel;
    private RecyclerView recyclerView;
    private NotificationLogAdapter adapter;
    private ProgressBar progressBar;
    private TextView textEmpty;
    /**
     * Inflates the notification logs layout, wires up the recycler view, and
     * subscribes to {@link AdminNotificationLogsViewModel} LiveData streams.
     * <p>
     * Behaviour:
     * <ul>
     *   <li>Initialises the recycler view and attaches {@link NotificationLogAdapter}.</li>
     *   <li>Adjusts bottom padding so content is not hidden behind the bottom nav bar.</li>
     *   <li>Observes notification logs:
     *     <ul>
     *       <li>Updates the adapter with the latest list.</li>
     *       <li>Shows or hides the empty-state text based on whether the list is empty.</li>
     *     </ul>
     *   </li>
     *   <li>Observes loading state to show or hide the progress bar.</li>
     *   <li>Triggers an initial load of all notifications via
     *       {@code viewModel.loadAllNotifications()}.</li>
     * </ul>
     *
     * @param inflater  the {@link LayoutInflater} used to inflate the fragment layout
     * @param container optional parent view that the fragment's UI will attach to
     * @param savedInstanceState previously saved state, or {@code null}
     * @return the inflated root {@link View} for this fragment
     */
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

        BottomNavigationView navView = requireActivity().findViewById(R.id.nav_view);
        navView.post(() -> {
            recyclerView.setPadding(0, 0, 0, navView.getHeight());
        });

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