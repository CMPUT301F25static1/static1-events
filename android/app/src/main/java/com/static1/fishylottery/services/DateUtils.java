package com.static1.fishylottery.services;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date and time formatter for common formats and ranges throughout the application.
 */
public class DateUtils {

    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("MMMM d, yyyy");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");

    /**
     * Format a date range using the date formatter.
     *
     * @param start Start date.
     * @param end End date.
     * @return The formatted string.
     */
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

    /**
     * Format a the time components of a date range using the date formatter.
     *
     * @param start Start date with time.
     * @param end End date with time.
     * @return The formatted time string.
     */
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

    /**
     * Format a single date and time as 1 string.
     *
     * @param date The date to format.
     * @return The formatted string.
     */
    public static String formatDateTime(Date date) {
        if (date == null) return "";

        return dateTimeFormat.format(date);
    }
}