package com.static1.fishylottery.view.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple UI tests for AdminImagesFragment:
 * - Admin can browse images (grid visible, 2 columns)
 * - Admin can delete images (delete button visible and clickable)
 *
 * Uses a test-only subclass that does NOT touch Firebase/Firestore.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminImagesFragmentTest {

    /**
     * Test-only subclass of AdminImagesFragment.
     *
     * - Inflates the same layout
     * - Uses its own in-memory list of Events
     * - Never talks to EventRepository or StorageManager
     */
    public static class TestAdminImagesFragment extends AdminImagesFragment {

        private RecyclerView testRecyclerView;
        private AdminImagesAdapter testAdapter;
        private final List<Event> testEvents = new ArrayList<>();

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_admin_images, container, false);

            testRecyclerView = view.findViewById(R.id.recycler_admin_images);
            ProgressBar progressBar = view.findViewById(R.id.progress_admin_images);
            TextView emptyText = view.findViewById(R.id.text_admin_images_empty);

            // No loading / empty states for tests
            progressBar.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);

            // Same grid layout as real fragment (2 columns)
            int spanCount = 2;
            testRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));

            // Adapter uses in-memory list
            testAdapter = new AdminImagesAdapter(testEvents, this);
            testRecyclerView.setAdapter(testAdapter);

            return view;
        }

        /**
         * Allow tests to inject fake events.
         */
        public void setFakeEvents(List<Event> events) {
            testEvents.clear();
            testEvents.addAll(events);
            if (testAdapter != null) {
                testAdapter.notifyDataSetChanged();
            }
        }

        /**
         * Override delete handler to just show a dialog
         * (no Firebase / Storage).
         */
        @Override
        public void onDeleteImageClicked(Event event) {
            String title = (event != null && event.getTitle() != null)
                    ? event.getTitle()
                    : "this event";

            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete poster")
                    .setMessage("Are you sure you want to delete the poster for \"" + title + "\"?")
                    .setPositiveButton("Delete", null)
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        public RecyclerView getTestRecyclerView() {
            return testRecyclerView;
        }
    }

    /**
     * US 03.06.01 – Admin can browse images.
     * Check that:
     *  - The RecyclerView is displayed.
     *  - It uses a 2-column GridLayoutManager.
     */
    @Test
    public void testBrowseImages_showsGridRecyclerView() {
        FragmentScenario<TestAdminImagesFragment> scenario =
                FragmentScenario.launchInContainer(TestAdminImagesFragment.class);

        // Inject some fake events so the grid has content
        scenario.onFragment(fragment -> {
            List<Event> fakeEvents = new ArrayList<>();
            Event e1 = new Event();
            e1.setTitle("Event One");
            e1.setImageUrl("fake-url-1");

            Event e2 = new Event();
            e2.setTitle("Event Two");
            e2.setImageUrl("fake-url-2");

            fakeEvents.add(e1);
            fakeEvents.add(e2);

            fragment.setFakeEvents(fakeEvents);
        });

        // RecyclerView is visible
        onView(withId(R.id.recycler_admin_images))
                .check(matches(isDisplayed()));

        // Verify grid layout with 2 columns
        scenario.onFragment(fragment -> {
            RecyclerView rv = fragment.getTestRecyclerView();
            assertTrue(rv.getLayoutManager() instanceof GridLayoutManager);
            GridLayoutManager glm = (GridLayoutManager) rv.getLayoutManager();
            assertEquals(2, glm.getSpanCount());
        });
    }

    /**
     * US 03.03.01 – Admin can delete images.
     *
     * We keep this very simple:
     *  - RecyclerView shows at least one item with a delete button.
     *  - We can perform a click on that delete button (no crash).
     */
    @Test
    public void testDeleteImage_deleteButtonIsClickable() {
        FragmentScenario<TestAdminImagesFragment> scenario =
                FragmentScenario.launchInContainer(TestAdminImagesFragment.class);

        // Inject a single fake event
        scenario.onFragment(fragment -> {
            List<Event> fakeEvents = new ArrayList<>();
            Event e = new Event();
            e.setTitle("Deletable Event");
            e.setImageUrl("fake-url");
            fakeEvents.add(e);
            fragment.setFakeEvents(fakeEvents);
        });

        // Ensure RecyclerView is visible
        onView(withId(R.id.recycler_admin_images))
                .check(matches(isDisplayed()));

        // Click the delete button on the first item
        onView(withId(R.id.recycler_admin_images))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0, clickChildViewWithId(R.id.admin_image_delete)
                ));

        // If we reach here without an exception, the UI wiring works:
        // - admin could see an image
        // - admin could press the delete button
    }

    /**
     * Helper ViewAction to click a child view inside a RecyclerView item.
     */
    private static ViewAction clickChildViewWithId(int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return any(View.class);
            }

            @Override
            public String getDescription() {
                return "Click on a child view with given id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }
}
