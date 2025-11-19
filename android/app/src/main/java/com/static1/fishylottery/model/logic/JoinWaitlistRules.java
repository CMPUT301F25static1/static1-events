package com.static1.fishylottery.model.logic;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.GeoPoint;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.GeolocationRequirement;
import com.static1.fishylottery.utils.LocationRequirementUtils;

public final class JoinWaitlistRules {
    private JoinWaitlistRules() {}

    /**
     * Returns true if joining is allowed. We allow join when there is no deadline,
     * or when now <= deadline. We disallow when now is after the deadline.
     *
     * @param deadlineMillis nullable deadline in epoch millis (null => no deadline)
     * @param nowMillis      current time in epoch millis
     */
    public static boolean canJoin(@Nullable Long deadlineMillis, long nowMillis) {
        if (deadlineMillis == null) return true;
        return nowMillis <= deadlineMillis;
    }

    /**
     * Determines if the user can join the waitlist based on the presence of a geolocation
     * requirement.
     *
     * @param event The event that the waitlist is for.
     * @param userLocation The user's current location from the device.
     * @return A boolean, true is can join, false is cannot.
     */
    public static boolean canJoinWithGeolocationRequirement(@NonNull Event event, Location userLocation) {
        GeolocationRequirement locationRequirement = event.getLocationRequirement();

        if (locationRequirement == null) return true;

        if (locationRequirement.getEnabled() == false) return true;

        GeoPoint geoPoint = locationRequirement.getLocation();
        Double radius = locationRequirement.getRadius();

        if (geoPoint == null || radius == null) return true;

        double requiredLat = geoPoint.getLatitude();
        double requiredLng = geoPoint.getLongitude();
        double userLat = userLocation.getLatitude();
        double userLng = userLocation.getLongitude();

        double radiusKm = radius / 1000;

        return LocationRequirementUtils.isWithinBoundary(
                requiredLat,
                requiredLng,
                userLat,
                userLng,
                radiusKm
        );
    }
}


