package com.procgrid.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Date and time utility class
 */
public class DateUtil {
    
    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SIMPLE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT);
    private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
    private static final DateTimeFormatter SIMPLE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(SIMPLE_DATE_TIME_FORMAT);
    
    /**
     * Get current UTC timestamp
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }
    
    /**
     * Get current local timestamp
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
    
    /**
     * Format LocalDateTime to ISO string
     */
    public static String toIsoString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.of("UTC")).format(ISO_FORMATTER);
    }
    
    /**
     * Format LocalDateTime to simple date string
     */
    public static String toSimpleDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(SIMPLE_FORMATTER);
    }
    
    /**
     * Format LocalDateTime to simple date time string
     */
    public static String toSimpleDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(SIMPLE_DATETIME_FORMATTER);
    }
    
    /**
     * Parse ISO string to LocalDateTime
     */
    public static LocalDateTime fromIsoString(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) {
            return null;
        }
        return ZonedDateTime.parse(isoString, ISO_FORMATTER.withZone(ZoneId.of("UTC")))
                .toLocalDateTime();
    }
    
    /**
     * Add days to LocalDateTime
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }
    
    /**
     * Add hours to LocalDateTime
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }
    
    /**
     * Calculate difference in days between two dates
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }
    
    /**
     * Calculate difference in hours between two dates
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }
    
    /**
     * Check if date is in the past
     */
    public static boolean isPast(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isBefore(LocalDateTime.now());
    }
    
    /**
     * Check if date is in the future
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * Get start of day
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atStartOfDay();
    }
    
    /**
     * Get end of day
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atTime(23, 59, 59, 999_999_999);
    }
}