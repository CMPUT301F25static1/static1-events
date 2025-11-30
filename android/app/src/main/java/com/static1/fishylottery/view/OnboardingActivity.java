package com.static1.fishylottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.static1.fishylottery.MainActivity;
import com.static1.fishylottery.R;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;
    private WormDotsIndicator dotsIndicator;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        dotsIndicator = findViewById(R.id.dotsIndicator);

        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        // Attach dots indicator (needs the dots dependency)
        dotsIndicator.setViewPager2(viewPager);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cur = viewPager.getCurrentItem();
                if (cur < adapter.getItemCount() - 1) {
                    viewPager.setCurrentItem(cur + 1);
                } else {
                    goToMain();
                }
            }
        });

        // When page changes update button text and play animations for visible page
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == adapter.getItemCount() - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
                playPageAnimations(position);
            }
        });

        // play animations for initial page
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                playPageAnimations(0);
            }
        });
    }

    private void playPageAnimations(int position) {
        // ViewPager2's child 0 is the internal RecyclerView
        View recyclerView = viewPager.getChildAt(0);
        if (recyclerView instanceof RecyclerView) {
            RecyclerView rv = (RecyclerView) recyclerView;
            RecyclerView.ViewHolder holder = rv.findViewHolderForAdapterPosition(position);
            if (holder != null) {
                View itemView = holder.itemView;
                View bg = itemView.findViewById(R.id.imgBackground);
                View bottomCard = itemView.findViewById(R.id.bottomCard);

                // load animations
                Animation fade = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                Animation slide = AnimationUtils.loadAnimation(this, R.anim.slide_up);

                if (bg != null) bg.startAnimation(fade);
                if (bottomCard != null) bottomCard.startAnimation(slide);
            }
        }
    }

    private void goToMain() {
        startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
        finish();
    }
}
