package com.example.rssreader.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private static final String[] DATE_PATTERNS = {
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss",
            "EEE, dd MMM yyyy HH:mm:ss",
            "dd MMM yyyy HH:mm:ss Z"
    };

    private DateUtils() {}

    public static long parseRssDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return System.currentTimeMillis();
        }
        String cleanDate = dateStr.trim();
        for (String pattern : DATE_PATTERNS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
                sdf.setLenient(true);
                Date date = sdf.parse(cleanDate);
                if (date != null) {
                    return date.getTime();
                }
            } catch (ParseException ignored) {
            }
        }
        return System.currentTimeMillis();
    }

    public static String formatRelativeTime(long timeMillis) {
        if (timeMillis <= 0) return "";
        long now = System.currentTimeMillis();
        long diff = now - timeMillis;

        if (diff < 0) {
            diff = 0;
        }

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days < 7) {
            return days + " ngày trước";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(timeMillis));
        }
    }
}
