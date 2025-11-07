package com.static1.fishylottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.testfakes.FakeEventRepository;
import com.static1.fishylottery.testutils.RecyclerViewMatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UI test that replaces the EventRepository with a fake one and asserts the
 * BrowseEventsFragment shows the data.
 */
@RunWith(AndroidJUnit4.class)
public class BrowseEventsFragmentTest {

    @Before
    public void setUp() {
        // Build deterministic fake data
        List<Event> list = new ArrayList<>();
        Event e1 = new Event();
        e1.setTitle("Test Event 1");
        e1.setLocation("Community Pool");
        e1.setEventStartDate(new Date());
        list.add(e1);

        Event e2 = new Event();
        e2.setTitle("Dance Class");
        e2.setLocation("Rec Hall");
        e2.setEventStartDate(new Date());
        list.add(e2);

        // Inject fake repo into ServiceLocator before launching Activity
        FakeEventRepository fakeRepo = new FakeEventRepository(list);
        ServiceLocator.setEventRepository(fakeRepo);
    }

    @After
    public void tearDown() {
        // Reset ServiceLocator so other tests are not affected
        ServiceLocator.reset();
    }

    @Test
    public void browseEvents_showsFakeEvents() {
        // Launch main activity which hosts the BrowseEventsFragment via nav graph
        ActivityScenario.launch(MainActivity.class);

        // Ensure recycler is visible
        onView(withId(R.id.recycler_browse_events)).check(matches(isDisplayed()));

        // Scroll to position 0 (binds the view)
        onView(withId(R.id.recycler_browse_events))
                .perform(RecyclerViewActions.scrollToPosition(0));

        // Check that first item's title matches our fake event
        onView(new RecyclerViewMatcher(R.id.recycler_browse_events)
                .atPositionOnView(0, R.id.event_title))
                .check(matches(withText("Test Event 1")));

        // Optionally click the item to exercise click handling (navigates to details)
        onView(withId(R.id.recycler_browse_events))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }
}
