package com.static1.fishylottery.model.entities;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class AppNotification {

    private String id;        // Firestore doc id (filled client-side)
    private String eventId;   // optional
    private String senderId;  // optional
    private String title;
    private String message;
    private String type;      // e.g., "info" or "invitation"
    private String status;    // "pending" | "accepted" | "declined"
    private boolean read;

    @ServerTimestamp
    private Date createdAt;

    /** REQUIRED empty constructor for Firestore */
    public AppNotification() { }

    /** ✅ FULL CONSTRUCTOR — this fixes your error */
    public AppNotification(String eventId,
                           String senderId,
                           String title,
                           String message,
                           String type,
                           String status,
                           Date createdAt,
                           boolean read) {

        this.eventId = eventId;
        this.senderId = senderId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.read = read;
    }

    // --- GETTERS & SETTERS ---
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
