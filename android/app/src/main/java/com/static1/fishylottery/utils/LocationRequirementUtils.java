package com.static1.fishylottery.utils;

/**
 * A utility class for determining the distance between location coordinates and determining if
 * a user's location is within a specified boundary.
 */
public final class LocationRequirementUtils {
    private static final double EARTH_RADIUS_KM = 6371.0;

    private LocationRequirementUtils() {}

    /**
     * This is the Haversine distance function to determine the distance between 2 points upon a
     * spherical surface such as the Earth. This function uses 2 sets of latitude and longitude
     * coordinate pairs to calculate the distance between.
     *
     * @param lat1 The latitude value of the first coordinate pair.
     * @param lon1 The longitude value of the first coordinate pair.
     * @param lat2 The latitude value of the second coordinate pair.
     * @param lon2 the longitude value of the second coordinate pair.
     * @return The distance between the points in kilometers (km)
     */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(rLat1) * Math.cos(rLat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Determines if 2 points are within a boundary.
     *
     * @param requiredLat The latitude for the centre of the required boundary.
     * @param requiredLng The longitude for the centre of the required boundary.
     * @param userLat The user's current latitude.
     * @param userLng The user's current longitude.
     * @param maxDistanceKm The max distance allowable from the required location to the user's
     *                      location.
     * @return A boolean indicating if the user is within the boundary.
     */
    public static boolean isWithinBoundary(double requiredLat,
                                           double requiredLng,
                                           double userLat,
                                           double userLng,
                                           double maxDistanceKm) {

        double actualDistance = distanceKm(userLat, userLng, requiredLat, requiredLng);
        return actualDistance <= maxDistanceKm;
    }
}
