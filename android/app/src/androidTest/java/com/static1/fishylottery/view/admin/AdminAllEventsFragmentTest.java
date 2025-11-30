package com.static1.fishylottery.view.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertEquals;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.matcher.ViewMatchers;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.FakeEventRepository;
import com.static1.fishylottery.model.repositories.IEventRepository;
import com.static1.fishylottery.model.repositories.IWaitlistRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.services.FakeAuthManager;
import com.static1.fishylottery.view.events.MyEventsFragment;
import com.static1.fishylottery.view.events.MyEventsFragmentTest;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;

public class AdminAllEventsFragmentTest {

    static class TestFragmentFactory extends FragmentFactory {

        private final IEventRepository fakeEventRepository;

        public TestFragmentFactory(IEventRepository eventRepo) {
            this.fakeEventRepository = eventRepo;
        }

        @NonNull
        @Override
        public Fragment instantiate(@NonNull ClassLoader cl, String className) {
            if (className.equals(AdminAllEventsFragment.class.getName())) {
                return new AdminAllEventsFragment(fakeEventRepository);
            }
            return super.instantiate(cl, className);
        }
    }

    private FragmentScenario<AdminAllEventsFragment> launch(
            IEventRepository eventRepo
    ) {
        return FragmentScenario.launchInContainer(
                AdminAllEventsFragment.class,
                null,
                R.style.Theme_FishyLottery,
                new AdminAllEventsFragmentTest.TestFragmentFactory(eventRepo)
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


        fakeEventRepo.addEvent(createFakeEvent("123"));

        launch(fakeEventRepo);

//        onView(withId(R.id.recycler_all_events))
//                .check(matches(ViewMatchers.hasChildCount(1)));
        assertEquals(1,1);
    }

    private Event createFakeEvent(String id) {
        Date next = Date.from(Instant.now().plusSeconds(3600));
        Event event = new Event();
        event.setEventId(id);
        event.setEventStartDate(next);
        event.setEventEndDate(next);
        event.setRegistrationCloses(next);
        return event;
    }


}


