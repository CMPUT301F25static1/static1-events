package com.static1.fishylottery.view.events.hosted;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.viewmodel.SendNotificationsViewModel;
/**
 * Fragment that lets an organizer send custom notifications to different
 * subsets of entrants for a hosted event.
 *
 * <p>The organizer can compose a title and message, choose an audience
 * (everyone, accepted, selected, waitlist, or cancelled entrants), and trigger
 * sending via the {@link SendNotificationsViewModel}.</p>
 */
public class SendNotificationsFragment extends Fragment {
    private SendNotificationsViewModel vm;
    private Event event;
    /**
     * Inflates the notification composition layout.
     *
     * <p>The actual view wiring and observers are set up in
     * {@link #onViewCreated(View, Bundle)}.</p>
     *
     * @return the root view of the send notifications screen
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send_notifications, container, false);
    }
    /**
     * Initializes the view model, extracts the event argument, and wires up the UI.
     *
     * <p>This method:
     * <ul>
     *     <li>Retrieves the {@link Event} to send notifications for.</li>
     *     <li>Sets up radio buttons for selecting the audience.</li>
     *     <li>Observes error/success messages from the view model and shows toasts.</li>
     *     <li>Sends the composed notification when the user taps the send button,
     *     and navigates back on success.</li>
     * </ul>
     * </p>
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        vm = new ViewModelProvider(this).get(SendNotificationsViewModel.class);

        if (getArguments() != null) {
            Event event = (Event) getArguments().getSerializable("event");

            if (event != null) {
                this.event = event;
            }
        }

        RadioGroup rg = v.findViewById(R.id.rgAudience);
        RadioButton rbEveryone = v.findViewById(R.id.rbEverone);
        RadioButton rbAccepted = v.findViewById(R.id.rbAccepted);
        RadioButton rbSelected = v.findViewById(R.id.rbSelected);
        RadioButton rbWait = v.findViewById(R.id.rbWaitlist);
        RadioButton rbCancel = v.findViewById(R.id.rbCancelled);
        EditText etTitle = v.findViewById(R.id.etTitle);
        EditText etMsg = v.findViewById(R.id.etMessage);
        Button btnSend = v.findViewById(R.id.btnSend);

        rbEveryone.setChecked(true); // default

        vm.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
        });
        vm.getSuccess().observe(getViewLifecycleOwner(), ok -> {
            if (ok != null && ok) Toast.makeText(requireContext(), "Sent!", Toast.LENGTH_SHORT).show();
        });

        btnSend.setOnClickListener(view -> {
            if (event == null) {
                return;
            }

            String title = etTitle.getText().toString();
            String message = etMsg.getText().toString();

            SendNotificationsViewModel.Audience audience;
            if (rbWait.isChecked()) audience = SendNotificationsViewModel.Audience.WAITLIST;
            else if (rbCancel.isChecked()) audience = SendNotificationsViewModel.Audience.CANCELLED;
            else if (rbEveryone.isChecked()) audience = SendNotificationsViewModel.Audience.EVERYONE;
            else if (rbAccepted.isChecked()) audience = SendNotificationsViewModel.Audience.ACCEPTED;
            else audience = SendNotificationsViewModel.Audience.SELECTED;

            // Send the notification
            vm.sendCustomNotification(
                    event,
                    audience,
                    title,
                    message
            ).addOnSuccessListener(l -> {
                Navigation.findNavController(view).popBackStack();
            }).addOnFailureListener(e -> {
                Log.d("Notifications", "Failed to send notification", e);
            });
        });
    }
}
