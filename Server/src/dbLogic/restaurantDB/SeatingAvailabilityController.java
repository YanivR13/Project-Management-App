package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

import MainControllers.DBController;
import dbLogic.restaurantDB.TableDBController;

public class SeatingAvailabilityController {

    public static boolean canSeatWithFutureReservations(
            int incomingGuests,
            LocalDateTime now
    ) {

        LocalDateTime end = now.plusHours(2);

        int futureGuests = getFutureReservedGuests(now, end);
        int restaurantCapacity = TableDBController.getRestaurantMaxCapacity();

        System.out.println("DEBUG – futureGuests: " + futureGuests);
        System.out.println("DEBUG – incomingGuests: " + incomingGuests);
        System.out.println("DEBUG – restaurantCapacity: " + restaurantCapacity);

        return futureGuests + incomingGuests <= restaurantCapacity;
    }

    /**
     * Counts how many guests are already reserved in the given time window.
     */
    private static int getFutureReservedGuests(
            LocalDateTime start,
            LocalDateTime end
    ) {

        String sql =
            "SELECT COALESCE(SUM(number_of_guests), 0) " +
            "FROM reservation " +
            "WHERE reservation_datetime >= ? " +
            "AND reservation_datetime < ? " +
            "AND status = 'ACTIVE'";

        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	
            ps.setObject(1, start);
            ps.setObject(2, end);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }

        } catch (Exception e) {
        	 throw new RuntimeException("Failed to check future reservations", e);
        }
    }
}
