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

    /** Test 4 — click a row in NotificationsFragment → navigates to NotificationDetailFragment */
    @Test
    public void clickingNotification_opensDetailFragment() {
        final TestNavHostController navController =
                new TestNavHostController(ApplicationProvider.getApplicationContext());

        // 1) Launch in CREATED (avoid onStart side-effects)
        FragmentScenario<NotificationsFragment> scenario =
                FragmentScenario.launchInContainer(
                        NotificationsFragment.class,
                        /* args */ null,
                        android.R.style.Theme_Material_Light_NoActionBar,
                        Lifecycle.State.CREATED
                );

        // 2) Move to STARTED so the view exists
        scenario.moveToState(Lifecycle.State.STARTED);

        // 3) Attach nav + inject one item
        scenario.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), navController);
            navController.setGraph(R.navigation.mobile_navigation);

            RecyclerView rv = fragment.requireView().findViewById(R.id.rvNotifications);
            NotificationAdapter ad = (NotificationAdapter) rv.getAdapter();

            AppNotification n = new AppNotification();
            n.setId("TEST_ID");
            n.setTitle("Test Title");
            n.setMessage("Body");
            n.setType("info");
            n.setStatus("pending");

            if (ad != null) ad.submit(Collections.singletonList(n));
        });

        // 4) Now allow user interaction
        scenario.moveToState(Lifecycle.State.RESUMED);

        // 5) Click first row (ensure RV is laid out)
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

        // 6) Assert destination
        assertEquals(R.id.notificationDetailFragment,
                navController.getCurrentDestination().getId());
    }

    /** Test 5 — NotificationDetailFragment shows Accept/Decline for pending invitations */
    @Test
    public void invitationButtons_areVisible() {
        // Arguments same as the list passes
        android.os.Bundle args = new android.os.Bundle();
        args.putString("notificationId", "TEST_ID");
        args.putString("title", "Invite");
        args.putString("message", "Join us");
        args.putLong("createdAt", 0L);
        args.putString("type", "invitation");
        args.putString("status", "pending");

        FragmentScenario<NotificationDetailFragment> scenario =
                FragmentScenario.launchInContainer(
                        NotificationDetailFragment.class,
                        args,
                        android.R.style.Theme_Material_Light_NoActionBar,
                        Lifecycle.State.RESUMED
                );

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();
            View inviteActions = root.findViewById(R.id.inviteActions);
            View btnAccept = root.findViewById(R.id.btnAccept);
            View btnDecline = root.findViewById(R.id.btnDecline);

            assertEquals(View.VISIBLE, inviteActions.getVisibility());
            assertTrue(btnAccept.isShown());
            assertTrue(btnDecline.isShown());
        });
    }
}
