package common;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core Domain Entity representing a Restaurant in the system.
 * This class serves as a Data Transfer Object (DTO) shared between the Client and Server.
 * It encapsulates basic identification, operational scheduling, and physical table inventory management.
 * * <p>Implemented as {@link Serializable} to facilitate object streaming over OCSF socket connections.</p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public class Restaurant implements Serializable {
    
    /** Serial Version UID for cross-platform serialization compatibility. */
    private static final long serialVersionUID = 1L;

    /** Unique Database Identifier (Primary Key in the 'restaurant' table). */
    private int restaurantId;
    
    /** Descriptive name of the restaurant for UI display purposes. */
    private String restaurantName;
    
    /** * Regular weekly operating schedule. 
     * Maps a day name (e.g., "Monday") to a {@link TimeRange} object defining opening/closing hours.
     */
    private Map<String, TimeRange> regularHours;
    
    /** * Calendar-specific scheduling overrides.
     * Maps a specific {@link LocalDate} to a {@link TimeRange}. 
     * Used for holidays, special events, or emergency closures.
     */
    private Map<LocalDate, TimeRange> specialHours;
    
    /** * Physical Table Inventory.
     * Maps table seating capacity (Integer) to the total count of such tables available (Integer).
     * Example: {4 -> 10} means there are ten tables that seat 4 people each.
     */
    private Map<Integer, Integer> tableInventory;

    /**
     * Constructs a new Restaurant instance.
     * Initializes empty HashMaps for scheduling and inventory to prevent NullPointerExceptions.
     *
     * @param restaurantId Unique database identifier.
     * @param restaurantName Display name of the restaurant.
     */
    public Restaurant(int restaurantId, String restaurantName) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.regularHours = new HashMap<>();
        this.specialHours = new HashMap<>();
        this.tableInventory = new HashMap<>();
    }

    // --- Identification Getters ---

    /** @return The unique restaurant ID. */
    public int getRestaurantId() { return restaurantId; }
    
    /** @return The restaurant's display name. */
    public String getRestaurantName() { return restaurantName; }

    // --- Operating Hours Management ---

    /**
     * Defines or updates the standard operating hours for a specific day of the week.
     * * @param day Standard day name (e.g., "Monday", "Tuesday").
     * @param open Opening time in "HH:mm" format.
     * @param close Closing time in "HH:mm" format.
     */
    public void setRegularHours(String day, String open, String close) {
        regularHours.put(day, new TimeRange(open, close));
    }

    /**
     * Defines or updates an override schedule for a specific calendar date.
     * * @param date The specific date for the override.
     * @param open Opening time in "HH:mm" format.
     * @param close Closing time in "HH:mm" format.
     */
    public void setSpecialHours(LocalDate date, String open, String close) {
        specialHours.put(date, new TimeRange(open, close));
    }
    
    /**
     * Retrieves the standard weekly operating schedule.
     * The map keys are day names (e.g., "Monday") and values are {@link TimeRange} objects.
     * * @return A {@link Map} containing the regular weekly hours.
     */
    public Map<String, TimeRange> getRegularHours() {
        return regularHours;
    }

    /**
     * Retrieves the calendar-specific scheduling overrides.
     * This map contains special hours for specific dates (holidays, events, etc.).
     * * @return A {@link Map} containing the date-specific special hours.
     */
    public Map<LocalDate, TimeRange> getSpecialHours() {
        return specialHours;
    }
    
    /**
     * Generates a comprehensive, human-readable summary of the restaurant's schedule.
     * This method fulfills two main requirements:
     * 1. Displays the standard weekly operating hours for general information.
     * 2. Identifies and highlights 'Special Hours' (overrides) for the next 14 days.
     * * The logic ensures that if a special date exists (like a holiday), it is 
     * explicitly marked as an override to the regular schedule.
     * * @return A formatted String containing the full schedule and prioritized alerts.
     */
    public String getFormattedOpeningHours() {
        // StringBuilder is used for efficient string manipulation in loops
        StringBuilder sb = new StringBuilder("=== RESTAURANT OPERATING HOURS ===\n\n");

        // --- SECTION 1: Standard Weekly Schedule ---
        sb.append("[ Standard Weekly Schedule ]\n");
        if (regularHours == null || regularHours.isEmpty()) {
            sb.append("No regular hours are currently defined.\n");
        } else {
            // Iterating through the regularHours Map (e.g., Monday: 09:00 - 22:00)
            for (Map.Entry<String, TimeRange> entry : regularHours.entrySet()) {
                sb.append("• ").append(entry.getKey())
                  .append(": ")
                  .append(entry.getValue().toString())
                  .append("\n");
            }
        }

        // --- SECTION 2: Special Overrides Logic ---
        // We define a 14-day window to show relevant upcoming schedule changes
        LocalDate today = LocalDate.now();
        LocalDate lookAheadLimit = today.plusDays(14);
        boolean hasSpecialAlerts = false;

        // Iterate through specialHours to find dates falling within the 14-day window
        for (Map.Entry<LocalDate, TimeRange> entry : specialHours.entrySet()) {
            LocalDate specialDate = entry.getKey();

            // Verification: Check if the special date is between today and 2 weeks from now
            if (!specialDate.isBefore(today) && !specialDate.isAfter(lookAheadLimit)) {
                
                // Add a header only if at least one special date is found
                if (!hasSpecialAlerts) {
                    sb.append("\n[ !!! IMPORTANT: UPCOMING SCHEDULE CHANGES !!! ]\n");
                    hasSpecialAlerts = true;
                }

                // Logic: Explicitly state that this specific date overrides regular hours
                sb.append("• Date: ").append(specialDate)
                  .append(" (").append(specialDate.getDayOfWeek()).append(")\n")
                  .append("  New Hours: ").append(entry.getValue().toString())
                  .append(" *Overrides regular schedule*\n");
            }
        }

        // Fallback message if no special dates are found in the near future
        if (!hasSpecialAlerts) {
            sb.append("\nNo special holiday or event hours in the next 14 days.\n");
        }

        sb.append("\n=================================");
        return sb.toString();
    }

    /**
     * Business Logic: Validates if the restaurant is operational at a specific timestamp.
     * The method prioritizes 'Special Hours' (overrides) over 'Regular Hours'.
     *
     * @param date The calendar date to check.
     * @param timeStr The time string (HH:mm) to validate against opening hours.
     * @return true if the restaurant is open, false otherwise.
     */
    public boolean isOpen(LocalDate date, String timeStr) {
        // Step 1: Check for specific date overrides (Holiday/Special hours)
        TimeRange hours = specialHours.get(date);
        
        // Step 2: If no specific override exists, fall back to the regular weekly schedule
        if (hours == null) {
            // Format the DayOfWeek enum to a Title Case string (e.g., MONDAY -> Monday)
            String dayName = date.getDayOfWeek().name().substring(0, 1) + 
                             date.getDayOfWeek().name().substring(1).toLowerCase(); 
            hours = regularHours.get(dayName);
        }

        // Step 3: Validate the time against the identified range
        return (hours != null && hours.isWithinRange(timeStr));
    }

    // --- Table Inventory & Upgrade Logic ---
    
    /**
     * Populates the restaurant's physical table setup.
     * * @param capacity Seating capacity per table.
     * @param count Number of tables of this capacity to add.
     */
    public void addTablesToInventory(int capacity, int count) {
        int currentCount = tableInventory.getOrDefault(capacity, 0);
        tableInventory.put(capacity, currentCount + count);
    }

    /**
     * @param capacity The table size to query.
     * @return Total count of tables for the specified capacity.
     */
    public int getTableCountByCapacity(int capacity) {
        return tableInventory.getOrDefault(capacity, 0);
    }

    /**
     * Business Logic Algorithm: Table Best-Fit Selection.
     * Identifies the most efficient table capacity for a requested party size.
     * * <p>Algorithm logic:
     * 1. Collect all available capacities.
     * 2. Sort them in ascending order.
     * 3. Return the smallest capacity that can accommodate the 'requestedSize'.
     * </p>
     * * @param requestedSize Number of guests in the reservation.
     * @return The optimal table capacity, or -1 if the party size exceeds restaurant limits.
     */
    public int getBestFitCapacity(int requestedSize) {
        // Extract all existing table sizes from the inventory
        List<Integer> capacities = new ArrayList<>(tableInventory.keySet());
        
        // Sort to ensure we find the smallest possible fit first
        Collections.sort(capacities);
        
        for (int capacity : capacities) {
            // Find the first capacity that is equal to or larger than the guest count
            if (capacity >= requestedSize) {
                return capacity;
            }
        }
        
        // Return error code if no table is large enough
        return -1; 
    }

    /**
     * Returns a copy of the current table inventory.
     * @return A map representing capacity -> table count.
     */
    public Map<Integer, Integer> getFullInventory() {
        return new HashMap<>(tableInventory);
    }
}