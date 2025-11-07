package com.static1.fishylottery.model.logic;

import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class JoinWaitlistRulesTest {

    @Test
    public void allows_whenDeadlineInFuture() {
        long now = 1_000L;
        long deadline = now + 1;
        assertTrue(JoinWaitlistRules.canJoin(deadline, now));
    }

    @Test
    public void allows_whenNowEqualsDeadline() {
        long now = 5_000L;
        long deadline = 5_000L;
        assertTrue(JoinWaitlistRules.canJoin(deadline, now));
    }

    @Test
    public void denies_whenPastDeadline() {
        long deadline = 10_000L;
        long now = 10_001L;
        assertFalse(JoinWaitlistRules.canJoin(deadline, now));
    }

    @Test
    public void allows_whenNoDeadline() {
        long now = System.currentTimeMillis();        // any runtime value
        assertTrue(JoinWaitlistRules.canJoin(null, now));
    }


    @Test
    public void boundary_oneMsBefore_isAllowed() {
        long now = System.currentTimeMillis();
        long deadline = now + 1;
        assertTrue(JoinWaitlistRules.canJoin(deadline, now));
    }

    @Test
    public void boundary_oneMsAfter_isDenied() {
        long now = System.currentTimeMillis();
        long deadline = now - 1;
        assertFalse(JoinWaitlistRules.canJoin(deadline, now));
    }
}

