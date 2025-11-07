package com.static1.fishylottery.view.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.viewmodel.ProfileViewModel;

public class EditProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText phone;
    private TextView deleteProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        firstName = view.findViewById(R.id.first_name);
        lastName = view.findViewById(R.id.last_name);
        email = view.findViewById(R.id.email);
        phone = view.findViewById(R.id.phone);
        deleteProfile = view.findViewById(R.id.button_delete_profile);
        TextView textInitials = view.findViewById(R.id.text_initials);

        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                firstName.setText(profile.getFirstName());
                lastName.setText(profile.getLastName());
                email.setText(profile.getEmail());
                phone.setText(profile.getPhone());
                textInitials.setText(profile.getInitials());
            }
        });

        Button saveButton = view.findViewById(R.id.button_save_profile);

        saveButton.setOnClickListener(v -> {
            saveProfile();
        });

        deleteProfile.setOnClickListener(v -> {
            deleteProfile();
        });

        return view;
    }

    private void saveProfile() {
        String f = firstName.getText().toString().trim();
        String l = lastName.getText().toString().trim();
        String e = email.getText().toString().trim();
        String p = phone.getText().toString().trim();

        if (TextUtils.isEmpty(f) || TextUtils.isEmpty(l) || TextUtils.isEmpty(e)) {
            Toast.makeText(requireContext(), "Missing required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.updateProfile(f, l, e, p)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Profile updated.", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .addOnFailureListener(e2 ->
                        Toast.makeText(requireContext(), "Failed: " + e2.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteProfile() {
        viewModel.deleteProfile()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Profile deleted.", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
