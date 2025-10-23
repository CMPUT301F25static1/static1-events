package com.static1.fishylottery.view.events;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class EventPagerAdapter extends FragmentStateAdapter {
    public EventPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new MyEventsFragment();
            case 1: return new BrowseEventsFragment();
            default: return new HostedEventsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
