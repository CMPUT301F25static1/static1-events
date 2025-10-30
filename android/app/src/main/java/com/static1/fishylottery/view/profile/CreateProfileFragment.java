package com.static1.fishylottery.view.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.ProfileController;
import com.static1.fishylottery.model.entities.Profile;

public class CreateProfileFragment extends Fragment {
    EditText editTextFirstName, editTextLastName, editTextEmail, editTextPhone;
    Button signupButton;

    ProfileController controller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_profile, container, false);

        controller = ((ProfileContainerFragment) getParentFragment()).getController();

        editTextFirstName = view.findViewById(R.id.edit_text_first_name);
        editTextLastName = view.findViewById(R.id.edit_text_last_name);
        editTextEmail = view.findViewById(R.id.edit_text_email);
        editTextPhone = view.findViewById(R.id.edit_text_phone);

        signupButton = view.findViewById(R.id.button_sign_up);

        signupButton.setOnClickListener(v -> {
            createProfile();
        });

        return view;
    }

    private void createProfile() {
        String firstName = editTextFirstName.getText().toString();
        String lastname = editTextLastName.getText().toString();
        String email = editTextEmail.getText().toString();
        String phone = editTextPhone.getText().toString();

        Profile profile = new Profile(null, firstName, lastname, email, phone);

        controller.uploadProfile(profile, new ProfileController.ProfileUploadCallback() {
            @Override
            public void onComplete() {
                Toast.makeText(getContext(), "Created Profile!", Toast.LENGTH_SHORT).show();
                controller.loadInitialProfile();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Could not create profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
