package com.static1.fishylottery;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.color.utilities.Scheme;
import com.static1.fishylottery.databinding.ActivityMainBinding;
import com.static1.fishylottery.model.repositories.EventRepository;
import com.static1.fishylottery.view.events.QrScanActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> qrScanLauncher;
    private EventRepository eventRepo;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        MainApplication app = (MainApplication) getApplication();
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_events, R.id.navigation_notifications, R.id.navigation_profile)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        eventRepo = new EventRepository();

        // Setup the Firebase Anonymous Auth
        app.getAuthManager().ensureSignedIn(new Runnable() {
            @Override
            public void run() {
                Log.d("AuthManager", "The user has been signed in");
            }
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
}