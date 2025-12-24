package dbLogic.restaurantDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import MainControllers.DBController;
import common.Reservation;
import common.Restaurant;
import common.ServiceResponse; 
import common.ServiceResponse.ReservationResponseStatus; 
import serverLogic.serverRestaurant.RestaurantManager;

/**
 * The CreateOrderController handles the core Server-side business logic for 
 * allocating restaurant tables and managing the reservation lifecycle.
 * * This controller acts as a bridge between the OCSF network layer and the MySQL database,
 * implementing complex logic such as automated table matching and alternative date suggestions.
 * * Database Schema: Aligned with the 'prototypedb' schema.
 * @author Software Engineering Student
 */
public class CreateOrderController {

    /** Formatter for SQL-compatible DATETIME strings (YYYY-MM-DD HH:mm:ss) */
    private static final DateTimeFormatter sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");

    /**
     * Entry point for processing a new reservation request from the Client.
     * The method implements a three-phase decision engine:
     * 1. Direct Availability: Tries to find a table for the requested time.
     * 2. Intelligent Suggestion: If full, looks ahead 3 days for the same time slot.
     * 3. Rejection: Informs the client if no capacity exists in the lookahead period.
     * * @param res The {@link Reservation} DTO containing requested date, guests, and user ID.
     * @return A {@link ServiceResponse} wrapping the status (Success/Suggestion/Full) and payload.
     */
    public static ServiceResponse processNewReservation(Reservation res) {
        try {
            // Parse the incoming date string into a Java LocalDateTime object for manipulation
            LocalDateTime requestedDT = LocalDateTime.parse(res.getReservationDateTime(), sqlFormatter);
            
            // Retrieve the shared restaurant instance (inventory and hours)
            Restaurant restaurant = RestaurantManager.getInstance();

            if (restaurant == null) {
                return new ServiceResponse(ReservationResponseStatus.INTERNAL_ERROR, "Server Error: Restaurant data not initialized.");
            }

            // --- PHASE 1: Direct Availability Check ---
            // Attempt to find the best-fitting table for the exact requested date/time
            int allocatedTableSize = findAvailableTableSize(restaurant, requestedDT, res.getNumberOfGuests());

            if (allocatedTableSize != -1) {
                /** * SUCCESS SCENARIO:
                 * Table found. Proceed to persist the record in MySQL.
                 * The 'confCode' is the BIGINT generated primary key from the 'reservation' table.
                 */
                Long confCode = saveNewReservation(res, allocatedTableSize);
                
                if (confCode != null) {
                    return new ServiceResponse(ReservationResponseStatus.RESERVATION_SUCCESS, confCode);
                }
                return new ServiceResponse(ReservationResponseStatus.INTERNAL_ERROR, "Database failed to save the reservation.");
            }

            // --- PHASE 2: 3-Day Lookahead (Intelligent Suggestions) ---
            /**
             * If the requested slot is full, the system automatically checks the next 3 days
             * at the same time slot to provide the user with an alternative.
             */
            for (int i = 1; i <= 3; i++) {
                LocalDateTime nextDate = requestedDT.plusDays(i);
                if (findAvailableTableSize(restaurant, nextDate, res.getNumberOfGuests()) != -1) {
                    // Alternative slot found; return it as a formatted String for the Client to display
                    String suggestion = nextDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    return new ServiceResponse(ReservationResponseStatus.RESERVATION_SUGGESTION, suggestion);
                }
            }

            // --- PHASE 3: Capacity Reached ---
            // No tables available in the initial slot or the 3-day lookahead window.
            return new ServiceResponse(ReservationResponseStatus.RESERVATION_FULL, "No tables available for the next 3 days.");

        } catch (Exception e) {
            // Generic exception catch-all for parsing or logic errors
            e.printStackTrace();
            return new ServiceResponse(ReservationResponseStatus.INTERNAL_ERROR, "Server Exception: " + e.getMessage());
        }
    }

    /**
     * Logic for matching the requested party size to the physical table inventory.
     * It iterates through available capacities to find the smallest table that fits.
     * * @param restaurant The restaurant entity containing the full {@link Map} of table inventory.
     * @param dt The target date and time.
     * @param requestedSize The number of guests attending.
     * @return The capacity of the allocated table, or -1 if no fit exists.
     */
    private static int findAvailableTableSize(Restaurant restaurant, LocalDateTime dt, int requestedSize) {
        Map<Integer, Integer> inventory = restaurant.getFullInventory();
        
        // Extract and sort table sizes to ensure the "Best Fit" (smallest possible table) is picked first
        List<Integer> sortedCapacities = new ArrayList<>(inventory.keySet());
        Collections.sort(sortedCapacities);

        for (int capacity : sortedCapacities) {
            // Skip tables that are too small for the requested party
            if (capacity < requestedSize) continue;
            
            /**
             * AVAILABILITY RULE:
             * A table size is considered available if the current count of existing 
             * reservations for that size/time is less than the total physical inventory.
             */
            if (getReservedTablesCount(dt, capacity) < inventory.get(capacity)) {
                return capacity; // Found an available slot for this table size
            }
        }
        return -1;
    }

    /**
     * SQL Engine: Calculates table occupancy within a 4-hour temporal window.
     * Logic: A reservation at 14:00 occupies a table for 2 hours. Therefore, we check 
     * the window from (Requested - 2hr) to (Requested + 2hr) to ensure no overlaps.
     * * @param dt The requested reservation timestamp.
     * @param capacity The table size being checked.
     * @return Count of tables already booked in this time window.
     */
    private static int getReservedTablesCount(LocalDateTime dt, int capacity) {
        // Query targets the 'reservation' table using DATE_SUB and DATE_ADD for windowing
        String query = "SELECT COUNT(*) FROM reservation " +
                       "WHERE number_of_guests = ? " +
                       "AND reservation_datetime > DATE_SUB(?, INTERVAL 2 HOUR) " +
                       "AND reservation_datetime < DATE_ADD(?, INTERVAL 2 HOUR)";
        
        try (PreparedStatement pstmt = DBController.getInstance().getConnection().prepareStatement(query)) {
            pstmt.setInt(1, capacity);
            pstmt.setString(2, dt.format(sqlFormatter));
            pstmt.setString(3, dt.format(sqlFormatter));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        // Failure Policy: If the DB check fails, we assume full capacity to prevent overbooking
        return 999; 
    }

    /**
     * Database Persistence: Records the new reservation in MySQL.
     * Uses a PreparedStatement with RETURN_GENERATED_KEYS to retrieve the 
     * BIGINT Confirmation Code automatically assigned by the DB.
     * * @param res The reservation details.
     * @param finalTableSize The actual table capacity allocated (e.g., party of 3 gets a table of 4).
     * @return The unique Confirmation Code (Long) generated by the DB, or null on failure.
     */
    private static Long saveNewReservation(Reservation res, int finalTableSize) {
        String query = "INSERT INTO reservation (reservation_datetime, number_of_guests, user_id, status) VALUES (?, ?, ?, ?)";
        
        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // Map DTO fields to SQL parameters
            pstmt.setString(1, res.getReservationDateTime());
            pstmt.setInt(2, finalTableSize); // Log the table capacity used, not just guest count
            pstmt.setInt(3, res.getUserId()); 
            pstmt.setString(4, res.getStatusString());

            if (pstmt.executeUpdate() > 0) {
                // Retrieve the auto-incremented BIGINT confirmation_code
                try (ResultSet gk = pstmt.getGeneratedKeys()) {
                    if (gk.next()) return gk.getLong(1); 
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }
}