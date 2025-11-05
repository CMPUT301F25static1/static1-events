package com.static1.fishylottery.view.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.SendNotificationViewModel;
import com.static1.fishylottery.model.repositories.NotificationSender;

public class SendNotificationFragment extends Fragment {

    public static final String ARG_EVENT_ID = "eventId";

    private SendNotificationViewModel vm;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        vm = new ViewModelProvider(this).get(SendNotificationViewModel.class);

        RadioGroup rg = v.findViewById(R.id.rgAudience);
        RadioButton rbSelected = v.findViewById(R.id.rbSelected);
        RadioButton rbWait = v.findViewById(R.id.rbWaitlist);
        RadioButton rbCancel = v.findViewById(R.id.rbCancelled);
        RadioButton rbChosen = v.findViewById(R.id.rbChosen);
        EditText etChosen = v.findViewById(R.id.etChosenUids);
        EditText etTitle = v.findViewById(R.id.etTitle);
        EditText etMsg = v.findViewById(R.id.etMessage);
        Button btnSend = v.findViewById(R.id.btnSend);

        rbSelected.setChecked(true); // default

        vm.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
        });
        vm.getSuccess().observe(getViewLifecycleOwner(), ok -> {
            if (ok != null && ok) Toast.makeText(requireContext(), "Sent!", Toast.LENGTH_SHORT).show();
        });

        btnSend.setOnClickListener(view -> {
            String eventId = requireArguments().getString(ARG_EVENT_ID);
            String title = etTitle.getText().toString();
            String message = etMsg.getText().toString();
            String organizerUid = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "system";

            NotificationSender.Audience audience;
            if (rbChosen.isChecked()) audience = NotificationSender.Audience.CHOSEN;
            else if (rbWait.isChecked()) audience = NotificationSender.Audience.WAITLIST;
            else if (rbCancel.isChecked()) audience = NotificationSender.Audience.CANCELLED;
            else audience = NotificationSender.Audience.SELECTED;

            vm.send(eventId, audience, title, message, organizerUid, etChosen.getText().toString());
        });
    }
}
