package com.static1.fishylottery;

import com.static1.fishylottery.model.repositories.EventRepository;

/**

 Very small service locator to allow tests to swap repositories.*/
public class ServiceLocator {
    private static EventRepository eventRepository;

    public static EventRepository getEventRepository() {
        if (eventRepository == null) {
            eventRepository = new EventRepository();
        }
        return eventRepository;
    }

    public static void setEventRepository(EventRepository repo) {
        eventRepository = repo;
    }

    public static void reset() {
        eventRepository = null;
    }
}