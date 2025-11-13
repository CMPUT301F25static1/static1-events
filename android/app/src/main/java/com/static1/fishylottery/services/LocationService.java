package com.static1.fishylottery.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Provides access to the user's last known location.
 * Designed for easy mocking in tests.
 */
public class LocationService {

    private final FusedLocationProviderClient locationClient;

    public interface LocationCallback {
        void onLocationResult(Location location);
        void onLocationError(Exception e);
    }

    public LocationService(FusedLocationProviderClient client) {
        this.locationClient = client;
    }

    public static LocationService create(Context context) {
        return new LocationService(LocationServices.getFusedLocationProviderClient(context));
    }

    /**
     * Fetch the last known device location.
     * Caller must ensure location permission is granted.
     */
    @SuppressLint("MissingPermission")
    public void getCurrentLocation(LocationCallback callback) {
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationResult(location);
                        Log.d("LocationService", "Last Location is: " + location.toString());
                    } else {
                        Exception e = new Exception("Location unavailable");
                        callback.onLocationError(e);
                        Log.e("LocationService", "Location is unavailable", e);
                    }
                })
                .addOnFailureListener(callback::onLocationError);
    }
}