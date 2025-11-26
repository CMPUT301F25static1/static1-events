package com.static1.fishylottery.view.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.services.StorageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin screen for browsing and deleting event poster images.
 * Implements:
 * - US 03.06.01 (browse images)
 * - US 03.03.01 (remove images)
 */
public class AdminImagesFragment extends Fragment implements AdminImagesAdapter.OnImageActionListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    private final List<Event> eventsWithImages = new ArrayList<>();
    private AdminImagesAdapter adapter;
    private EventRepository eventRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_images, container, false);

        recyclerView = view.findViewById(R.id.recycler_admin_images);
        progressBar = view.findViewById(R.id.progress_admin_images);
        emptyText = view.findViewById(R.id.text_admin_images_empty);

        eventRepository = new EventRepository();

        setupRecyclerView();
        loadImages();

        return view;
    }

    private void setupRecyclerView() {
        // Grid layout to satisfy "grid view of images"
        int spanCount = 2; // change to 3 if you prefer smaller tiles
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));

        adapter = new AdminImagesAdapter(eventsWithImages, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadImages() {
        showLoading(true);
        emptyText.setVisibility(View.GONE);

        Task<List<Event>> task = eventRepository.fetchAllEvents();
        task.addOnCompleteListener(t -> {
            showLoading(false);

            if (!t.isSuccessful() || t.getResult() == null) {
                Toast.makeText(requireContext(),
                        "Failed to load images.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            List<Event> allEvents = t.getResult();
            eventsWithImages.clear();

            for (Event e : allEvents) {
                if (e != null && e.getImageUrl() != null && !e.getImageUrl().isEmpty()) {
                    eventsWithImages.add(e);
                }
            }

            adapter.notifyDataSetChanged();

            if (eventsWithImages.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
            } else {
                emptyText.setVisibility(View.GONE);
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    // ===== AdminImagesAdapter.OnImageActionListener =====

    @Override
    public void onDeleteImageClicked(Event event) {
        if (event == null) return;

        String title = event.getTitle() != null ? event.getTitle() : "this event";

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete poster")
                .setMessage("Are you sure you want to delete the poster for \"" + title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deletePosterForEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the image from Firebase Storage and clears the imageUrl in Firestore.
     */
    private void deletePosterForEvent(Event event) {
        String imageUrl = event.getImageUrl();

        if (imageUrl == null || imageUrl.isEmpty()) {
            // Nothing in storage, just clear Firestore
            clearImageUrlOnEvent(event);
            return;
        }

        showLoading(true);

        StorageManager.deleteImage(imageUrl).addOnCompleteListener(deleteTask -> {
            if (!deleteTask.isSuccessful()) {
                showLoading(false);
                Toast.makeText(requireContext(),
                        "Failed to delete image from storage.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Now clear image URL in Firestore
            clearImageUrlOnEvent(event);
        });
    }

    private void clearImageUrlOnEvent(Event event) {
        event.setImageUrl(null);

        eventRepository.updateEvent(event).addOnCompleteListener(updateTask -> {
            showLoading(false);

            if (!updateTask.isSuccessful()) {
                Toast.makeText(requireContext(),
                        "Failed to update event.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Remove from local list / grid view
            int idx = eventsWithImages.indexOf(event);
            if (idx >= 0) {
                eventsWithImages.remove(idx);
                adapter.notifyItemRemoved(idx);
            } else {
                // fallback
                eventsWithImages.remove(event);
                adapter.notifyDataSetChanged();
            }

            Toast.makeText(requireContext(),
                    "Poster deleted.",
                    Toast.LENGTH_SHORT).show();

            if (eventsWithImages.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }
}
