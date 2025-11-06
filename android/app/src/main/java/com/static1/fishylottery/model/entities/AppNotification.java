package com.static1.fishylottery.model.entities;

import java.util.Date;

/** In-app notification stored under users/{uid}/notifications/{notificationId} */
public class AppNotification {
    private String id;        // Firestore doc id (filled client-side)
    private String eventId;
    private String senderId;  // organizer uid or "system"
    private String title;
    private String message;
    private String type;      // CHOSEN | NOT_CHOSEN | ORGANIZER_BROADCAST
    private Date createdAt;
    private boolean read;

    public AppNotification() { } // Firestore needs empty ctor

    public AppNotification(String eventId, String senderId, String title,
                           String message, String type, Date createdAt, boolean read) {
        this.eventId = eventId;
        this.senderId = senderId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.read = read;
    }

    // Getters / Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
