package com.static1.fishylottery.model.logic;

import androidx.annotation.Nullable;

/** Registration window gating. */
public final class RegistrationWindowRules {

    private RegistrationWindowRules() {}

    /**
     * Window is active iff:
     *   (start == null or now >= start) AND (end == null or now <= end),
     * and invalid ranges (start > end) are treated as inactive.
     */
    public static boolean isActive(@Nullable Long startMillis,
                                   @Nullable Long endMillis,
                                   long nowMillis) {
        if (startMillis != null && endMillis != null && startMillis > endMillis) {
            return false; // defensive
        }
        if (startMillis != null && nowMillis < startMillis) return false;
        if (endMillis   != null && nowMillis > endMillis)   return false;
        return true;
    }
}
