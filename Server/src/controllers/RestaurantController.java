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
	    //Find the highest existing table ID in the list of tables
	    int maxId = 0;
	    for (Table t : restaurant.getTables()) {
	        if (t.getTableID() > maxId) {
	            maxId = t.getTableID();
	        }
	    }

	    //Create a new Table object with a unique ID (highest ID + 1)
	    Table newTable = new Table(maxId + 1, capacity);

	    //Add the new table to the restaurant's table list
	    restaurant.addTable(newTable);
	}

    
    
     
    
    /**
     * Purpose: Removes an existing table from the system.
     * Receives: int tableId.
     * Returns: boolean (true if removal was successful).
     */
    public boolean removeTable(int tableId) {
        //Check whether the table exists in the system before attempting to remove it
        Table tableToRemove = restaurant.getTableById(tableId);
        
        if (tableToRemove != null) {
            //Call the removal method inside the Restaurant object
            restaurant.removeTable(tableId);
            
            // Note: In the future, we will add a call to dbController.delete(tableToRemove)
            return true; // Removal was successful
        }
        
        //If the table was not found, return false
        return false;
    }

    
    ///// aiben al sharmuta
    ///asdasdasdasdasd
    
    
    
    
    /**
     * Purpose: Updates the seating capacity of a specific table.
     * Receives: int tableId, int newCapacity.
     * Returns: boolean (true if updated successfully).
     */
    public boolean updateTableCapacity(int tableId, int newCapacity) {
        //Search for the table using the given ID
        Table table = restaurant.getTableById(tableId);
        
        //If the table is found, update its capacity
        if (table != null) {
            table.setCapacity(newCapacity);
            
            // Note: In the future, a database update will be added here (UPDATE query)
            return true; // Update was successful
        }
        
        //If the table does not exist in the system
        return false;
    }

    
    
    
    
    

    /**
     * Purpose: Sets the standard operating hours for a specific day of the week.
     * Receives: DayOfWeek day, LocalTime open, LocalTime close.
     * Returns: void.
     */
    public void setRegularHours(DayOfWeek day, LocalTime open, LocalTime close) {
        // Call the method inside the Restaurant entity that updates the regular hours map
        restaurant.setRegularHours(day, open, close);
        
        System.out.println("Regular hours updated for " + day + ": " + open + " - " + close);
    }

    
    
    
    
    /**
     * Purpose: Sets special opening hours for a specific calendar date.
     * Receives: LocalDate date, LocalTime open, LocalTime close.
     * Returns: void.
     */
    public void setSpecialHours(LocalDate date, LocalTime open, LocalTime close) {
        // Update the special hours map inside the Restaurant entity
        restaurant.setSpecialHours(date, open, close);
        
        System.out.println("Special hours set for " + date + ": " + open + " - " + close);
    }


    
    
    
    

    /**
     * Purpose: Checks if the restaurant is open at a specific timestamp.
     * Receives: LocalDateTime dateTime.
     * Returns: boolean (true if open).
     */
    public boolean isRestaurantOpen(LocalDateTime dateTime) {
        //Split the LocalDateTime into separate date and time values
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();
        
        //Get the appropriate hours range for this date
        //(checks special hours first, then regular hours)
        //The getHoursForDate method already exists in the Restaurant class
        TimeRange hours = restaurant.getHoursForDate(date);
        
        //If no hours are defined for this date (neither special nor regular), the restaurant is closed
        if (hours == null) {
            return false;
        }
        
        // 4. Check whether the requested time falls within the range [openTime, closeTime]
        boolean isTooEarly = time.isBefore(hours.getOpenTime());
        boolean isTooLate = time.isAfter(hours.getCloseTime());
        
        return !isTooEarly && !isTooLate;
    }

    
    
   
        
    /**
     * Purpose: Finds a specific table by its unique identifier.
     * Receives: int tableId.
     * Returns: Table object (or null if not found).
     */
    public Table getTableById(int tableId) {
        // Use the existing method in the Restaurant entity that iterates over the table list
        return restaurant.getTableById(tableId);
    }

    
    
    
       
    /**
     * Purpose: Retrieves the full list of all tables in the restaurant.
     * Receives: None.
     * Returns: List<Table>.
     */
    public List<Table> getAllTables() {
        // Return the list stored inside the Restaurant object
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
