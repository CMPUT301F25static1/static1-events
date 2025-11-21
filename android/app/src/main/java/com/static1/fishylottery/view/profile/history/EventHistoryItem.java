package com.static1.fishylottery.view.profile.history;

import java.util.Date;

public class EventHistoryItem {
    private final String title;
    private final String when;
    private final String where;
    private final String status;
    private final Date eventDate;

    public EventHistoryItem(String title, String when, String where, String status, Date eventDate) {
        this.title = title;
        this.when = when;
        this.where = where;
        this.status = status;
        this.eventDate = eventDate;
    }

    public String getTitle() { return title; }
    public String getWhen() { return when; }
    public String getWhere() { return where; }
    public String getStatus() { return status; }
    public Date getEventDate() { return eventDate; }
}