package com.static1.fishylottery.testfakes;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;

import java.util.List;

/**

 Fake repository for tests. Returns a pre-provided list synchronously.*/
public class FakeEventRepository extends EventRepository {
    private final List<Event> events;

    public FakeEventRepository(List<Event> events) {
        this.events = events;
    }

    @Override
    public Task<List<Event>> fetchAllEvents() {
        return Tasks.forResult(events);
    }
}
