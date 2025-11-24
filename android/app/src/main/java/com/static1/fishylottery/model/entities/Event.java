package com.static1.fishylottery.model.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Event implements Serializable {
    private String eventId;
    private String title;
    private String description;
    private String eventType;
    private List<String> interests;
    private String location;
    private String hostedBy;
    private String status;
    private Integer capacity;
    private Integer maxWaitlistSize = null;
    private String organizerId;
    private String imageUrl;
    private Date eventStartDate;
    private Date eventEndDate;
    private Date registrationCloses;
    private Date lotteryDate;
    private Date createdAt;
    private Date updatedAt;
    private Date registrationOpens;
    private GeolocationRequirement locationRequirement;

    public Event() {}

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getMaxWaitlistSize() {
        return maxWaitlistSize;
    }

    public void setMaxWaitlistSize(Integer maxWaitlistSize) {
        this.maxWaitlistSize = maxWaitlistSize;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(Date eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public Date getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(Date eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public Date getRegistrationCloses() {
        return registrationCloses;
    }

    public void setRegistrationCloses(Date registrationCloses) {
        this.registrationCloses = registrationCloses;
    }

    public Date getLotteryDate() {
        return lotteryDate;
    }

    public void setLotteryDate(Date lotteryDate) {
        this.lotteryDate = lotteryDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getHostedBy() {
        return hostedBy;
    }

    public void setHostedBy(String hostedBy) {
        this.hostedBy = hostedBy;
    }

    public Date getRegistrationOpens() { return registrationOpens; }
    public void setRegistrationOpens(Date registrationOpens) { this.registrationOpens = registrationOpens; }

    public boolean isWithinRegistrationWindow(Date now) {
        if (now == null) now = new Date();
        if (registrationOpens != null && now.before(registrationOpens)) return false;
        if (registrationCloses != null && now.after(registrationCloses)) return false;
        return true;
    }


    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public GeolocationRequirement getLocationRequirement() {
        return locationRequirement;
    }

    public void setLocationRequirement(GeolocationRequirement locationRequirement) {
        this.locationRequirement = locationRequirement;
    }
}
