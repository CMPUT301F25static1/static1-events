package com.static1.fishylottery.view.profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.static1.fishylottery.R;

public class NotificationSettingsFragment extends Fragment {

    private static final String TAG = "NotifSettings";
    private static final String PREFS_NAME = "FishyLotterySettings";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private Switch switchNotification;
    private SharedPreferences sharedPreferences;
    private boolean isUpdatingProgrammatically = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME,
                android.content.Context.MODE_PRIVATE);

        // Find the switch
        switchNotification = view.findViewById(R.id.switchNotification);

        // Load saved preference
        boolean savedValue = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true);
        Log.d(TAG, "onViewCreated - Loading saved value: " + savedValue);

        isUpdatingProgrammatically = true;
        switchNotification.setChecked(savedValue);
        isUpdatingProgrammatically = false;

        // Set listener for switch changes
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingProgrammatically) {
                Log.d(TAG, "Programmatic change, ignoring");
                return;
            }

            Log.d(TAG, "User toggled switch to: " + isChecked);

            // Save the preference
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_NOTIFICATIONS, isChecked);
            boolean saveSuccess = editor.commit();

            Log.d(TAG, "Save successful: " + saveSuccess);

            // Verify it was saved
            boolean verifyValue = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true);
            Log.d(TAG, "Verified saved value: " + verifyValue);

            // Show feedback to user
            String message = isChecked
                    ? "Notifications enabled"
                    : "Notifications disabled";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload the preference when fragment resumes
        boolean savedValue = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true);
        Log.d(TAG, "onResume - Loading saved value: " + savedValue);

        isUpdatingProgrammatically = true;
        switchNotification.setChecked(savedValue);
        isUpdatingProgrammatically = false;
    }

    // Static method to check notification status from anywhere in the app
    public static boolean areNotificationsEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
                android.content.Context.MODE_PRIVATE);
        boolean value = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        Log.d(TAG, "areNotificationsEnabled called: " + value);
        return value;
    }
}