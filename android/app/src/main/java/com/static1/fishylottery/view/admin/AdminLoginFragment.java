package com.static1.fishylottery.view.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.logic.AdminSession;

public class AdminLoginFragment extends Fragment {
    private static final String ADMIN_PASSCODE = "1234";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_login, container, false);

        EditText passcodeInput = view.findViewById(R.id.edit_text_passcode);
        Button loginButton = view.findViewById(R.id.button_login);

        loginButton.setOnClickListener(v -> {
            String input = passcodeInput.getText().toString().trim();
            if (input.equals(ADMIN_PASSCODE)) {
                AdminSession.isLoggedIn = true;
                Toast.makeText(getContext(), "Access granted", Toast.LENGTH_SHORT).show();

                // Switch to dashboard
                if (getParentFragment() instanceof AdminContainerFragment) {
                    ((AdminContainerFragment) getParentFragment()).showDashboard();
                }
            } else {
                Toast.makeText(getContext(), "Incorrect passcode", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
