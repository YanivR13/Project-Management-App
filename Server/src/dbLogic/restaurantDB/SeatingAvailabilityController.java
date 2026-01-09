package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

import MainControllers.DBController;
import dbLogic.restaurantDB.TableDBController;

/**
 * Provides capacity-based availability checks for immediate seating.
 * Determines whether adding new guests would conflict with future reservations.
 */
public class SeatingAvailabilityController {

    /**
     * Determines whether incoming guests can be seated immediately
     * without exceeding the restaurant's total capacity when considering
     * future active reservations in a fixed time window.
     *
     * @param incomingGuests Number of guests attempting to enter immediately
     * @param now             Current timestamp
     * @return true if seating is possible without conflicts, false otherwise
     */
    public static boolean canSeatWithFutureReservations(int incomingGuests, LocalDateTime now) 
    {

        // Define the end of the future reservation window (fixed duration)
        LocalDateTime end = now.plusHours(2);

        // Count guests from future active reservations within the window
        int futureGuests = getFutureReservedGuests(now, end);

        // Retrieve the maximum seating capacity of the restaurant
        int restaurantCapacity = TableDBController.getRestaurantMaxCapacity();

        // Debug output for capacity calculation
        System.out.println("DEBUG – futureGuests: " + futureGuests);
        System.out.println("DEBUG – incomingGuests: " + incomingGuests);
        System.out.println("DEBUG – restaurantCapacity: " + restaurantCapacity);

        // Allow seating only if total guests do not exceed capacity
        return futureGuests + incomingGuests <= restaurantCapacity;
    }

    /**
     * Retrieves the total number of guests from ACTIVE reservations
     * within a given future time window.
     * This method aggregates reservation data to support capacity checks.
     *
     * @param start Start of the time window
     * @param end   End of the time window
     * @return Total number of guests reserved in the given interval
     */
    public static int getFutureReservedGuests(
            LocalDateTime start,
            LocalDateTime end
    ) {

        String sql =
            "SELECT COALESCE(SUM(number_of_guests), 0) " +
            "FROM reservation " +
            "WHERE reservation_datetime >= ? " +
            "AND reservation_datetime < ? " +
            "AND status = 'ACTIVE'";

        // Acquire database connection
        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	
            // Bind time window parameters
            ps.setObject(1, start);
            ps.setObject(2, end);

            // Execute query and extract aggregated result
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }

        } catch (Exception e) {
            // Escalate DB failures as runtime exceptions for upper-layer handling
            throw new RuntimeException("Failed to check future reservations", e);
        }
    }
}
