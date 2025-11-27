package com.static1.fishylottery.view.events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.FakeEventRepository;
import com.static1.fishylottery.model.repositories.IEventRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.services.FakeAuthManager;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class HostedEventsFragmentTest {
    static class TestFragmentFactory extends FragmentFactory {

        private final IEventRepository mockRepo;

        public TestFragmentFactory(IEventRepository mockRepo) {
            this.mockRepo = mockRepo;
        }

        @NonNull
        @Override
        public Fragment instantiate(@NonNull ClassLoader cl, String className) {
            if (className.equals(HostedEventsFragment.class.getName())) {
                return new HostedEventsFragment(mockRepo);
            }
            return super.instantiate(cl, className);
        }
    }

    private FragmentScenario<HostedEventsFragment> launch(IEventRepository repo) {
        return FragmentScenario.launchInContainer(
                HostedEventsFragment.class,
                null,
                R.style.Theme_FishyLottery,
                new TestFragmentFactory(repo)
        );
    }

    @Before
    public void setupAuth() {
        AuthManager authManager = new FakeAuthManager("user123");
        AuthManager.setInstanceForTesting(authManager);
    }

    @Test
    public void emptyList_ShowsEmptyState() {
        IEventRepository repo = new FakeEventRepository();
        launch(repo);

        onView(withId(R.id.text_no_events_message)).check(matches(isDisplayed()));
    }

    @Test
    public void singleEvent_ShowsInRecyclerView() {
        Event ev = new Event();
        ev.setTitle("Test Event");
        ev.setEventId("123");
        ev.setDescription("Description");

        IEventRepository repo = new FakeEventRepository();
        repo.addEvent(ev);

        launch(repo);

        onView(withText("Test Event")).check(matches(isDisplayed()));
    }

    @Test
    public void multipleEvents_ShowAll() {
        Event ev1 = new Event();
        ev1.setTitle("Event A");
        ev1.setEventId("id1");
        ev1.setEventStartDate(new Date());
        ev1.setEventEndDate(new Date());

        Event ev2 = new Event();
        ev2.setTitle("Event B");
        ev2.setEventId("id2");
        ev2.setEventStartDate(new Date());
        ev2.setEventEndDate(new Date());


        IEventRepository repo = new FakeEventRepository();
        repo.addEvent(ev1);
        repo.addEvent(ev2);

        launch(repo);

        onView(withText("Event A")).check(matches(isDisplayed()));
        onView(withText("Event B")).check(matches(isDisplayed()));
    }
}
