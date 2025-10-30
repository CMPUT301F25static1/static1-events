package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;

public class CreateEventPosterFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_create_event_poster, container, false);

        Button nextButton = view.findViewById(R.id.button_next_preview);

        nextButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_eventPoster_to_eventPreview);
        });

        return view;
    }
}
