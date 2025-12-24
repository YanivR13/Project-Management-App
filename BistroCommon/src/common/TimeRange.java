package common;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Utility class representing a specific time interval (e.g., Opening Hours).
 * This class is crucial for validating if a requested reservation falls within 
 * the operational hours of the restaurant.
 * * <p>Implemented as {@link Serializable} to allow cross-network transfer between 
 * Client and Server via OCSF.</p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public class TimeRange implements Serializable {
    
    /** Serial version UID for maintaining serialization consistency across different JVMs. */
    private static final long serialVersionUID = 1L;
    
    /** The start of the time interval in "HH:mm" format. */
    private String openTime;  
    
    /** The end of the time interval in "HH:mm" format. */
    private String closeTime; 

    /**
     * Constructs a TimeRange with designated start and end points.
     * * @param openTime Opening time string (e.g., "08:00").
     * @param closeTime Closing time string (e.g., "22:00").
     */
    public TimeRange(String openTime, String closeTime) {
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    /**
     * Core Algorithm: Determines if a given time string falls within this defined range.
     * This method is designed to handle two distinct logical scenarios:
     * 1. Standard ranges (e.g., 08:00 to 22:00).
     * 2. Midnight-crossing ranges (e.g., 22:00 to 02:00).
     *
     * @param timeStr The time to check in "HH:mm" format.
     * @return true if the time is within the range (inclusive), false otherwise.
     */
    public boolean isWithinRange(String timeStr) {
        try {
            // Parse the input strings into Java 8 LocalTime objects for accurate comparison
            LocalTime target = LocalTime.parse(timeStr);
            LocalTime start = LocalTime.parse(openTime);
            LocalTime end = LocalTime.parse(closeTime);
            
            /**
             * SCENARIO 1: Standard Operational Hours
             * Example: Opening at 08:00 and closing at 22:00.
             * Logic: The target must be AFTER (or equal to) the start AND BEFORE (or equal to) the end.
             */
            if (start.isBefore(end)) {
                // Using !isBefore and !isAfter effectively creates an 'Inclusive' (>= and <=) check
                return (!target.isBefore(start) && !target.isAfter(end));
            } 
            
            /**
             * SCENARIO 2: Midnight Crossing / Late Night Hours
             * Example: Opening at 22:00 and closing at 02:00 the next day.
             * Logic: The target is valid if it is AFTER the start (late night) OR BEFORE the end (early morning).
             */
            return (!target.isBefore(start) || !target.isAfter(end));
            
        } catch (DateTimeParseException e) {
            // Logs formatting errors to the standard error stream for debugging
            System.err.println("TimeRange Error: Invalid time format - " + timeStr);
            return false;
        }
    }

    /** @return The starting time of this range. */
    public String getOpenTime() { 
        return openTime; 
    }

    /** @return The ending time of this range. */
    public String getCloseTime() { 
        return closeTime; 
    }

    /**
     * Provides a human-readable representation of the time interval.
     * @return A string in the format "HH:mm - HH:mm".
     */
    @Override
    public String toString() {
        return openTime + " - " + closeTime;
    }
}