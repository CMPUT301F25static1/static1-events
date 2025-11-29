package com.static1.fishylottery.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.static1.fishylottery.MainActivity;
import com.static1.fishylottery.R;

public class TermsActivity extends AppCompatActivity {
    private static final String PREFS_NAME  = "FishyLotterySettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        CheckBox checkBox = findViewById(R.id.checkBox);
        Button buttonAccept = findViewById(R.id.Continue);

        buttonAccept.setEnabled(false);

        checkBox.setOnCheckedChangeListener((v, isChecked) -> {
            buttonAccept.setEnabled(isChecked);
        });

        buttonAccept.setOnClickListener(v -> {
            if (!checkBox.isChecked()) {
                Toast.makeText(this, "You must accept to continue", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean("accepted_terms", true).apply();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
