package com.static1.fishylottery.view.events.create;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.static1.fishylottery.R;
import com.static1.fishylottery.services.LocationService;

import java.lang.reflect.Method;

public class LocationPickerFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private Marker centerMarker;
    private Circle radiusCircle;

    private Double centerLat = 37.7749;
    private Double centerLng = -122.4194;
    private Double radiusMeters = 1000.0;

    private SeekBar radiusSeekBar;
    private TextView radiusLabel;
    private static final int MIN_RADIUS = 1000;
    private static final int MAX_RADIUS = 1_000_000;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_location_picker, container, false);

        radiusLabel = view.findViewById(R.id.radius_label);
        radiusSeekBar = view.findViewById(R.id.radius_seek_bar);

        radiusSeekBar.setMax(MAX_RADIUS - MIN_RADIUS);
        radiusSeekBar.setProgress(radiusMeters.intValue() - MIN_RADIUS);

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int radius = progress + MIN_RADIUS;

                radiusMeters = Integer.valueOf(radius).doubleValue();
                updateRadiusLabel(radiusMeters);
                updateCircle(radiusMeters);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.sheet_map_fragment);
        mapFragment.getMapAsync(this);

        // Save button
        view.findViewById(R.id.button_save_location).setOnClickListener(v -> {
            if (centerLat != null && centerLng != null) {
                Bundle result = new Bundle();
                result.putDouble("lat", centerLat);
                result.putDouble("lng", centerLng);
                result.putDouble("radius", radiusMeters);

                getParentFragmentManager().setFragmentResult(
                        "locationPickerResult",
                        result
                );
            }

            Navigation.findNavController(view).navigateUp();
        });

        BottomNavigationView navView = requireActivity().findViewById(R.id.nav_view);
        navView.post(() -> {
            view.setPadding(0, 0,0, navView.getHeight());
        });

        updateRadiusLabel(radiusMeters);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        LocationService locationService = LocationService.create(requireContext());
        locationService.getCurrentLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationResult(Location location) {
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d("Location", pos.toString());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f));
                centerMarker = googleMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .draggable(true)
                        .title("Event Location"));
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                radiusCircle = googleMap.addCircle(new CircleOptions()
                        .center(pos)
                        .radius(radiusMeters)
                        .strokeWidth(2)
                        .strokeColor(0xFF0066FF)
                        .fillColor(0x220066FF));

                centerLat = location.getLatitude();
                centerLng = location.getLongitude();
            }

            @Override
            public void onLocationError(Exception e) {
                Log.e("LocationService", "Failed to get location", e);
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) {
                centerLat = marker.getPosition().latitude;
                centerLng = marker.getPosition().longitude;
                updateCircle(radiusMeters);
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                centerLat = marker.getPosition().latitude;
                centerLng = marker.getPosition().longitude;
                updateCircle(radiusMeters);
            }
        });
    }

    private void updateCircle(Double radius) {
        if (googleMap == null) return;

        if (radiusCircle == null) {
            radiusCircle = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(centerLat, centerLng))
                    .radius(radius)
                    .strokeWidth(2)
                    .strokeColor(0xFF0066FF)
                    .fillColor(0x220066FF)
            );
        } else {
            radiusCircle.setRadius(radius);
            radiusCircle.setCenter(new LatLng(centerLat, centerLng));
        }
    }

    private void updateRadiusLabel(Double radius) {
        radiusLabel.setText(String.format("Radius: %.1f km", radius / 1000));
    }
}
