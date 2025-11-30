package com.static1.fishylottery.view.events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;

import com.google.android.gms.tasks.Task;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.FakeProfileRepository;
import com.static1.fishylottery.model.repositories.FakeWaitlistRepository;
import com.static1.fishylottery.model.repositories.IProfileRepository;
import com.static1.fishylottery.model.repositories.IWaitlistRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.services.FakeAuthManager;
import com.static1.fishylottery.viewmodel.EventDetailsViewModel;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDetailsFragmentTest {
    static class TestFragmentFactory extends FragmentFactory {

        private EventDetailsViewModel viewModel;

        public TestFragmentFactory(IWaitlistRepository waitlistRepository, IProfileRepository profileRepository) {
            this.viewModel = new EventDetailsViewModel(waitlistRepository, profileRepository);
        }

        @NonNull
        @Override
        public Fragment instantiate(@NonNull ClassLoader cl, String className) {
            if (className.equals(EventDetailsFragment.class.getName())) {
                return new EventDetailsFragment(this.viewModel);
            }
            return super.instantiate(cl, className);
        }
    }

    private FragmentScenario<EventDetailsFragment> launch(IWaitlistRepository waitlistRepository, IProfileRepository profileRepository, Event event) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);

        return FragmentScenario.launchInContainer(
                EventDetailsFragment.class,
                bundle,
                R.style.Theme_FishyLottery,
                new EventDetailsFragmentTest.TestFragmentFactory(waitlistRepository, profileRepository)
        );
    }

    private void launchWithEmptyRepos(Event event) {
        FakeWaitlistRepository waitlistRepo = new FakeWaitlistRepository() {
            @Override
            public Task<Void> addToWaitlistRespectingLimit(Event e, WaitlistEntry entry) {
                return null;
            }
        };
        FakeProfileRepository profileRepo = new FakeProfileRepository();
        launch(waitlistRepo, profileRepo, event);
    }

    @Before
    public void setupAuth() {
        AuthManager authManager = new FakeAuthManager("user123");
        AuthManager.setInstanceForTesting(authManager);
    }

    @Test
    public void showsCorrectEventDetails_onLaunch() {
        Event event = createTestEvent();

        String whenString = "Start: Jan 1, 2025 12:00 AM\n" +
                "End: Jan 1, 2025 1:00 AM\n" +
                "Registration closes: Jan 1, 2025 12:00 AM";

        FakeWaitlistRepository waitlistRepo = new FakeWaitlistRepository() {
            @Override
            public Task<Void> addToWaitlistRespectingLimit(Event e, WaitlistEntry entry) {
                return null;
            }
        };
        FakeProfileRepository profileRepo = new FakeProfileRepository();
        launch(waitlistRepo, profileRepo, event);

        // Tests
        onView(withId(R.id.text_title)).check(matches(withText("Test Event")));
        onView(withId(R.id.text_desc)).check(matches(withText("Test Description")));
        onView(withId(R.id.text_where)).check(matches(withText("Test Location")));
        onView(withId(R.id.text_when)).check(matches(withText(whenString)));
        onView(withId(R.id.text_hosted_by)).check(matches(withText("Test Host")));
        onView(withId(R.id.text_max_attendees)).check(matches(withText("Max Attendees: 50")));
        onView(withId(R.id.text_max_waitlist)).check(matches(withText("Max Waitlist: 100")));
    }

    @Test
    public void joinButtonDisabled_whenRegistrationClosed() {
        Event event = createTestEvent();

        Date registrationCloses = new Date(System.currentTimeMillis() - 10000);
        event.setRegistrationCloses(registrationCloses);

        // Launch the fragment to start the test with helper function
        launchWithEmptyRepos(event);

        // Tests
        onView(withId(R.id.button_join_waitlist)).check(matches(not(isEnabled())));
    }

    @Test
    public void posterImageShown_whenUrlExists() {
        Event event = createTestEvent();
        event.setImageUrl("https://example.com/image.png");
        launchWithEmptyRepos(event);

        onView(withId(R.id.image_event_poster)).check(matches(isDisplayed()));
    }

    @Test
    public void posterImageHidden_whenUrlIsNull() {
        Event event = createTestEvent();
        event.setImageUrl(null);
        launchWithEmptyRepos(event);

        onView(withId(R.id.image_event_poster)).check(matches(not(isDisplayed())));
    }

    @Test
    public void showsCorrectButtons_whenNotOnWaitlist() {
        Event event = createTestEvent();

        launchWithEmptyRepos(event);

        // Tests
        onView(withId(R.id.button_join_waitlist)).check(matches(isDisplayed()));
        onView(withId(R.id.button_leave_waitlist)).check(matches(not(isDisplayed())));
        onView(withId(R.id.button_accept_invite)).check(matches(not(isDisplayed())));
        onView(withId(R.id.button_decline_invite)).check(matches(not(isDisplayed())));
    }

    @Test
    public void showsCorrectButtons_whenOnWaitlist() {
        FakeProfileRepository fakeProfileRepository = new FakeProfileRepository();
        FakeWaitlistRepository fakeWaitlistRepository = new FakeWaitlistRepository() {
            @Override
            public Task<Void> addToWaitlistRespectingLimit(Event e, WaitlistEntry entry) {
                return null;
            }
        };
        Event event = createTestEvent();

        WaitlistEntry entry = new WaitlistEntry();
        Profile profile = new Profile();
        profile.setUid("user123");
        entry.setProfile(profile);
        entry.setStatus("waiting");
        entry.setEventId("event1");

        fakeWaitlistRepository.addToWaitlist(event, entry);
        fakeProfileRepository.addProfile(profile);

        launch(fakeWaitlistRepository, fakeProfileRepository, event);

        // Tests
        onView(withId(R.id.button_join_waitlist)).check(matches(not(isDisplayed())));
        onView(withId(R.id.button_leave_waitlist)).check(matches(isDisplayed()));
        onView(withId(R.id.button_accept_invite)).check(matches(not(isDisplayed())));
        onView(withId(R.id.button_decline_invite)).check(matches(not(isDisplayed())));
    }

    @NonNull
    private static Event createTestEvent() {
        Event event = new Event();

        event.setEventId("event1");
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setLocation("Test Location");
        event.setHostedBy("Test Host");
        event.setOrganizerId("user567");
        event.setCapacity(50);
        event.setMaxWaitlistSize(100);

        event.setRegistrationOpens(new Date());
        event.setRegistrationCloses(new Date());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2025);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        event.setEventStartDate(cal.getTime());
        event.setRegistrationCloses(cal.getTime());

        cal.set(Calendar.HOUR, 1);

        event.setEventEndDate(cal.getTime());
        return event;
    }
    private static WaitlistEntry makeEntry(String uid) {
        Profile profile = new Profile();
        profile.setUid(uid);

        WaitlistEntry entry = new WaitlistEntry();
        entry.setProfile(profile);
        entry.setStatus("waiting");
        return entry;
    }
    @Test
    public void joinButtonDisabled_whenWaitlistIsFull() {
        // Event with a waitlist limit of 2
        Event event = createTestEvent();
        event.setWaitlistLimited(true);
        event.setWaitlistLimit(2);

        // Pre-populate the fake waitlist with 2 waiting entries for this event
        Map<String, List<WaitlistEntry>> initial = new HashMap<>();
        List<WaitlistEntry> entries = new ArrayList<>();
        entries.add(makeEntry("u1"));
        entries.add(makeEntry("u2"));
        initial.put(event.getEventId(), entries);

        FakeWaitlistRepository waitlistRepo = new FakeWaitlistRepository(initial);
        FakeProfileRepository profileRepo = new FakeProfileRepository();

        // Launch EventDetailsFragment with this event + fake repos
        launch(waitlistRepo, profileRepo, event);

        // Because the waitlist is already at the limit, the Join button should be disabled
        onView(withId(R.id.button_join_waitlist)).check(matches(not(isEnabled())));
    }
    /**
     * when the current user is INVITED, the Accept + Decline buttons
     * are shown and the join/leave buttons are hidden.
     */
    @Test
    public void showsCorrectButtons_whenInvited() {
        FakeProfileRepository fakeProfileRepository = new FakeProfileRepository();
        FakeWaitlistRepository fakeWaitlistRepository = new FakeWaitlistRepository();
        Event event = createTestEvent();

        // Current user (matches FakeAuthManager above)
        Profile profile = new Profile();
        profile.setUid("user123");

        WaitlistEntry invited = new WaitlistEntry();
        invited.setProfile(profile);
        invited.setStatus("invited");
        invited.setEventId("event1");

        // Pre-populate repos so the fragment sees the user as invited
        fakeWaitlistRepository.addToWaitlist(event, invited);
        fakeProfileRepository.addProfile(profile);

        launch(fakeWaitlistRepository, fakeProfileRepository, event);

        // Join & Leave hidden
        onView(withId(R.id.button_join_waitlist)).check(matches(not(isDisplayed())));
        onView(withId(R.id.button_leave_waitlist)).check(matches(not(isDisplayed())));
        // Accept & Decline visible
        onView(withId(R.id.button_accept_invite)).check(matches(isDisplayed()));
        onView(withId(R.id.button_decline_invite)).check(matches(isDisplayed()));
    }
}
