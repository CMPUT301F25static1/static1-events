package com.static1.fishylottery.model.logic;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegistrationWindowRulesTest {

    private static long anchor() { return 1_000_000L; } // not a compile-time constant

    @Test public void active_whenNowBetweenStartAndEnd() {
        long base = anchor();
        long start = base;
        long end   = base + 100;
        long now   = base + 50;

        boolean active = RegistrationWindowRules.isActive(start, end, now);
        assertTrue(active);
    }

    @Test public void active_whenNowEqualsStart_isInclusive() {
        long base = anchor();
        long start = base;
        long end   = base + 100;
        long now   = base;

        boolean active = RegistrationWindowRules.isActive(start, end, now);
        assertTrue(active);
    }

    @Test public void active_whenNowEqualsEnd_isInclusive() {
        long base = anchor();
        long start = base;
        long end   = base + 100;
        long now   = base + 100;

        boolean active = RegistrationWindowRules.isActive(start, end, now);
        assertTrue(active);
    }

    @Test public void inactive_whenNowBeforeStart() {
        long base = anchor();
        long start = base + 10;
        long end   = base + 100;
        long now   = base + 9;

        boolean active = RegistrationWindowRules.isActive(start, end, now);
        assertFalse(active);
    }

    @Test public void inactive_whenNowAfterEnd() {
        long base = anchor();
        long start = base;
        long end   = base + 100;
        long now   = base + 101;

        boolean active = RegistrationWindowRules.isActive(start, end, now);
        assertFalse(active);
    }

    @Test public void active_whenNoStart_openStart() {
        long base = anchor();
        Long start = null;
        long end   = base + 100;
        long now   = base - 50; // before 'end' but no start gate

        boolean active = RegistrationWindowRules.isActive(start, end, now);
        assertTrue(active);
    }

    @Test public void active_whenNoEnd_openEnd() {
        long base = anchor();
        long start = base;
        Long end   = null;
        long now   = base + 5_000;

        boolean active = RegistrationWindowRules.isActive(start, end, now);
        assertTrue(active);
    }

    @Test public void inactive_whenStartAfterEnd_invalidRange() {
        long base = anchor();
        long start = base + 100;
        long end   = base;       // start > end
        long now   = base + 50;

        boolean active = RegistrationWindowRules.isActive(start, end, now);
        assertFalse(active);
    }
}
