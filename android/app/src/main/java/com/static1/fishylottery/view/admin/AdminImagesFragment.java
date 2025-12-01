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
 * Fragment that allows administrators to browse all event poster images and delete them.
 * <p>
 * Implements the following user stories:
 * <ul>
 *     <li><b>US 03.06.01:</b> Admin browses poster images in a grid view.</li>
 *     <li><b>US 03.03.01:</b> Admin removes an event’s poster image.</li>
 * </ul>
 * <p>
 * This screen retrieves all events from Firestore, filters those with image URLs,
 * displays them in a grid, and supports deletion from both Firebase Storage and Firestore.
 */
public class AdminImagesFragment extends Fragment implements AdminImagesAdapter.OnImageActionListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    private final List<Event> eventsWithImages = new ArrayList<>();
    private AdminImagesAdapter adapter;
    private EventRepository eventRepository;

    /**
     * Inflates the admin poster management layout, initializes UI components,
     * sets up the RecyclerView, and loads all poster images.
     *
     * @param inflater  inflater used to inflate the fragment layout
     * @param container optional parent container
     * @param savedInstanceState saved state bundle (unused)
     * @return the root view for this fragment
     */
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

    /**
     * Configures the RecyclerView to display event posters in a grid layout
     * and initializes the adapter.
     */
    private void setupRecyclerView() {
        int spanCount = 2; // grid column count
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));

        adapter = new AdminImagesAdapter(eventsWithImages, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Loads all events from Firestore, filters the ones containing image URLs,
     * and updates the grid UI. Displays loading indicators and handles failure cases.
     */
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

    /**
     * Shows or hides the loading spinner and the RecyclerView content.
     *
     * @param loading true to show the loading indicator, false to show content
     */
    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    // ===== AdminImagesAdapter.OnImageActionListener =====

    /**
     * Called when the admin presses the delete button on a poster.
     * Opens a confirmation dialog to prevent accidental deletion.
     *
     * @param event the event whose poster is being deleted
     */
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
     * Deletes an event poster from Firebase Storage and then clears
     * its associated imageUrl field from Firestore.
     *
     * @param event the event whose poster should be deleted
     */
    private void deletePosterForEvent(Event event) {
        String imageUrl = event.getImageUrl();

        if (imageUrl == null || imageUrl.isEmpty()) {
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

            clearImageUrlOnEvent(event);
        });
    }

    /**
     * Clears the image URL reference from the event in Firestore
     * and updates the local UI by removing the poster from the list.
     *
     * @param event the event whose image URL is being cleared
     */
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

            int idx = eventsWithImages.indexOf(event);
            if (idx >= 0) {
                eventsWithImages.remove(idx);
                adapter.notifyItemRemoved(idx);
            } else {
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
