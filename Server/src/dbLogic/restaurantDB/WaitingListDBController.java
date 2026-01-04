package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

import MainControllers.DBController;
import common.Reservation;

/**
 * Handles DB operations related to the waiting_list_entry table.
 */
public class WaitingListDBController {

    /**
     * Inserts a new entry into the waiting list.
     *
     * @param confirmationCode Reservation confirmation code
     * @param userId           User ID
     * @param numberOfGuests   Number of guests
     * @param entryTime        Time the user entered the waiting list
     * @throws SQLException if a DB error occurs
     */
    public static void insertWaitingListEntry(
            Reservation reservation,
            long confirmationCode
    ) throws SQLException {

        String sql =
            "INSERT INTO waiting_list_entry " +
            "(confirmation_code, entry_time, number_of_guests, user_id, status, notification_time) " +
            "VALUES (?, ?, ?, ?, ?, NULL)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBController.getInstance().getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, confirmationCode);
            pstmt.setObject(2, reservation.getReservationDateTime()); 
            pstmt.setInt(3, reservation.getNumberOfGuests());
            pstmt.setInt(4, reservation.getUserId());
            pstmt.setString(5, "WAITING");

            pstmt.executeUpdate();

        } finally {
            if (pstmt != null) pstmt.close();
        }
    }
}
