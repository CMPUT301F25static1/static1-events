package com.static1.fishylottery.view.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.repositories.IEventRepository;
import com.static1.fishylottery.model.repositories.IProfileRepository;
import com.static1.fishylottery.view.admin.AdminOrganizerAdapter.OrganizerInfo;
import com.static1.fishylottery.viewmodel.AdminOrganizersViewModel;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminOrganizersFragmentTest {

    /**
     * Custom FragmentFactory to inject a ViewModel with fake repositories
     */
    static class TestFragmentFactory extends FragmentFactory {
        private final AdminOrganizersViewModel viewModel;

        public TestFragmentFactory(IEventRepository eventRepository, IProfileRepository profileRepository) {
            this.viewModel = new AdminOrganizersViewModel(eventRepository, profileRepository);
        }

        @NonNull
        @Override
        public Fragment instantiate(@NonNull ClassLoader cl, String className) {
            if (className.equals(AdminOrganizersFragment.class.getName())) {
                return new AdminOrganizersFragment(this.viewModel);
            }
            return super.instantiate(cl, className);
        }
    }

    /**
     * Fake EventRepository for testing - implements ALL interface methods
     */
    static class FakeEventRepository implements IEventRepository {
        private final List<Event> events;

        public FakeEventRepository(List<Event> events) {
            this.events = new ArrayList<>(events);
        }

        @Override
        public Task<Event> addEvent(Event event) {
            events.add(event);
            return Tasks.forResult(event);
        }

        @Override
        public Task<Void> updateEvent(Event event) {
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).getEventId().equals(event.getEventId())) {
                    events.set(i, event);
                    break;
                }
            }
            return Tasks.forResult(null);
        }

        @Override
        public Task<Void> deleteEvent(Event event) {
            events.remove(event);
            return Tasks.forResult(null);
        }

        @Override
        public Task<Event> getEventById(String eventId) {
            for (Event e : events) {
                if (e.getEventId().equals(eventId)) {
                    return Tasks.forResult(e);
                }
            }
            return Tasks.forResult(null);
        }

        @Override
        public Task<List<Event>> fetchAllEvents() {
            return Tasks.forResult(new ArrayList<>(events));
        }

        @Override
        public Task<List<Event>> fetchEventsByOrganizerId(String uid) {
            List<Event> filtered = new ArrayList<>();
            for (Event event : events) {
                if (uid.equals(event.getOrganizerId())) {
                    filtered.add(event);
                }
            }
            return Tasks.forResult(filtered);
        }

        @Override
        public Task<Void> drawEntrants(String eventId) {
            return Tasks.forResult(null);
        }

        @Override
        public Task<List<String>> fetchCancelledEntrantIds(String eventId) {
            return Tasks.forResult(new ArrayList<>());
        }

        @Override
        public Task<Void> cancelSelectedEntrant(String eventId, String profileId) {
            return Tasks.forResult(null);
        }
    }

    /**
     * Fake ProfileRepository for testing - implements ALL interface methods
     */
    static class FakeProfileRepository implements IProfileRepository {
        private final List<Profile> profiles;

        public FakeProfileRepository(List<Profile> profiles) {
            this.profiles = new ArrayList<>(profiles);
        }

        @Override
        public Task<Void> addProfile(Profile profile) {
            profiles.add(profile);
            return Tasks.forResult(null);
        }

        @Override
        public Task<Void> updateProfile(Profile profile) {
            for (int i = 0; i < profiles.size(); i++) {
                if (profiles.get(i).getUid().equals(profile.getUid())) {
                    profiles.set(i, profile);
                    break;
                }
            }
            return Tasks.forResult(null);
        }

        @Override
        public Task<Void> deleteProfile(Profile profile) {
            profiles.remove(profile);
            return Tasks.forResult(null);
        }

        @Override
        public Task<List<Profile>> getAllProfiles() {
            return Tasks.forResult(new ArrayList<>(profiles));
        }

        @Override
        public Task<Profile> getProfileById(String uid) {
            for (Profile p : profiles) {
                if (p.getUid().equals(uid)) {
                    return Tasks.forResult(p);
                }
            }
            return Tasks.forResult(null);
        }

        @Override
        public Task<List<Profile>> fetchProfilesByIds(List<String> uids) {
            List<Profile> result = new ArrayList<>();
            for (Profile p : profiles) {
                if (uids.contains(p.getUid())) {
                    result.add(p);
                }
            }
            return Tasks.forResult(result);
        }
    }

    /**
     * Helper method to create test events
     */
    private List<Event> createTestEvents() {
        List<Event> events = new ArrayList<>();

        // Organizer 1 has 3 events
        Event event1 = new Event();
        event1.setEventId("event1");
        event1.setOrganizerId("org1");
        event1.setTitle("Summer Festival");
        events.add(event1);

        Event event2 = new Event();
        event2.setEventId("event2");
        event2.setOrganizerId("org1");
        event2.setTitle("Spring Concert");
        events.add(event2);

        Event event3 = new Event();
        event3.setEventId("event3");
        event3.setOrganizerId("org1");
        event3.setTitle("Fall Gala");
        events.add(event3);

        // Organizer 2 has 2 events
        Event event4 = new Event();
        event4.setEventId("event4");
        event4.setOrganizerId("org2");
        event4.setTitle("Tech Conference");
        events.add(event4);

        Event event5 = new Event();
        event5.setEventId("event5");
        event5.setOrganizerId("org2");
        event5.setTitle("Workshop");
        events.add(event5);

        // Organizer 3 has 1 event
        Event event6 = new Event();
        event6.setEventId("event6");
        event6.setOrganizerId("org3");
        event6.setTitle("Charity Run");
        events.add(event6);

        return events;
    }

    /**
     * Helper method to create test profiles (organizers)
     */
    private List<Profile> createTestProfiles() {
        List<Profile> profiles = new ArrayList<>();

        profiles.add(new Profile(
                "org1",
                "Bob",
                "Organizer",
                "bob@events.com",
                "1111111111"
        ));

        profiles.add(new Profile(
                "org2",
                "Alice",
                "EventPlanner",
                "alice@events.com",
                "2222222222"
        ));

        profiles.add(new Profile(
                "org3",
                "Charlie",
                "Host",
                "charlie@events.com",
                "3333333333"
        ));

        return profiles;
    }

    /**
     * Helper method to launch fragment with fake data
     */
    private FragmentScenario<AdminOrganizersFragment> launchWithData(
            List<Event> events, List<Profile> profiles) {
        IEventRepository eventRepo = new FakeEventRepository(events);
        IProfileRepository profileRepo = new FakeProfileRepository(profiles);

        return FragmentScenario.launchInContainer(
                AdminOrganizersFragment.class,
                null,
                R.style.Theme_FishyLottery,
                new TestFragmentFactory(eventRepo, profileRepo)
        );
    }

    @Test
    public void displaysOrganizerList_whenOrganizersExist() {
        // Arrange
        List<Event> events = createTestEvents();
        List<Profile> profiles = createTestProfiles();

        // Act
        launchWithData(events, profiles);

        // Assert - Check organizer names are displayed
        onView(withText("Bob Organizer")).check(matches(isDisplayed()));
        onView(withText("Alice EventPlanner")).check(matches(isDisplayed()));
        onView(withText("Charlie Host")).check(matches(isDisplayed()));
    }

    @Test
    public void displaysCorrectEventCounts() {
        // Arrange
        List<Event> events = createTestEvents();
        List<Profile> profiles = createTestProfiles();

        // Act
        launchWithData(events, profiles);

        // Assert - Check event counts
        onView(withText("Events: 3")).check(matches(isDisplayed())); // Bob has 3 events
        onView(withText("Events: 2")).check(matches(isDisplayed())); // Alice has 2 events
        onView(withText("Events: 1")).check(matches(isDisplayed())); // Charlie has 1 event
    }

    @Test
    public void displaysOrganizerEmails() {
        // Arrange
        List<Event> events = createTestEvents();
        List<Profile> profiles = createTestProfiles();

        // Act
        launchWithData(events, profiles);

        // Assert - Check emails are displayed
        onView(withText("bob@events.com")).check(matches(isDisplayed()));
        onView(withText("alice@events.com")).check(matches(isDisplayed()));
        onView(withText("charlie@events.com")).check(matches(isDisplayed()));
    }

    @Test
    public void displaysEmptyMessage_whenNoOrganizers() {
        // Arrange
        List<Event> emptyEvents = new ArrayList<>();
        List<Profile> emptyProfiles = new ArrayList<>();

        // Act
        launchWithData(emptyEvents, emptyProfiles);

        // Assert
        onView(withId(R.id.text_empty)).check(matches(isDisplayed()));
        onView(withId(R.id.text_empty)).check(matches(withText("No organizers found")));
    }

    @Test
    public void recyclerViewVisible_whenOrganizersExist() {
        // Arrange
        List<Event> events = createTestEvents();
        List<Profile> profiles = createTestProfiles();

        // Act
        launchWithData(events, profiles);

        // Assert
        onView(withId(R.id.recycler_organizers)).check(matches(isDisplayed()));
        onView(withId(R.id.text_empty)).check(matches(not(isDisplayed())));
    }

    @Test
    public void displaysRemoveButton_forEachOrganizer() {
        // Arrange
        List<Event> events = createTestEvents();
        List<Profile> profiles = createTestProfiles();

        // Act
        launchWithData(events, profiles);

        // Assert - Check that remove buttons exist
        onView(withId(R.id.recycler_organizers))
                .check(matches(hasDescendant(withId(R.id.button_remove))));
    }

    @Test
    public void displaysSingleOrganizer_correctly() {
        // Arrange
        List<Event> events = new ArrayList<>();
        Event event = new Event();
        event.setEventId("event1");
        event.setOrganizerId("org1");
        event.setTitle("Solo Event");
        events.add(event);

        List<Profile> profiles = new ArrayList<>();
        profiles.add(new Profile(
                "org1",
                "Solo",
                "Organizer",
                "solo@events.com",
                "5555555555"
        ));

        // Act
        launchWithData(events, profiles);

        // Assert
        onView(withText("Solo Organizer")).check(matches(isDisplayed()));
        onView(withText("solo@events.com")).check(matches(isDisplayed()));
        onView(withText("Events: 1")).check(matches(isDisplayed()));
    }

    @Test
    public void countsMultipleEvents_forSameOrganizer() {
        // Arrange
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Event event = new Event();
            event.setEventId("event" + i);
            event.setOrganizerId("org1");
            event.setTitle("Event " + i);
            events.add(event);
        }

        List<Profile> profiles = new ArrayList<>();
        profiles.add(new Profile(
                "org1",
                "Busy",
                "Organizer",
                "busy@events.com",
                "9999999999"
        ));

        // Act
        launchWithData(events, profiles);

        // Assert
        onView(withText("Events: 5")).check(matches(isDisplayed()));
    }
}
