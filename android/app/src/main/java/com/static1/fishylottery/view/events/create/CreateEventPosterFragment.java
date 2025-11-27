package com.static1.fishylottery.view.events.create;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.static1.fishylottery.R;
import com.static1.fishylottery.viewmodel.CreateEventViewModel;

/**
 * Fragment view for choosing an event poster image to upload during the event
 * creation process by an organizer.
 */
public class CreateEventPosterFragment extends Fragment {

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView imagePreview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event_poster, container, false);

        // Initialize the view model
        CreateEventViewModel viewModel = initViewModel();

        imagePreview = view.findViewById(R.id.image_poster_preview);
        Button chooseImageButton = view.findViewById(R.id.button_choose_image);
        Button nextButton = view.findViewById(R.id.button_next_preview);

        nextButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_eventPoster_to_eventPreview);
        });

        chooseImageButton.setOnClickListener(v -> {
            openFileChooser();
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        viewModel.setImageUri(uri);
                    }
                }
        );

        viewModel.getImageUri().observe(getViewLifecycleOwner(), imageUri -> {
            imagePreview.setImageURI(imageUri);
        });

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private CreateEventViewModel initViewModel() {
        // Create the view model, but scope it to the create event navigation graph so that it
        // only lives the lifetime of the 3 views used to create or edit the event.
        ViewModelStoreOwner vmOwner = NavHostFragment.findNavController(this)
                .getViewModelStoreOwner(R.id.create_event_graph);

        return new ViewModelProvider(vmOwner).get(CreateEventViewModel.class);
    }
}
