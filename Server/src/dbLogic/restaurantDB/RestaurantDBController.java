package dbLogic.restaurantDB;

import java.sql.*;
import java.time.LocalDate;
import common.Restaurant;
import MainControllers.DBController;

/**
 * The RestaurantDBController is a specialized data access object (DAO) responsible for 
 * reconstructing a complete {@link Restaurant} domain entity from the database.
 * * It executes multiple relational queries to aggregate the restaurant's basic information, 
 * its physical table inventory, and its operational weekly schedule.
 * * This class interacts directly with the 'prototypedb' schema using JDBC.
 * @author Software Engineering Student
 * @version 1.0
 */
public class RestaurantDBController {

    /**
     * Performs a deep load of a restaurant's data based on its unique ID.
     * This method orchestrates three distinct data retrieval phases:
     * 1. Basic Metadata: Retrieves the restaurant's name and initializes the object.
     * 2. Inventory Aggregation: Performs a JOIN to calculate total table counts by capacity.
     * 3. Schedule Mapping: Joins weekly hours with time ranges to define operational windows.
     * * @param restaurantId The primary key of the restaurant to be loaded.
     * @return A fully populated {@link Restaurant} object, or null if the ID is not found.
     */
	
    public static Restaurant loadFullRestaurantData(int restaurantId) throws SQLException {
        Restaurant restaurant = null;
        // Accessing the shared database connection singleton
        Connection conn = DBController.getInstance().getConnection(); 

        try {
            // --- PHASE 1: Basic Identification ---
            // Retrieves the display name from the core 'restaurant' table.
            String queryName = "SELECT name FROM restaurant WHERE restaurant_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(queryName)) {
                stmt.setInt(1, restaurantId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    // Initialize the Restaurant DTO with ID and Name
                    restaurant = new Restaurant(restaurantId, rs.getString("name"));
                }
            }

            // Early exit if the restaurant does not exist in the database
            if (restaurant == null) return null;

            // --- PHASE 2: Physical Table Inventory ---
            /** * RELATIONAL JOIN LOGIC:
             * This query links 'restaurant_table' (the mapping table) with 'table' (the entity table).
             * It uses GROUP BY to aggregate how many tables of each seating capacity exist.
             * Example: Results might show 5 tables of capacity 2, and 10 tables of capacity 4.
             */
            String queryTables = "SELECT t.capacity, COUNT(*) as total " +
                                 "FROM restaurant_table rt " +
                                 "JOIN `table` t ON rt.table_id = t.table_id " +
                                 "WHERE rt.restaurant_id = ? GROUP BY t.capacity";
            try (PreparedStatement stmt = conn.prepareStatement(queryTables)) {
                stmt.setInt(1, restaurantId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    // Update the restaurant's internal inventory map
                    restaurant.addTablesToInventory(rs.getInt("capacity"), rs.getInt("total"));
                }
            }

            // --- PHASE 3: Regular Operating Hours ---
            /**
             * RELATIONAL JOIN LOGIC:
             * Links 'restaurant_regular_hours' with 'time_range' to fetch start and end strings.
             * This populates the weekly schedule (e.g., Monday: 08:00 - 22:00).
             */
            String queryHours = "SELECT rh.day_of_week, tr.open_time, tr.close_time " +
                                "FROM restaurant_regular_hours rh " +
                                "JOIN time_range tr ON rh.time_range_id = tr.time_range_id " +
                                "WHERE rh.restaurant_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(queryHours)) {
                stmt.setInt(1, restaurantId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    // Map the DB hours to the restaurant's operational schedule map
                    restaurant.setRegularHours(
                        rs.getString("day_of_week"), 
                        rs.getString("open_time"), 
                        rs.getString("close_time")
                    );
                }
            }
            
            
            // --- PHASE 4: Special Operating Hours Overrides ---
               /**
                * RELATIONAL JOIN LOGIC:
                * Fetches date-specific overrides from 'restaurant_special_hours' joined with 'time_range'.
                * This ensures that if a specific date (e.g., Holiday) has different hours, it is loaded into RAM.
                */
               String querySpecial = "SELECT sh.special_date, tr.open_time, tr.close_time " +
                                     "FROM restaurant_special_hours sh " +
                                     "JOIN time_range tr ON sh.time_range_id = tr.time_range_id " +
                                     "WHERE sh.restaurant_id = ?";
               try (PreparedStatement stmt = conn.prepareStatement(querySpecial)) {
                   stmt.setInt(1, restaurantId);
                   ResultSet rs = stmt.executeQuery();
                   while (rs.next()) {
                       // Convert SQL Date to Java LocalDate
                       LocalDate specialDate = rs.getDate("special_date").toLocalDate();
                       
                       // Populate the specialHours map in the Restaurant object
                       restaurant.setSpecialHours(
                           specialDate, 
                           rs.getString("open_time"), 
                           rs.getString("close_time")
                       );
                   }
               }

        } catch (SQLException e) { 
            // Standard JDBC error handling; prints the stack trace for server-side debugging
            e.printStackTrace(); 
        }
 
        
        return restaurant;
    }
}