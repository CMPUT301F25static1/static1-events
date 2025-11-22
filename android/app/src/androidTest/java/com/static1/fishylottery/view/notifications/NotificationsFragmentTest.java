package com.static1.fishylottery.view.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.View;

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

@RunWith(AndroidJUnit4.class)
public class NotificationsFragmentTest {

    /** ✅ SIMPLE TEST 1 — Adapter receives 1 item */
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

    /** ✅ SIMPLE TEST 2 — Clicking row navigates to detail screen */
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

    /** ✅ SIMPLE TEST 3 — Invitation buttons visible */
    @Test
    public void invitationButtons_areVisible() {
        android.os.Bundle args = new android.os.Bundle();
        args.putString("notificationId", "T1");
        args.putString("title", "Invite");
        args.putString("message", "Join");
        args.putString("type", "invitation");
        args.putString("status", "pending");
        args.putLong("createdAt", 0L);

        FragmentScenario<NotificationDetailFragment> scenario =
                FragmentScenario.launchInContainer(
                        NotificationDetailFragment.class,
                        args,
                        android.R.style.Theme_Material_Light_NoActionBar,
                        Lifecycle.State.RESUMED
                );

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();
            View invite = root.findViewById(R.id.inviteActions);
            View accept = root.findViewById(R.id.btnAccept);
            View decline = root.findViewById(R.id.btnDecline);

            assertEquals(View.VISIBLE, invite.getVisibility());
            assertTrue(accept.isShown());
            assertTrue(decline.isShown());
        });
    }
}
