package com.static1.fishylottery.repositories;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.static1.fishylottery.model.repositories.EventRepository;

public class EventRepositoryCapacityTest {

    @Test
    public void remainingSlots_zeroWhenAcceptedEqualsCapacity() {
        int remaining = EventRepository.computeRemainingSlots(1, 1);
        assertEquals(0, remaining);
    }

    @Test
    public void remainingSlots_zeroWhenAcceptedExceedsCapacity() {
        int remaining = EventRepository.computeRemainingSlots(1, 2);
        assertEquals(0, remaining);
    }

    @Test
    public void remainingSlots_positiveWhenSpaceLeft() {
        int remaining = EventRepository.computeRemainingSlots(3, 1);
        assertEquals(2, remaining);
    }

    @Test
    public void remainingSlots_zeroWhenCapacityNull() {
        int remaining = EventRepository.computeRemainingSlots(null, 0);
        assertEquals(0, remaining);
    }
}

