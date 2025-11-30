package com.static1.fishylottery.view.events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.not;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.matcher.ViewMatchers;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.FakeEventRepository;
import com.static1.fishylottery.model.repositories.FakeWaitlistRepository;
import com.static1.fishylottery.model.repositories.IEventRepository;
import com.static1.fishylottery.model.repositories.IWaitlistRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.services.FakeAuthManager;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class MyEventsFragmentTest {
    static class TestFragmentFactory extends FragmentFactory {

        private final IEventRepository fakeEventRepository;
        private final IWaitlistRepository fakeWaitlistRepository;

        public TestFragmentFactory(IEventRepository eventRepo, IWaitlistRepository waitlistRepo) {
            this.fakeEventRepository = eventRepo;
            this.fakeWaitlistRepository = waitlistRepo;
        }

        @NonNull
        @Override
        public Fragment instantiate(@NonNull ClassLoader cl, String className) {
            if (className.equals(MyEventsFragment.class.getName())) {
                return new MyEventsFragment(fakeEventRepository, fakeWaitlistRepository);
            }
            return super.instantiate(cl, className);
        }
    }

    private FragmentScenario<MyEventsFragment> launch(
            IEventRepository eventRepo,
            IWaitlistRepository waitlistRepo
    ) {
        return FragmentScenario.launchInContainer(
                MyEventsFragment.class,
                null,
                R.style.Theme_FishyLottery,
                new MyEventsFragmentTest.TestFragmentFactory(eventRepo, waitlistRepo)
        );
    }

    @Before
    public void setupAuth() {
        AuthManager authManager = new FakeAuthManager("user123");
        AuthManager.setInstanceForTesting(authManager);
    }

    @Test
    public void recyclerViewHasCorrectNumberOfItems() {
        // Arrange: create fake repos with some events and waitlist entries
        IEventRepository fakeEventRepo = new FakeEventRepository();

        fakeEventRepo.addEvent(createFakeEvent("event1"));
        fakeEventRepo.addEvent(createFakeEvent("event2"));
        fakeEventRepo.addEvent(createFakeEvent("event3"));
        fakeEventRepo.addEvent(createFakeEvent("event4"));
        fakeEventRepo.addEvent(createFakeEvent("event5"));

        FakeWaitlistRepository fakeWaitlistRepo = createFakeWaitlist();

        // Launch the fragment
        FragmentScenario<MyEventsFragment> scenario =
                FragmentScenario.launchInContainer(
                        MyEventsFragment.class,
                        null,
                        R.style.Theme_FishyLottery,
                        new TestFragmentFactory(fakeEventRepo, fakeWaitlistRepo)
                );

        // Act & Assert: RecyclerView should show 2 items (matching waitlist entries)
        onView(withId(R.id.recycler_my_events))
                .check(matches(ViewMatchers.hasChildCount(2)));
    }

    @Test
    public void noEventsMessageIsVisibleWhenNoEvents() {
        // Arrange: fake repos with empty lists
        IEventRepository fakeEventRepo = new FakeEventRepository();
        IWaitlistRepository fakeWaitlistRepo = new FakeWaitlistRepository();

        FragmentScenario<MyEventsFragment> scenario =
                FragmentScenario.launchInContainer(
                        MyEventsFragment.class,
                        null,
                        R.style.Theme_FishyLottery,
                        new TestFragmentFactory(fakeEventRepo, fakeWaitlistRepo)
                );

        // Assert: TextView should be visible
        onView(withId(R.id.text_no_events_message))
                .check(matches(isDisplayed()));
    }

    @Test
    public void noEventsMessageIsGoneWhenThereAreEvents() {
        // Arrange: one event in the waitlist
        IEventRepository fakeEventRepo = new FakeEventRepository();
        fakeEventRepo.addEvent(createFakeEvent("event1"));

        IWaitlistRepository fakeWaitlistRepo = new FakeWaitlistRepository();
        fakeWaitlistRepo.addToWaitlist(createFakeEvent("event1"), createFakeWaitlistEntry("user123", "event1"));

        FragmentScenario<MyEventsFragment> scenario =
                FragmentScenario.launchInContainer(
                        MyEventsFragment.class,
                        null,
                        R.style.Theme_FishyLottery,
                        new TestFragmentFactory(fakeEventRepo, fakeWaitlistRepo)
                );

        // Assert: TextView should be gone
        onView(withId(R.id.text_no_events_message))
                .check(matches(not(isDisplayed())));
    }

    private FakeWaitlistRepository createFakeWaitlist() {
        FakeWaitlistRepository fakeWaitlistRepo = new FakeWaitlistRepository();

        Profile profile = new Profile();
        profile.setUid("user123");

        WaitlistEntry entry1 = new WaitlistEntry();
        entry1.setProfile(profile);
        entry1.setEventId("event1");
        entry1.setStatus("waiting");

        WaitlistEntry entry2 = new WaitlistEntry();
        entry2.setProfile(profile);
        entry2.setEventId("event2");
        entry2.setStatus("waiting");

        Event event1 = new Event();
        event1.setEventId("event1");

        Event event2 = new Event();
        event2.setEventId("event2");

        fakeWaitlistRepo.addToWaitlist(event1, entry1);
        fakeWaitlistRepo.addToWaitlist(event2, entry2);

        return fakeWaitlistRepo;
    }

    private Event createFakeEvent(String id) {
        Date next = Date.from(Instant.now().plusSeconds(3600));
        Event event = new Event();
        event.setEventId(id);
        event.setEventStartDate(next);
        event.setEventEndDate(next);
        event.setRegistrationCloses(next);
        event.setTitle("title123");
        event.setLocation("location123");
        return event;
    }

    private WaitlistEntry createFakeWaitlistEntry(String uid, String eventId) {
        WaitlistEntry entry = new WaitlistEntry();
        Profile profile = new Profile();
        profile.setUid(uid);
        entry.setProfile(profile);
        entry.setEventId(eventId);
        entry.setStatus("waiting");
        return entry;
    }
}
