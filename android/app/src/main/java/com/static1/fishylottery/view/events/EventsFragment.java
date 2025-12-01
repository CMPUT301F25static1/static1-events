package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.static1.fishylottery.R;

/**
 * Fragment that hosts a ViewPager2 with tabs for navigating between event-related fragments.
 * Displays "My Events", "Browse", and "Hosted" tabs.
 */
public class EventsFragment extends Fragment {

    /**
     * Inflates the fragment's layout and sets up the ViewPager2 with tabs.
     *
     * @param inflater           the LayoutInflater to inflate the layout
     * @param container          the parent ViewGroup
     * @param savedInstanceState the saved instance state, if any
     * @return the inflated View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);

        EventPagerAdapter adapter = new EventPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: tab.setText("My Events"); break;
                        case 1: tab.setText("Browse"); break;
                        case 2: tab.setText("Hosted"); break;
                    }
                }).attach();

        return view;
    }

    /**
     * Cleans up resources when the fragment's view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}