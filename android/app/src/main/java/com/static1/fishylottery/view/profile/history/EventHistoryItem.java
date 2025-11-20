package com.static1.fishylottery.view.profile.history;

public class EventHistoryItem {
    private final String title;
    private final String when;
    private final String where;
    private final String status;

    public EventHistoryItem(String title, String when, String where, String status) {
        this.title = title;
        this.when = when;
        this.where = where;
        this.status = status;
    }

    public String getTitle() { return title; }
    public String getWhen() { return when; }
    public String getWhere() { return where; }
    public String getStatus() { return status; }
}