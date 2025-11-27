package com.static1.fishylottery.view.events.create;

import static com.google.common.truth.Truth.assertThat;

import android.view.View;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;

import com.static1.fishylottery.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;
import org.junit.Ignore;
import org.junit.Test;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Verifies the checkbox shows/hides the max field */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
@Ignore("Flaky under Robolectric due to FragmentScenario EmptyFragmentActivity manifest issue")

public class CreateEventDetailsWaitlistToggleRoboTest {

    @Test
    public void checkbox_toggles_maxField_visibility() {
        try (FragmentScenario<CreateEventDetailsFragment> scenario =
                     FragmentScenario.launchInContainer(
                             CreateEventDetailsFragment.class,
                             null,
                             R.style.Theme_FishyLottery,
                             (FragmentFactory) null)) {

            scenario.onFragment(f -> {
                MaterialCheckBox cb =
                        f.requireView().findViewById(R.id.checkbox_limit_waitlist);
                TextInputLayout til =
                        f.requireView().findViewById(R.id.layout_waitlist_max);

                // default hidden/disabled
                assertThat(til.getVisibility()).isEqualTo(View.GONE);
                assertThat(til.isEnabled()).isFalse();

                // check -> visible/enabled
                cb.setChecked(true);
                assertThat(til.getVisibility()).isEqualTo(View.VISIBLE);
                assertThat(til.isEnabled()).isTrue();

                // uncheck -> hidden/disabled again
                cb.setChecked(false);
                assertThat(til.getVisibility()).isEqualTo(View.GONE);
                assertThat(til.isEnabled()).isFalse();
            });
        }
    }
}
