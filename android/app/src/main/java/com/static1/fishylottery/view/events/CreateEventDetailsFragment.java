package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.CreateEventControllerViewModel;

public class CreateEventDetailsFragment extends Fragment {

    private EditText editTextTitle, editTextDescription;
    private CreateEventControllerViewModel vm;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_create_event_details, container, false);

        vm = new ViewModelProvider(requireActivity()).get(CreateEventControllerViewModel.class);

        Button nextButton = view.findViewById(R.id.button_next_poster);
        editTextTitle = view.findViewById(R.id.edit_text_event_title);
        editTextDescription = view.findViewById(R.id.edit_text_event_description);

        nextButton.setOnClickListener(v -> {
            updateDetails();
            Navigation.findNavController(view).navigate(R.id.action_eventDetails_to_eventPoster);
        });


        return view;
    }

    private void updateDetails() {
        vm.updateEventDetails(editTextTitle.getText().toString(), editTextDescription.getText().toString());
    }
}
