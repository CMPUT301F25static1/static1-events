package com.static1.fishylottery.view.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.NotificationsViewModel;

import java.text.DateFormat;
import java.util.Date;

public class NotificationDetailFragment extends Fragment {

    private NotificationsViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        vm = new ViewModelProvider(this).get(NotificationsViewModel.class);

        TextView tvTitle = v.findViewById(R.id.tvDetailTitle);
        TextView tvDate = v.findViewById(R.id.tvDetailDate);
        TextView tvMessage = v.findViewById(R.id.tvDetailMessage);

        LinearLayout inviteActions = v.findViewById(R.id.inviteActions);
        Button accept = v.findViewById(R.id.btnAccept);
        Button decline = v.findViewById(R.id.btnDecline);

        Bundle args = getArguments();
        if (args == null) return;

        String notifId = args.getString("notificationId");
        String title = args.getString("title");
        String message = args.getString("message");
        String type = args.getString("type");
        String status = args.getString("status");
        long createdAt = args.getLong("createdAt");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user != null) ? user.getUid() : "TEST_UID";

        tvTitle.setText(title);
        tvMessage.setText(message);

        if (createdAt > 0)
            tvDate.setText(DateFormat.getDateTimeInstance().format(new Date(createdAt)));

        boolean isInvitation = "invitation".equalsIgnoreCase(type);
        boolean isPending = "pending".equalsIgnoreCase(status);

        if (isInvitation && isPending) {
            inviteActions.setVisibility(View.VISIBLE);

            accept.setOnClickListener(x -> vm.respondToInvitation(uid, notifId, true));
            decline.setOnClickListener(x -> vm.respondToInvitation(uid, notifId, false));
        } else {
            inviteActions.setVisibility(View.GONE);
        }
    }
}
