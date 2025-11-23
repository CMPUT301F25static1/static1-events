package com.static1.fishylottery.services;

import android.content.SharedPreferences;

/**
 * Helper class for notification settings.
 * Use NotificationSettingsFragment for the UI.
 */
public class NotificationSettings {

    public static final String PREFS_NAME = "FishyLotterySettings";
    public static final String KEY_NOTIFICATIONS = "notifications_enabled";

    /**
     * Check if notifications are enabled from anywhere in your app
     */
    public static boolean areNotificationsEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
                android.content.Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    /**
     * Manually set notification preference
     */
    public static void setNotificationsEnabled(android.content.Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
                android.content.Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }
}