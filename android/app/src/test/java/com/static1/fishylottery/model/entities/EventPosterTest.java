package com.static1.fishylottery.model.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Simple unit tests around the Event poster (imageUrl) behaviour.
 * These tests are pure JVM tests (no Firebase, no Android).
 */
public class EventPosterTest {

    @Test
    public void newEvent_hasNullImageUrlByDefault() {
        Event event = new Event();

        // When a new event is created, there should be no poster set
        assertNull(event.getImageUrl());
    }

    @Test
    public void setPosterUrl_updatesImageUrlField() {
        Event event = new Event();

        String url = "https://example.com/poster1.png";
        event.setImageUrl(url);

        // imageUrl should now match the URL that was set
        assertEquals(url, event.getImageUrl());
    }

    @Test
    public void removePoster_clearsImageUrlField() {
        Event event = new Event();

        // Start with an existing poster
        event.setImageUrl("https://example.com/poster2.png");
        assertEquals("https://example.com/poster2.png", event.getImageUrl());

        // Removing a poster = setting imageUrl to null
        event.setImageUrl(null);

        // imageUrl should now be cleared
        assertNull(event.getImageUrl());
    }
}
