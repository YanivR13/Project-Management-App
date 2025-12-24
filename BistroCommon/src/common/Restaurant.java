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