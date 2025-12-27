package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import MainControllers.DBController;
import common.Reservation;

public class viewReservationController {
	
	/**
	 * Fetches all ACTIVE reservations for a specific user from the database.
	 * Aligned with the provided Reservation DTO and 'prototypedb' schema.
	 * * @param userId The ID of the customer whose reservations are being requested.
	 * @return A list of active Reservation objects.
	 */
	public static List<Reservation> getActiveReservationsByUserId(int userId) {
	    List<Reservation> activeReservations = new ArrayList<>();
	    String query = "SELECT confirmation_code, reservation_datetime, number_of_guests, user_id, status " +
	                   "FROM reservation WHERE user_id = ? AND status = 'ACTIVE'";

	    // מקבלים את החיבור מה-DBController בלי להכניס אותו ל-try-with-resources
	    Connection conn = DBController.getInstance().getConnection();
	    
	    // רק ה-PreparedStatement וה-ResultSet נסגרים אוטומטית
	    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setInt(1, userId);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                Reservation res = new Reservation(
	                    rs.getInt("user_id"),
	                    rs.getString("reservation_datetime"),
	                    rs.getInt("number_of_guests")
	                );
	                res.setConfirmationCode(rs.getLong("confirmation_code"));
	                activeReservations.add(res);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return activeReservations;
	}
}
