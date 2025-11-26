package com.static1.fishylottery.view.events.create;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * - runs on the JVM with Robolectric (src/test)
 * - uses a mocked NavController
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class CreateEventDetailsNavigationRoboTest {

    @Test
    public void clickingNext_navigatesToPoster() {
        // Launch the real fragment layout in a container, with app theme
        FragmentScenario<CreateEventDetailsFragment> scenario =
                FragmentScenario.launchInContainer(
                        CreateEventDetailsFragment.class,
                        new Bundle(),
                        R.style.Theme_FishyLottery,
                        (FragmentFactory) null
                );

        // Install a mocked NavController and click the button
        scenario.onFragment(fragment -> {
            NavController nav = mock(NavController.class);
            Navigation.setViewNavController(fragment.requireView(), nav);

            fragment.requireView()
                    .findViewById(R.id.button_next_poster)
                    .performClick();

            // Verify the exact action ID the fragment calls
            verify(nav).navigate(R.id.action_eventDetails_to_eventPoster);
        });
    }
}
