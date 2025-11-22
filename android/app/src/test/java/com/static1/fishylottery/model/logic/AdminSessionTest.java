package com.static1.fishylottery.model.logic;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AdminSessionTest {

    @Before
    public void setUp() {
        // Reset before each test
        AdminSession.isLoggedIn = false;
    }

    @After
    public void tearDown() {
        // Reset after each test to avoid side effects
        AdminSession.isLoggedIn = false;
    }

    @Test
    public void testDefaultValue() {
        // By default, should be false
        assertFalse("Admin should not be logged in by default", AdminSession.isLoggedIn);
    }

    @Test
    public void testSetLoggedInTrue() {
        AdminSession.isLoggedIn = true;
        assertTrue("Admin should be logged in after setting to true", AdminSession.isLoggedIn);
    }

    @Test
    public void testSetLoggedInFalse() {
        AdminSession.isLoggedIn = true;  // set it true first
        AdminSession.isLoggedIn = false; // then false
        assertFalse("Admin should be logged out after setting to false", AdminSession.isLoggedIn);
    }
}
