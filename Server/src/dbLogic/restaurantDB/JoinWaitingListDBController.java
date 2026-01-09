package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

import MainControllers.DBController;

/**
 * Handles DB operations related to the waiting_list_entry table.
 */
public class JoinWaitingListDBController {

    /**
     * Inserts a new entry into the waiting list.
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
    
 // בתוך JoinWaitingListDBController.java

    public static boolean isUserAlreadyActive(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM waiting_list_entry WHERE user_id = ? AND (status = 'WAITING' OR status = 'ARRIVED')";
        
        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // מחזיר true אם נמצאה לפחות רשומה אחת
                }
            }
        }
        return false;
    }
    
    public static boolean isRestaurantOpenNow() throws Exception {

        Connection conn = DBController.getInstance().getConnection();
        LocalTime now = LocalTime.now();

        // --------------------------------------------------
        //  בדיקת שעות SPECIAL (אם קיימות – הן מנצחות)
        // --------------------------------------------------
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

        // --------------------------------------------------
        //  אם אין SPECIAL – בדיקת שעות רגילות
        // --------------------------------------------------
        String regularSql =
        	    "SELECT tr.open_time, tr.close_time " +
        	    	    "FROM restaurant_regular_hours rrh " +
        	    	    "JOIN time_range tr ON rrh.time_range_id = tr.time_range_id " +
        	    	    "WHERE rrh.day_of_week = DAYOFWEEK(CURDATE())";;

        try (PreparedStatement ps = conn.prepareStatement(regularSql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return false; // אין שעות מוגדרות ליום הזה
            }

            LocalTime open = rs.getTime("open_time").toLocalTime();
            LocalTime close = rs.getTime("close_time").toLocalTime();

            return !now.isBefore(open) && !now.isAfter(close);
        }
    }

}
