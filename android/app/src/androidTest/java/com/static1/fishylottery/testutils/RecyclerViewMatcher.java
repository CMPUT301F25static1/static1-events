package com.static1.fishylottery.testutils;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**

 Helper to match child view inside a RecyclerView item at a given position.
 Usage:
 onView(withId(R.id.recycler)).check(matches(new RecyclerViewMatcher(R.id.recycler).atPositionOnView(0, R.id.title)));*/
public class RecyclerViewMatcher {
    private final int recyclerViewId;

    public RecyclerViewMatcher(int recyclerViewId) {
        this.recyclerViewId = recyclerViewId;
    }

    public Matcher<View> atPositionOnView(final int position, final int targetViewId) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with id: " + recyclerViewId + " at position: " + position);
            }

            @Override
            protected boolean matchesSafely(View view) {
                RecyclerView recyclerView = view.getRootView().findViewById(recyclerViewId);
                if (recyclerView == null) {
                    return false;
                }
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // item not bound yet (RecyclerView may not have laid out), try to scroll instead in test.
                    return false;
                }
                View targetView = viewHolder.itemView.findViewById(targetViewId);
                return view == targetView;
            }
        };
    }
}
