package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;

import MainControllers.DBController;

/**
 * Handles DB operations related to the waiting_list_entry table.
 * Provides utilities for inserting entries, updating status,
 * and querying waiting list and restaurant availability data.
 */
public class JoinWaitingListDBController {

    /**
     * Inserts a new entry into the waiting list table.
     * This method is used when no immediate table is available.
     *
     * @param confirmationCode Unique confirmation code
     * @param userId           User ID
     * @param numberOfGuests   Number of guests
     * @param status           Entry status (WAITING / ARRIVED)
     * @throws SQLException if a DB error occurs
     */
    public static void insertWaitingListEntry(
            long confirmationCode,
            int userId,
            int numberOfGuests,
            String status
    ) throws SQLException {

        String sql =
            "INSERT INTO waiting_list_entry " +
            "(confirmation_code, entry_time, number_of_guests, user_id, status, notification_time) " +
            "VALUES (?, NOW(), ?, ?, ?, NULL)";

        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, confirmationCode);
            ps.setInt(2, numberOfGuests);
            ps.setInt(3, userId);
            ps.setString(4, status);

            ps.executeUpdate();
        }
    }

    /**
     * Updates the status of an existing waiting list entry.
     * Also records the notification time for tracking purposes.
     *
     * @param confirmationCode Unique confirmation code
     * @param newStatus        New status value
     * @throws Exception if a DB error occurs
     */
    public static void updateStatus(
            long confirmationCode,
            String newStatus
    ) throws Exception {

        String sql =
            "UPDATE waiting_list_entry " +
            "SET status = ?, notification_time = NOW() " +
            "WHERE confirmation_code = ?";

        Connection conn = DBController.getInstance().getConnection(); 
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, confirmationCode);
            ps.executeUpdate();
        }
    }

    /**
     * Retrieves the current status of a waiting list entry
     * based on its confirmation code.
     *
     * @param confirmationCode Unique confirmation code
     * @return The current status string, or null if not found
     * @throws Exception if a DB error occurs
     */
    public static String getStatusByCode(long confirmationCode) throws Exception {
        String sql = "SELECT status FROM waiting_list_entry WHERE confirmation_code = ?";
        
        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, confirmationCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        }
        return null;
    }

    /**
     * Checks whether a given user already has an active
     * waiting list entry (WAITING or ARRIVED).
     * Used to prevent duplicate active entries.
     *
     * @param userId User identifier
     * @return true if the user already appears as active, false otherwise
     * @throws SQLException if a DB error occurs
     */
    public static boolean isUserAlreadyActive(int userId) throws SQLException {
        String sql =
            "SELECT COUNT(*) FROM waiting_list_entry " +
            "WHERE user_id = ? AND (status = 'WAITING' OR status = 'ARRIVED')";
        
        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether the restaurant is currently open.
     * The check prioritizes special hours for the current date.
     * If no special hours exist, regular weekly hours are used.
     *
     * @return true if the restaurant is open at the current time, false otherwise
     * @throws Exception if a DB error occurs
     */
    public static boolean isRestaurantOpenNow() throws Exception {

        Connection conn = DBController.getInstance().getConnection();
        LocalTime now = LocalTime.now();

        // --- Check special opening hours (override regular hours) ---
        String specialSql =
            "SELECT tr.open_time, tr.close_time " +
            "FROM restaurant_special_hours sh " +
            "JOIN time_range tr ON sh.time_range_id = tr.time_range_id " +
            "WHERE sh.special_date = CURDATE()";

        try (PreparedStatement ps = conn.prepareStatement(specialSql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                LocalTime open = rs.getTime("open_time").toLocalTime();
                LocalTime close = rs.getTime("close_time").toLocalTime();

                return !now.isBefore(open) && !now.isAfter(close);
            }
        }

        // --- Fallback to regular weekly opening hours ---
        String dayName = LocalDate.now()
        	    .getDayOfWeek()
        	    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        	String regularSql =
        	    "SELECT tr.open_time, tr.close_time " +
        	    "FROM restaurant_regular_hours rrh " +
        	    "JOIN time_range tr ON rrh.time_range_id = tr.time_range_id " +
        	    "WHERE rrh.day_of_week = ?";

        	try (PreparedStatement ps = conn.prepareStatement(regularSql)) {

        	    ps.setString(1, dayName);

        	    try (ResultSet rs = ps.executeQuery()) {

        	        if (!rs.next()) {
        	            return false;
        	        }

        	        LocalTime open = rs.getTime("open_time").toLocalTime();
        	        LocalTime close = rs.getTime("close_time").toLocalTime();

        	        // Closed day
        	        if (open.equals(close)) {
        	            return false;
        	        }

        	        return !now.isBefore(open) && !now.isAfter(close);
        	    }
        	}

    }
    
    /**
     * Checks whether the waiting list currently contains any active WAITING entries.
     * Used to prevent immediate entry when other guests are already waiting.
     *
     * @return true if the waiting list is not empty, false otherwise
     * @throws SQLException if a DB error occurs
     */
    public static boolean hasWaitingGuests() throws SQLException {

        String sql =
            "SELECT COUNT(*) " +
            "FROM waiting_list_entry " +
            "WHERE status = 'WAITING'";

        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}
