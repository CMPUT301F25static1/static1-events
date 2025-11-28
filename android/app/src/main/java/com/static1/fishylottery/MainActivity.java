package com.static1.fishylottery;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.static1.fishylottery.databinding.ActivityMainBinding;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.model.repositories.IEventRepository;
import com.static1.fishylottery.services.AuthManager;
import com.static1.fishylottery.view.events.QrScanActivity;

import java.util.Objects;

/**
 * This is the main activity and entry point for the entire application.
 * Other than some other utility views like the QR code scanner, every screen in the tab is a
 * fragment that exists within this Activity.
 */
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> qrScanLauncher;
    private IEventRepository eventRepo = new EventRepository();
    private NavController navController;
    private boolean locationEnabled = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MaterialToolbar toolbar = findViewById(R.id.top_app_bar);

        setSupportActionBar(toolbar);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_events,
                R.id.navigation_notifications,
                R.id.navigation_profile
        ).build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        forceWhiteNavigationIcon(toolbar);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            forceWhiteNavigationIcon(toolbar);
        });

        qrScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String qrValue = result.getData().getStringExtra("qrResult");
                        openEventDetailsWithQrCode(qrValue);
                    }
                }
        );

        setupAuth();

        enableLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_qr_scan) {
            Intent intent = new Intent(this, QrScanActivity.class);
            qrScanLauncher.launch(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    locationEnabled = true;
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                locationEnabled = false;
            }
        }
    }

    private void setupAuth() {
        // Setup the Firebase Anonymous Auth
        AuthManager.getInstance().signInAnonymously().addOnSuccessListener(result -> {
            if (result.getUser() != null) {
                Log.d("Auth", "Signed in user with uid: " + result.getUser().getUid());
            } else {
                Log.d("Auth", "User signed in, but no ID?");
            }
        }).addOnFailureListener(e -> {
            Log.e("Auth", "Could not sign in user. Try again later.", e);
        });
    }

    private void openEventDetailsWithQrCode(String qrValue) {
        if (qrValue == null) {
            throw new IllegalArgumentException("QR value cannot be null");
        }

        Log.d("QrScanner", "Scanned QR result is: " + qrValue);

        Uri uri = Uri.parse(qrValue);

        if (uri == null) {
            return;
        }

        String bundleId = getApplicationContext().getPackageName();

        String scheme = uri.getScheme();
        String host = uri.getHost();
        String eventId = uri.getQueryParameter("id");

        if (!bundleId.equals(scheme) || !Objects.equals(host, "events")) {
            return;
        }

        if (eventId == null) {
            return;
        }

        Log.d("QrScanner", "Scanned event id is " + eventId);

        // Fetch the event
        eventRepo.getEventById(eventId)
                .addOnSuccessListener(event -> {
                    if (event != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event", event);
                        navController.navigate(R.id.action_mainActivity_to_eventDetails, bundle);
                    } else {
                        Log.d("QrScanner", "No event found with ID: " + eventId);
                    }
                }).addOnFailureListener(e -> {
                    Log.d("QrScanner", "Failed to get event", e);
                });
    }

    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationEnabled = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void forceWhiteNavigationIcon(MaterialToolbar toolbar) {
        toolbar.post(() -> {
            Drawable icon = toolbar.getNavigationIcon();
            if (icon != null) {
                icon = DrawableCompat.wrap(icon).mutate();
                DrawableCompat.setTint(
                        icon,
                        ContextCompat.getColor(this, R.color.white)
                );
                toolbar.setNavigationIcon(icon);
            }
        });
    }

}