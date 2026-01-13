package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import MainControllers.DBController;
import common.Reservation;

/**
 * Controller class responsible for handling database operations related to viewing reservations.
 * This class interacts with the 'reservation' table in the database.
 */
public class viewReservationController {
	
	/**
	 * Fetches all ACTIVE reservations for a specific user from the database.
	 * This method maps database records to Reservation DTO objects.
	 * * @param userId The unique ID of the customer.
	 * @return A list of active Reservation objects belonging to the user.
	 */
	public static List<Reservation> getActiveReservationsByUserId(int userId) {
		
	    List<Reservation> activeReservations = new ArrayList<>();
	    
	   //SQL query to retrieve specific columns for active reservations of a given user
	    String query = "SELECT confirmation_code, reservation_datetime, number_of_guests, user_id, status " +
	                   "FROM reservation WHERE user_id = ? AND status = 'ACTIVE'";

        //Accessing the shared database connection singleton
	    Connection conn = DBController.getInstance().getConnection();
	    
	   //Using try-with-resources to ensure the PreparedStatement is closed automatically
	    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	    	
	    	//Bind the userId parameter to the first placeholder (?) in the query
	        pstmt.setInt(1, userId);
	        
	        //Execute the query and process the results
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	            	//Create a new Reservation object and map the standard fields from the result set
	                Reservation res = new Reservation(
	                    rs.getInt("user_id"),
	                    rs.getString("reservation_datetime"),
	                    rs.getInt("number_of_guests")
	                );
	                //Set the unique confirmation code (mapping long from DB)
	                res.setConfirmationCode(rs.getLong("confirmation_code"));
	                //Add the populated reservation object to the list
	                activeReservations.add(res);
	            }
	        }
	    } catch (SQLException e) {
	    	
	    	//Log database errors to the console (consider using a logger for production)
	        e.printStackTrace();
	    }
	    return activeReservations;
	}
	
	/**
	 * Updates the status of a specific reservation to 'CANCELED' in the database.
	 * @param confirmationCode The unique code of the reservation to cancel.
	 * @return true if the update was successful (rows affected > 0), false otherwise.
	 */
	public static boolean cancelReservationByCode(long confirmationCode) {
	    String query = "UPDATE reservation SET status = 'CANCELLED'"+ 
	    		       "WHERE confirmation_code = ? AND status = 'ACTIVE'";

	    Connection conn = DBController.getInstance().getConnection();
	    
	    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setLong(1, confirmationCode);
	        
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	        
	    } catch (SQLException e) {
	        System.out.println("DB Error during cancellation: " + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
	}
	
	
	/**
	 * Fetches ALL active reservations from the database for staff view.
	 * Uses a JOIN with the user table to include customer phone numbers.
	 */
	public static ArrayList<Object[]> getAllActiveReservations() {
	    ArrayList<Object[]> activeList = new ArrayList<>();
	    
	    // שימוש ב-LEFT JOIN כדי למשוך את מספר הטלפון מטבלת המשתמשים
	    String query = "SELECT r.confirmation_code, r.reservation_datetime, r.number_of_guests, u.phone_number, r.status " +
	                   "FROM prototypedb.reservation r " +
	                   "LEFT JOIN prototypedb.user u ON r.user_id = u.user_id " +
	                   "WHERE r.status = 'ACTIVE' " +
	                   "ORDER BY r.reservation_datetime ASC";

	    Connection conn = DBController.getInstance().getConnection();
	    try (PreparedStatement pstmt = conn.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {

	        while (rs.next()) {
	            Object[] row = new Object[] {
	                rs.getLong("confirmation_code"),
	                rs.getString("reservation_datetime"),
	                rs.getInt("number_of_guests"),
	                // אם הטלפון הוא NULL ב-DB, נציג הודעה מתאימה בטבלה
	                rs.getString("phone_number") != null ? rs.getString("phone_number") : "No Phone",
	                rs.getString("status")
	            };
	            activeList.add(row);
	        }
	    } catch (SQLException e) {
	        System.err.println("Database Error: " + e.getMessage());
	    }
	    return activeList;
	}
}
