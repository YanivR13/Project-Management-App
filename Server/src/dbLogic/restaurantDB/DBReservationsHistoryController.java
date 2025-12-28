package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import MainControllers.DBController;
import common.Reservation;
import common.Reservation.ReservationStatus;

public class DBReservationsHistoryController {
    /**
     * Retrieves all reservations for a specific user from the database.
     */
    public List<Reservation> getReservationsForUser(int userId) {

        List<Reservation> reservations = new ArrayList<>();

        String sql = """
            SELECT confirmation_code,
                   reservation_datetime,
                   number_of_guests,
                   status
            FROM reservation
            WHERE user_id = ?
            ORDER BY reservation_datetime DESC
        """;

        try {
            Connection conn = DBController.getInstance().getConnection();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {

                    // Create Reservation using existing constructor
                    Reservation reservation = new Reservation(
                            userId,
                            rs.getString("reservation_datetime"),
                            rs.getInt("number_of_guests")
                    );

                    // Set confirmation code
                    reservation.setConfirmationCode(
                            rs.getLong("confirmation_code")
                    );

                    // Set status (String -> Enum)
                    try {
                        ReservationStatus status =
                                ReservationStatus.valueOf(rs.getString("status"));
                        reservation.setStatus(status);
                    } catch (Exception e) {
                        // אם הסטטוס לא תקין – נשאר ACTIVE כברירת מחדל
                    }

                    reservations.add(reservation);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return reservations;
    }

}
