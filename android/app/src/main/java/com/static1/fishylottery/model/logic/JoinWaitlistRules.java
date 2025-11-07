package com.static1.fishylottery.model.logic;

import androidx.annotation.Nullable;

public final class JoinWaitlistRules {
    private JoinWaitlistRules() {}

    /**
     * Returns true if joining is allowed. We allow join when there is no deadline,
     * or when now <= deadline. We disallow when now is after the deadline.
     *
     * @param deadlineMillis nullable deadline in epoch millis (null => no deadline)
     * @param nowMillis      current time in epoch millis
     */
    public static boolean canJoin(@Nullable Long deadlineMillis, long nowMillis) {
        if (deadlineMillis == null) return true;
        return nowMillis <= deadlineMillis;
    }
}


