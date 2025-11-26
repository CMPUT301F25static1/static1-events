package com.static1.fishylottery.model.entities;

import org.junit.Test;

import static org.junit.Assert.*;

public class EventWaitlistLimitUnitTest {

    @Test
    public void defaults_areUnlimited() {
        Event e = new Event();
        // default should behave as "unlimited"
        assertFalse(Boolean.TRUE.equals(e.getWaitlistLimited())); // passes if false or null
        assertNull(e.getWaitlistLimit());
    }

    @Test
    public void can_enable_without_value() {
        Event e = new Event();
        e.setWaitlistLimited(true);
        assertTrue(e.getWaitlistLimited());
        assertNull(e.getWaitlistLimit()); // value is optional
    }

    @Test
    public void can_set_limit_value() {
        Event e = new Event();
        e.setWaitlistLimit(10);
        assertEquals(Integer.valueOf(10), e.getWaitlistLimit());
    }
}
