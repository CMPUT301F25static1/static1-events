package com.static1.fishylottery.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class LocationRequirementUtilsTest {
    @Test
    public void testDistanceKm_samePoint() {
        double d = LocationRequirementUtils.distanceKm(0, 0, 0, 0);
        assertEquals(0.0, d, 0.0001);
    }

    @Test
    public void testDistanceKm_knownDistance() {
        // Approx distance between New York and Los Angeles ~3940 km
        double d = LocationRequirementUtils.distanceKm(40.7128, -74.0060, 34.0522, -118.2437);
        assertEquals(3940, d, 20); // allow a tolerance
    }

    @Test
    public void testIsWithinBoundary_inside() {
        boolean inside = LocationRequirementUtils.isWithinBoundary(
                10, 10,   // requirement
                10.01, 10.01, // user
                5 // 5 km allowed
        );
        assertTrue(inside);
    }

    @Test
    public void testIsWithinBoundary_outside() {
        boolean inside = LocationRequirementUtils.isWithinBoundary(
                10, 10,      // requirement
                10.5, 10.5,  // user
                5            // 5 km allowed
        );
        assertFalse(inside);
    }
}
