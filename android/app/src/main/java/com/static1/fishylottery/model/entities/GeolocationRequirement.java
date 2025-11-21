package com.static1.fishylottery.model.entities;

import com.google.firebase.firestore.GeoPoint;

public class GeolocationRequirement {
    private Boolean enabled;
    private GeoPoint location;
    private Double radius;

    public GeolocationRequirement() {
        enabled = false;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
