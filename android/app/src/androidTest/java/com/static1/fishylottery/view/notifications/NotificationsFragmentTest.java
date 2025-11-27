package com.static1.fishylottery.view.notifications;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.Button;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.AppNotification;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Date;

@RunWith(AndroidJUnit4.class)
public class NotificationsFragmentTest {

    @Test
    public void adapter_receivesOneItem() {
        FragmentScenario<NotificationsFragment> scenario =
                FragmentScenario.launchInContainer(
                        NotificationsFragment.class,
                        null,
                        android.R.style.Theme_Material_Light_NoActionBar,
                        Lifecycle.State.RESUMED
                );

        scenario.onFragment(fragment -> {
            RecyclerView rv = fragment.requireView().findViewById(R.id.rvNotifications);
            NotificationAdapter ad = (NotificationAdapter) rv.getAdapter();

            AppNotification n = new AppNotification();
            n.setId("ID1");
            n.setTitle("Hello");
            n.setMessage("World");

            ad.submit(Collections.singletonList(n));

            assertEquals(1, ad.getItemCount());
        });
    }

    @Test
    public void clickingNotification_opensDetailFragment() {
        TestNavHostController nav = new TestNavHostController(
                ApplicationProvider.getApplicationContext());

        FragmentScenario<NotificationsFragment> scenario =
                FragmentScenario.launchInContainer(
                        NotificationsFragment.class,
                        null,
                        android.R.style.Theme_Material_Light_NoActionBar,
                        Lifecycle.State.CREATED
                );

        scenario.moveToState(Lifecycle.State.STARTED);

        scenario.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), nav);
            nav.setGraph(R.navigation.mobile_navigation);

            RecyclerView rv = fragment.requireView().findViewById(R.id.rvNotifications);
            NotificationAdapter ad = (NotificationAdapter) rv.getAdapter();

            AppNotification n = new AppNotification();
            n.setId("TEST_ID");
            n.setTitle("Test Title");
            n.setMessage("Body");

            ad.submit(Collections.singletonList(n));
        });

        scenario.moveToState(Lifecycle.State.RESUMED);

        scenario.onFragment(fragment -> {
            RecyclerView rv = fragment.requireView().findViewById(R.id.rvNotifications);

            rv.measure(
                    View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
            );
            rv.layout(0, 0, 1080, 1920);

            RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(0);
            if (vh != null) vh.itemView.performClick();
        });

        assertEquals(R.id.notificationDetailFragment,
                nav.getCurrentDestination().getId());
    }

    @Test
    public void viewEventDetailsIsVisible() {
        android.os.Bundle args = new android.os.Bundle();

        AppNotification notification = new AppNotification();

        notification.setId("T1");
        notification.setTitle("Invite");
        notification.setType("invitation");
        notification.setStatus("pending");
        notification.setCreatedAt(new Date());
        notification.setMessage("Join");

        args.putSerializable("notification", notification);

        FragmentScenario<NotificationDetailFragment> scenario =
                FragmentScenario.launchInContainer(
                        NotificationDetailFragment.class,
                        args,
                        android.R.style.Theme_Material_Light_NoActionBar,
                        Lifecycle.State.RESUMED
                );

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();
            Button buttonViewEvent = root.findViewById(R.id.button_view_event);


            assertEquals(View.VISIBLE, buttonViewEvent.getVisibility());
        });

        onView(withId(R.id.tvDetailTitle)).check(matches(withText("Invite")));
        onView(withId(R.id.tvDetailMessage)).check(matches(withText("Join")));
    }
}
