package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavHostController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.static1.fishylottery.R;

public class HostedEventsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_hosted_events, container, false);

        FloatingActionButton addEvent = view.findViewById(R.id.fab_add_event);

        addEvent.setOnClickListener(v -> {
            // TODO: Open the create event stuff
            Navigation.findNavController(view).navigate(R.id.action_hostedEvents_to_createEvent);
        });

        return view;
    }
}
