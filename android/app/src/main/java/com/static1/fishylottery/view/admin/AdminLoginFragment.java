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
/**
 * Fragment that handles the simple admin passcode login flow.
 * <p>
 * When the correct passcode is entered, the fragment marks the admin session
 * as logged in and asks the parent {@link AdminContainerFragment} to show
 * the admin dashboard. Otherwise, it displays an error toast.
 */
public class AdminLoginFragment extends Fragment {
    private static final String ADMIN_PASSCODE = "1234";
    /**
     * Inflates the admin login layout and wires up the login button behaviour.
     * <p>
     * The login button:
     * <ul>
     *   <li>Reads the text from the passcode input field.</li>
     *   <li>Compares it against the static {@link #ADMIN_PASSCODE}.</li>
     *   <li>If it matches:
     *     <ul>
     *       <li>Sets {@code AdminSession.isLoggedIn} to {@code true}.</li>
     *       <li>Shows a success toast.</li>
     *       <li>Requests the parent {@link AdminContainerFragment} (if present)
     *           to display the dashboard.</li>
     *     </ul>
     *   </li>
     *   <li>If it does not match, shows an error toast.</li>
     * </ul>
     *
     * @param inflater           the {@link LayoutInflater} used to inflate the view
     * @param container          the optional parent view that the fragment's UI should attach to
     * @param savedInstanceState previously saved state, or {@code null}
     * @return the inflated root {@link View} for this fragment
     */
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
