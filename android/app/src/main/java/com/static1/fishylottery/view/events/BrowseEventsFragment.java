package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.repositories.EventRepository;

import java.util.List;

public class BrowseEventsFragment extends Fragment {
    private List<Event> events;
    private EventRepository eventsRepo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_browse_events, container, false);

        eventsRepo = new EventRepository();

        return view;
    }

    private void getEvents() {
        eventsRepo.fetchEvents().addOnSuccessListener(docs -> {
            for (DocumentSnapshot doc : docs.getDocuments()) {
                Event event = doc.toObject(Event.class);
            }
        });
    }
}
