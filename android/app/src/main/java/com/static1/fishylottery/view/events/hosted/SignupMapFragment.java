package com.static1.fishylottery.view.events.hosted;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.collect.Maps;
import com.google.firebase.firestore.GeoPoint;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.WaitlistEntry;
import com.static1.fishylottery.model.repositories.WaitlistRepository;
import com.static1.fishylottery.services.LocationService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class SignupMapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap map;
    private Event event;
    private WaitlistRepository waitlistRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signup_map, container, false);

        waitlistRepository = new WaitlistRepository();

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_signup);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fetchWaitlist(event);

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);

        BottomNavigationView navView = requireActivity().findViewById(R.id.nav_view);
        navView.post(() -> {
            map.setPadding(0, 0,0, navView.getHeight());
        });

        LocationService locationService = LocationService.create(requireContext());
        locationService.getCurrentLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationResult(Location location) {
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d("Location", pos.toString());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f));
                googleMap.addMarker(new MarkerOptions().position(pos).title("You are here"));
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }

            @Override
            public void onLocationError(Exception e) {
                Log.e("LocationService", "Failed to get location", e);
            }
        });
    }

    private void fetchWaitlist(Event event) {
        if (event == null) return;

        waitlistRepository.getWaitlist(event)
                .addOnSuccessListener(this::addLocationsToMap)
                .addOnFailureListener(e -> {
                    Log.e("Waitlist", "Failed to get waitlist for event", e);
                });
    }

    private void addLocationsToMap(List<WaitlistEntry> entries) {
        for (WaitlistEntry entry : entries) {
            GeoPoint location = entry.getJoinLocation();
            String entrantName = entry.getProfile().getFullName();

            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            String formattedDate = formatter.format(entry.getJoinedAt());

            String joinText = "Joined: " + formattedDate;

            if (location == null) continue;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(entrantName)
                    .snippet(joinText)
            );
        }
    }
}
