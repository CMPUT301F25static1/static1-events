package com.static1.fishylottery.view.events;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.repositories.FakeEventRepository;
import com.static1.fishylottery.model.repositories.IEventRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.services.FakeAuthManager;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BrowseEventsFragmentTest {
    static class TestFragmentFactory extends FragmentFactory {

        private final IEventRepository mockRepo;

        public TestFragmentFactory(IEventRepository mockRepo) {
            this.mockRepo = mockRepo;
        }

        @NonNull
        @Override
        public Fragment instantiate(@NonNull ClassLoader cl, String className) {
            if (className.equals(BrowseEventsFragment.class.getName())) {
                return new BrowseEventsFragment(mockRepo);
            }
            return super.instantiate(cl, className);
        }
    }
    private FragmentScenario<BrowseEventsFragment> launch(IEventRepository repo) {
        return FragmentScenario.launchInContainer(
                BrowseEventsFragment.class,
                null,
                R.style.Theme_FishyLottery,
                new BrowseEventsFragmentTest.TestFragmentFactory(repo)
        );
    }

    @Before
    public void setupAuth() {
        AuthManager authManager = new FakeAuthManager("user123");
        AuthManager.setInstanceForTesting(authManager);
    }

    @Test
    public void basicTest() {
        FakeEventRepository eventRepo = new FakeEventRepository();

        launch(eventRepo);

        // Todo: do regular UI testing here
        assertEquals(1, 1);
    }
}
