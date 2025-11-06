package com.static1.fishylottery.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.static1.fishylottery.R;
import com.static1.fishylottery.controller.CreateEventControllerViewModel;
import com.static1.fishylottery.model.entities.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateEventPreviewFragment extends Fragment {

    private CreateEventControllerViewModel vm;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy  h:mm a", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_event_preview, container, false);

        vm = new ViewModelProvider(requireActivity()).get(CreateEventControllerViewModel.class);

        // optional preview summary. look up IDs dynamically so XML can omit them
        int idTitle = getResources().getIdentifier("text_preview_title", "id", requireContext().getPackageName());
        int idLoc   = getResources().getIdentifier("text_preview_location", "id", requireContext().getPackageName());
        int idWin   = getResources().getIdentifier("text_preview_window", "id", requireContext().getPackageName());

        final TextView tvTitle    = idTitle != 0 ? view.findViewById(idTitle) : null;
        final TextView tvLocation = idLoc   != 0 ? view.findViewById(idLoc)   : null;
        final TextView tvWindow   = idWin   != 0 ? view.findViewById(idWin)   : null;

        vm.getEvent().observe(getViewLifecycleOwner(), e -> {
            if (e == null) return;
            if (tvTitle != null)    tvTitle.setText(e.getTitle());
            if (tvLocation != null) tvLocation.setText(e.getLocation());
            if (tvWindow != null)   tvWindow.setText(formatWindow(
                    e.getEventStartDate(), e.getEventEndDate(), e.getRegistrationCloses()));
        });

        // Show validation errors emitted by the ViewModel
        vm.getValidationError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && getView() != null) {
                Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
            }
        });

        // “Create / Publish” button
        Button createBtn = view.findViewById(R.id.button_create_event);
        if (createBtn != null) {
            createBtn.setOnClickListener(v -> {
                boolean ok = vm.submit();   // runs the checks; saves if valid
                if (ok) {
                    Snackbar.make(v, "Event created!", Snackbar.LENGTH_SHORT).show();
                    // navigate here for a nav target:
                    // Navigation.findNavController(v).navigate(R.id.action_eventPreview_to_hostedEvents);
                }
                // If not ok, the observer above shows the error.
            });
        }

        return view;
    }

    private String formatWindow(Date start, Date end, Date regClose) {
        String s = (start == null ? "-" : df.format(start));
        String e = (end   == null ? "-" : df.format(end));
        String r = (regClose == null ? "-" : df.format(regClose));
        return "Start: " + s + "\nEnd: " + e + "\nRegistration closes: " + r;
    }
}
