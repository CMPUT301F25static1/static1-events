package com.static1.fishylottery.view.events.hosted;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.matcher.ViewMatchers;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.FakeWaitlistRepository;
import com.static1.fishylottery.model.repositories.IWaitlistRepository;

import org.junit.Test;

public class CancelledEntrantsFragmentTest {

    /**
     * Custom FragmentFactory to inject a fake IEventRepository.
     */
    static class TestFragmentFactory extends FragmentFactory {

        private final IWaitlistRepository repo;

        TestFragmentFactory(IWaitlistRepository repo) {
            this.repo = repo;
        }

        @NonNull
        @Override
        public Fragment instantiate(@NonNull ClassLoader cl, @NonNull String className) {
            if (className.equals(CancelledEntrantsFragment.class.getName())) {
                return new CancelledEntrantsFragment(repo);
            }
            return super.instantiate(cl, className);
        }
    }

    private FragmentScenario<CancelledEntrantsFragment> launchWith(
            IWaitlistRepository repo,
            Event event
    ) {
        Bundle args = new Bundle();
        args.putSerializable("event", event);

        return FragmentScenario.launchInContainer(
                CancelledEntrantsFragment.class,
                args,
                R.style.Theme_FishyLottery,
                new TestFragmentFactory(repo)
        );
    }

    private static Event createTestEvent(String id) {
        Event e = new Event();
        e.setEventId(id);
        e.setTitle("Test Event");
        return e;
    }

    @Test
    public void showsCancelled_whenRepositoryReturnsData() {
        FakeWaitlistRepository fakeWaitlistRepository = new FakeWaitlistRepository();
        Event event = createTestEvent("event1");
        WaitlistEntry entry = new WaitlistEntry();
        entry.setEventId("event1");
        entry.setStatus("cancelled");
        fakeWaitlistRepository.addToWaitlist(event, entry);

        launchWith(fakeWaitlistRepository, event);

        // empty message hidden
        onView(withId(R.id.text_empty)).check(matches(not(isDisplayed())));
        // list is shown and contains both IDs
        onView(withId(R.id.recycler_cancelled_entrants)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_cancelled_entrants))
                .check(matches(ViewMatchers.hasChildCount(1)));
    }

    @Test
    public void showsEmptyMessage_whenNoCancelledEntrants() {
        FakeWaitlistRepository fakeWaitlistRepository = new FakeWaitlistRepository();
        Event event = createTestEvent("event2");
        // no cancelled IDs set for this event

        launchWith(fakeWaitlistRepository, event);

        // Empty text visible when there are no items
        onView(withId(R.id.text_empty)).check(matches(isDisplayed()));
    }
}

