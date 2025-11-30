package com.static1.fishylottery.view.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.repositories.IProfileRepository;
import com.static1.fishylottery.viewmodel.AdminProfilesViewModel;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminProfilesFragmentTest {

    /**
     * Custom FragmentFactory to inject a ViewModel with fake repository
     */
    static class TestFragmentFactory extends FragmentFactory {
        private final AdminProfilesViewModel viewModel;

        public TestFragmentFactory(IProfileRepository profileRepository) {
            this.viewModel = new AdminProfilesViewModel(profileRepository);
        }

        @NonNull
        @Override
        public Fragment instantiate(@NonNull ClassLoader cl, String className) {
            if (className.equals(AdminProfilesFragment.class.getName())) {
                return new AdminProfilesFragment(this.viewModel);
            }
            return super.instantiate(cl, className);
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
     * Helper method to create test profiles
     */
    private List<Profile> createTestProfiles() {
        List<Profile> profiles = new ArrayList<>();

        profiles.add(new Profile(
                "user1",
                "John",
                "Doe",
                "john.doe@example.com",
                "1234567890"
        ));

        profiles.add(new Profile(
                "user2",
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "0987654321"
        ));

        profiles.add(new Profile(
                "user3",
                "Alice",
                "Johnson",
                "alice.johnson@example.com",
                "5555555555"
        ));

        return profiles;
    }

    /**
     * Helper method to launch fragment with fake data
     */
    private FragmentScenario<AdminProfilesFragment> launchWithProfiles(List<Profile> profiles) {
        IProfileRepository fakeRepo = new FakeProfileRepository(profiles);

        return FragmentScenario.launchInContainer(
                AdminProfilesFragment.class,
                null,
                R.style.Theme_FishyLottery,
                new TestFragmentFactory(fakeRepo)
        );
    }

    @Test
    public void displaysProfileList_whenProfilesExist() {
        // Arrange
        List<Profile> testProfiles = createTestProfiles();

        // Act
        launchWithProfiles(testProfiles);

        // Assert - Check that profile data is displayed
        onView(withText("John Doe")).check(matches(isDisplayed()));
        onView(withText("john.doe@example.com")).check(matches(isDisplayed()));
        onView(withText("(123) 456-7890")).check(matches(isDisplayed()));

        onView(withText("Jane Smith")).check(matches(isDisplayed()));
        onView(withText("jane.smith@example.com")).check(matches(isDisplayed()));

        onView(withText("Alice Johnson")).check(matches(isDisplayed()));
        onView(withText("alice.johnson@example.com")).check(matches(isDisplayed()));
    }

    @Test
    public void displaysInitials_correctly() {
        // Arrange
        List<Profile> testProfiles = createTestProfiles();

        // Act
        launchWithProfiles(testProfiles);

        // Assert - Check initials are displayed
        onView(withText("JD")).check(matches(isDisplayed())); // John Doe
        onView(withText("JS")).check(matches(isDisplayed())); // Jane Smith
        onView(withText("AJ")).check(matches(isDisplayed())); // Alice Johnson
    }

    @Test
    public void displaysEmptyMessage_whenNoProfiles() {
        // Arrange
        List<Profile> emptyProfiles = new ArrayList<>();

        // Act
        launchWithProfiles(emptyProfiles);

        // Assert
        onView(withId(R.id.text_empty)).check(matches(isDisplayed()));
        onView(withId(R.id.text_empty)).check(matches(withText("No profiles found")));
    }

    @Test
    public void recyclerViewVisible_whenProfilesExist() {
        // Arrange
        List<Profile> testProfiles = createTestProfiles();

        // Act
        launchWithProfiles(testProfiles);

        // Assert
        onView(withId(R.id.recycler_view_all_profiles)).check(matches(isDisplayed()));
        onView(withId(R.id.text_empty)).check(matches(not(isDisplayed())));
    }

    @Test
    public void displaysDeleteButton_forEachProfile() {
        // Arrange
        List<Profile> testProfiles = createTestProfiles();

        // Act
        launchWithProfiles(testProfiles);

        // Assert - RecyclerView should display and have items with delete buttons
        onView(withId(R.id.recycler_view_all_profiles)).check(matches(isDisplayed()));

        // Check that profile items exist (we can't easily count RecyclerView items with basic Espresso,
        // but we can verify the structure exists)
        onView(withId(R.id.recycler_view_all_profiles))
                .check(matches(hasDescendant(withId(R.id.button_profile_delete))));
    }

    @Test
    public void formatsPhoneNumber_correctly() {
        // Arrange
        List<Profile> profiles = new ArrayList<>();
        profiles.add(new Profile(
                "user1",
                "Test",
                "User",
                "test@example.com",
                "5551234567"
        ));

        // Act
        launchWithProfiles(profiles);

        // Assert - Phone should be formatted as (555) 123-4567
        onView(withText("(555) 123-4567")).check(matches(isDisplayed()));
    }

    @Test
    public void displaysSingleProfile_correctly() {
        // Arrange
        List<Profile> profiles = new ArrayList<>();
        profiles.add(new Profile(
                "user1",
                "Solo",
                "Profile",
                "solo@example.com",
                "1112223333"
        ));

        // Act
        launchWithProfiles(profiles);

        // Assert
        onView(withText("Solo Profile")).check(matches(isDisplayed()));
        onView(withText("solo@example.com")).check(matches(isDisplayed()));
        onView(withText("(111) 222-3333")).check(matches(isDisplayed()));
        onView(withText("SP")).check(matches(isDisplayed())); // Initials
    }
}