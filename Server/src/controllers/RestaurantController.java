package controllers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import entities.Restaurant;
import entities.Table;
import entities.TimeRange;

public class RestaurantController {
	
	private Restaurant restaurant;
	
	public RestaurantController(Restaurant restaurant) {
        this.restaurant = restaurant;
   }
	
	/**
     * Purpose: Adds a new table to the restaurant.
     * Receives: int capacity (number of seats).
     * Returns: void.
     */
    public void addTable(int capacity) {
    	// Logic to generate unique ID and add a new Table object to the list
    }
    
    /**
     * Purpose: Removes an existing table from the system.
     * Receives: int tableId.
     * Returns: boolean (true if removal was successful).
     */
    public boolean removeTable(int tableId) {
    	// Search for the table in the list and remove it
        return false;
    }
    
    /**
     * Purpose: Updates the seating capacity of a specific table.
     * Receives: int tableId, int newCapacity.
     * Returns: boolean (true if updated successfully).
     */
    public boolean updateTableCapacity(int tableId, int newCapacity) {
        // Find the table and set its new capacity
        return false;
    }
    
    /**
     * Purpose: Sets the standard operating hours for a specific day of the week.
     * Receives: DayOfWeek day, LocalTime open, LocalTime close.
     * Returns: void.
     */
    public void setRegularHours(DayOfWeek day, LocalTime open, LocalTime close) {
        // Create a TimeRange and store it in the regularHours map
    }
    
    /**
     * Purpose: Sets special opening hours for a specific calendar date.
     * Receives: LocalDate date, LocalTime open, LocalTime close.
     * Returns: void.
     */
    public void setSpecialHours(LocalDate date, LocalTime open, LocalTime close) {
        // Create a TimeRange and store it in the specialHours map
    }
    
    /**
     * Purpose: Checks if the restaurant is open at a specific timestamp.
     * Receives: LocalDateTime dateTime.
     * Returns: boolean (true if open).
     */
    public boolean isRestaurantOpen(LocalDateTime dateTime) {
        // Retrieve the relevant TimeRange and check if the time is within range
        return false;
    }
    
    /**
     * Purpose: Finds a specific table by its unique identifier.
     * Receives: int tableId.
     * Returns: Table object (or null if not found).
     */
    public Table getTableById(int tableId) {
        // Iterate through the table list to find a match
        return null;
    }
    
    /**
     * Purpose: Retrieves the full list of all tables in the restaurant.
     * Receives: None.
     * Returns: List<Table>.
     */
    public List<Table> getAllTables() {
        return restaurant.getTables();
    }
    
    public LocalTime getOpeningHour(LocalDate date) {
        TimeRange range = restaurant.getHoursForDate(date);       
        return (range != null) ? range.getOpenTime() : null;
    }

  
    public LocalTime getClosingHour(LocalDate date) {
        TimeRange range = restaurant.getHoursForDate(date);
        return (range != null) ? range.getCloseTime() : null;
    }
	// check
}
