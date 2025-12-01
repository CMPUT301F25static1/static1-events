package com.static1.fishylottery.view.events;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter for managing fragments in a ViewPager2 to display different event-related views.
 * This adapter handles the creation of fragments for "My Events", "Browse Events", and "Hosted Events" tabs.
 */
public class EventPagerAdapter extends FragmentStateAdapter {

    /**
     * Constructs an EventPagerAdapter for the given fragment.
     *
     * @param fragment the parent fragment hosting the ViewPager2
     */
    public EventPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    /**
     * Creates a fragment for the specified position in the ViewPager2.
     *
     * @param position the position of the fragment to create (0 for MyEventsFragment, 1 for BrowseEventsFragment, 2 for HostedEventsFragment)
     * @return a new instance of the fragment corresponding to the position
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new MyEventsFragment();
            case 1: return new BrowseEventsFragment();
            default: return new HostedEventsFragment();
        }
    }

    /**
     * Returns the total number of fragments managed by this adapter.
     *
     * @return the number of fragments (3)
     */
    @Override
    public int getItemCount() {
        return 3;
    }
}
