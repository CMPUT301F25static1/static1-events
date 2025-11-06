package com.static1.fishylottery.services;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("MMMM d, yyyy");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");

    // Formats the date range
    public static String formatDateRange(Date start, Date end) {
        if (start == null || end == null) return "";

        long startDay = start.getTime() / (24 * 60 * 60 * 1000);
        long endDay = end.getTime() / (24 * 60 * 60 * 1000);

        if (startDay == endDay) {
            return dayFormat.format(start);
        } else {
            return dayFormat.format(start) + " - " + dayFormat.format(end);
        }
    }

    // Formats the time range
    public static String formatTimeRange(Date start, Date end) {
        if (start == null || end == null) return "";

        long startMillis = start.getTime();
        long endMillis = end.getTime();

        if (startMillis == endMillis) {
            return timeFormat.format(start);
        } else {
            return timeFormat.format(start) + " - " + timeFormat.format(end);
        }
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "";

        return dateTimeFormat.format(date);
    }
}