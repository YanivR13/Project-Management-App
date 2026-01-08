package dbLogic.systemLogin; // Defining the package for login-related database logic

import MainControllers.DBController; // Importing the singleton database controller to manage connections
import dbLogic.ILoginDatabase; // Importing the login database interface for architectural consistency
import java.sql.*; // Importing standard Java SQL classes for JDBC interaction

/**
 * The DBSubscriberConnection class provides the database access logic specifically 
 * for the Subscriber authentication flow.
 */
public class DBSubscriberConnection implements ILoginDatabase { // Beginning of class definition implementing ILoginDatabase

    /**
     * Validates a subscriber based on their unique Subscriber ID.
     * @param subID The numeric identification of the subscriber.
     * @return The unique 'user_id' (int) if valid; -1 otherwise.
     */
    @Override // Indicating that this method overrides an interface definition
    public int verifySubscriber(long subID) { // Start of verifySubscriber method
        
        // Accessing the shared database connection instance from the DBController singleton
        Connection conn = DBController.getInstance().getConnection(); // Retrieving the connection
        
        // Defining the SQL query to check for a valid subscriber ID with an 'Active' status
        String sql = "SELECT user_id FROM subscriber WHERE subscriber_id = ? AND status = 'subscriber'"; // SQL query string
        
        // Using try-with-resources to ensure the PreparedStatement is automatically closed
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // Initializing the statement
            
            // Binding the provided subID parameter to the first placeholder (?) in the query
            pstmt.setLong(1, subID); // Assigning the numeric ID
            
            // Executing the query and storing the outcome in a ResultSet object
            try (ResultSet rs = pstmt.executeQuery()) { // Start of ResultSet try-block
                
                // Checking if the database returned at least one matching row
                if (rs.next()) { // Start of result processing
                    
                    // Retrieve and return the internal user_id associated with this subscriber
                    return rs.getInt("user_id"); // Returning the ID from the DB
                    
                } // End of if-condition for matching results
                
            } // End of inner try-block for ResultSet
            
        } catch (SQLException e) { // Handling any database-specific exceptions (e.g., connectivity issues)
            
            // Outputting the technical stack trace to the system log for server-side debugging
            e.printStackTrace(); // Printing error details
            
        } // End of outer catch-block for SQLException
        
        // Returning -1 as a default value to indicate that authentication failed or an error occurred
        return -1; // Default failure response
        
    } // End of verifySubscriber method

    /**
     * Interface Implementation: Occasional customer verification.
     * @return always -1 as this class is Subscriber-specific.
     */
    @Override // Implementing interface method
    public int verifyOccasional(String username, String contactInfo) { // Start of verifyOccasional method
        
        // This specific connector does not handle guests, so it returns a failure code
        return -1; // Standard failure return
        
    } // End of verifyOccasional method

    /**
     * Interface Implementation: Occasional customer registration.
     * @return always false as this class is Subscriber-specific.
     */
    @Override // Implementing interface method
    public boolean registerOccasional(String username, String phone, String email) { // Start of registerOccasional method
        
        // This specific connector does not handle guest registration
        return false; // Standard failure return
        
    } // End of registerOccasional method
    
} // End of DBSubscriberConnection class definition