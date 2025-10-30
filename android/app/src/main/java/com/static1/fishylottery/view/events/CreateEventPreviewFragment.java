package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.CreateEventControllerViewModel;

public class CreateEventPreviewFragment extends Fragment {

    CreateEventControllerViewModel vm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        vm = new ViewModelProvider(requireActivity()).get(CreateEventControllerViewModel.class);

        View view = inflater.inflate(R.layout.fragment_create_event_preview, container, false);

        Button button = view.findViewById(R.id.button_create_event);

        button.setOnClickListener(v -> {
            vm.submit();
            Navigation.findNavController(view).popBackStack(R.id.navigation_events, false);
        });

        return view;
    }
}
