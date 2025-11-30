package com.static1.fishylottery.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.static1.fishylottery.R;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardViewHolder> {

    private final int[] layouts = new int[] {
            R.layout.onboarding_screen_1,
            R.layout.onboarding_screen_2,
            R.layout.onboarding_screen_3
    };

    private final LayoutInflater inflater;

    public OnboardingAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public OnboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(viewType, parent, false);
        return new OnboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardViewHolder holder, int position) {
        // no binding necessary for static onboarding pages; animations triggered from Activity
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return layouts[position];
    }

    static class OnboardViewHolder extends RecyclerView.ViewHolder {
        public OnboardViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
