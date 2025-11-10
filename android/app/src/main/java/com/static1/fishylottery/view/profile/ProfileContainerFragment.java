package com.static1.fishylottery.view.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.static1.fishylottery.MainApplication;
import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.ProfileController;
import com.static1.fishylottery.model.repositories.ProfileRepository;
import com.static1.fishylottery.services.AuthManager;

public class ProfileContainerFragment extends Fragment {

    private ProfileController controller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_container, container, false);

        MainApplication app = (MainApplication) requireActivity().getApplication();

        controller = new ProfileController(
                new ProfileRepository(),
                getChildFragmentManager(),
                R.id.profile_container
        );

        controller.loadInitialProfile();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public ProfileController getController() {
        return controller;
    }
}