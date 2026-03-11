package com.example.lab10.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DISPLAY_TIME_FORMAT = "HH:mm";
    public static final String DISPLAY_DATETIME_FORMAT = "dd/MM/yyyy HH:mm";
    
    public static String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    public static String formatTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) return "";
        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_TIME_FORMAT, Locale.getDefault());
            // ISO datetime: 2026-03-05T10:00:16.691345 hoặc 2026-03-05T10:00:16
            if (timeString.contains("T")) {
                String timePart = timeString.split("T")[1];
                // Cắt phần nano/micro seconds để chỉ lấy HH:mm:ss
                if (timePart.length() > 8) timePart = timePart.substring(0, 8);
                SimpleDateFormat isoFmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                Date time = isoFmt.parse(timePart);
                return outputFormat.format(time);
            }
            // Hỗ trợ cả "HH:mm" lẫn "HH:mm:ss"
            String fmt = timeString.length() > 5 ? "HH:mm:ss" : "HH:mm";
            SimpleDateFormat inputFormat = new SimpleDateFormat(fmt, Locale.getDefault());
            Date time = inputFormat.parse(timeString);
            return outputFormat.format(time);
        } catch (ParseException e) {
            return timeString;
        }
    }
    
    public static String formatDateTime(String dateTimeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_DATETIME_FORMAT, Locale.getDefault());
            Date dateTime = inputFormat.parse(dateTimeString);
            return outputFormat.format(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTimeString;
        }
    }
    
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }
    
    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }
}
