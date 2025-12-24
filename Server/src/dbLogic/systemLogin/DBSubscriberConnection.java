package dbLogic.systemLogin;

import MainControllers.DBController;
import dbLogic.ILoginDatabase;
import java.sql.*;

/**
 * The DBSubscriberConnection class provides the database access logic specifically 
 * for the Subscriber authentication flow.
 * * It implements the {@link ILoginDatabase} interface to integrate with the 
 * system's login subsystem, focusing on verifying registered restaurant subscribers.
 * * This class bridges the 'subscriber' table and the 'user' table to retrieve 
 * session-level identification.
 * * @author Software Engineering Student
 * @version 1.0
 */
public class DBSubscriberConnection implements ILoginDatabase {

    /**
     * Validates a subscriber based on their unique Subscriber ID.
     * Logic: Queries the 'subscriber' table to ensure the ID exists and the 
     * account status is currently 'Active'.
     * * <p>Implementation Detail:
     * If the subscriber is found and active, the method retrieves the associated 
     * 'user_id' (foreign key), which is used globally across the system to link 
     * reservations and orders to the specific person.</p>
     * * @param subID The numeric identification of the subscriber (BIGINT in SQL).
     * @return The unique 'user_id' (int) if the subscriber is valid and active; -1 otherwise.
     */
    @Override
    public int verifySubscriber(long subID) {
        // Access the shared singleton database connection
        Connection conn = DBController.getInstance().getConnection();
        
        // SQL query to verify identity and check for an active account status
        String sql = "SELECT user_id FROM subscriber WHERE subscriber_id = ? AND status = 'Active'";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, subID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Success: Return the internal user_id for session management
                    return rs.getInt("user_id"); 
                }
            }
        } catch (SQLException e) {
            // Logs SQL-specific errors (e.g., table not found, connection lost)
            e.printStackTrace();
        }
        
        // Return -1 to signal authentication failure or error to the caller
        return -1; 
    }

    /**
     * Interface Implementation: Occasional customer verification.
     * This method is not supported by the Subscriber-specific database connector.
     * @return always -1.
     */
    @Override
    public int verifyOccasional(String username, String contactInfo) {
        return -1; 
    }

    /**
     * Interface Implementation: Occasional customer registration.
     * This method is not supported by the Subscriber-specific database connector.
     * @return always false.
     */
    @Override
    public boolean registerOccasional(String username, String phone, String email) {
        return false; 
    }
}