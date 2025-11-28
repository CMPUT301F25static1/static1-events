package com.static1.fishylottery.view.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.filters.LargeTest;

import com.static1.fishylottery.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminDashboardFragmentTest {

    @Test
    public void rowsExist() {

        // Create a Test Navigation Controller
        TestNavHostController navController = new TestNavHostController(
                ApplicationProvider.getApplicationContext()
        );

        // Launch fragment
        FragmentScenario<AdminDashboardFragment> scenario =
                FragmentScenario.launchInContainer(AdminDashboardFragment.class, null, R.style.Theme_FishyLottery);

        // Set NavController on fragment
        scenario.onFragment(fragment -> {
            navController.setGraph(R.navigation.mobile_navigation); // your nav graph
            Navigation.setViewNavController(fragment.requireView(), navController);
        });

        // Check all rows are displayed
        onView(withId(R.id.row_all_events)).check(matches(isDisplayed()));
        onView(withId(R.id.row_profiles)).check(matches(isDisplayed()));
        onView(withId(R.id.row_organizers)).check(matches(isDisplayed()));
        onView(withId(R.id.row_images)).check(matches(isDisplayed()));
        onView(withId(R.id.row_notification_logs)).check(matches(isDisplayed()));
    }
}