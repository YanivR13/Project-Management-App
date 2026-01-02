package dbLogic.systemLogin; // Defining the package for login-related database logic

import java.sql.Connection; // Importing Connection for database connectivity
import java.sql.PreparedStatement; // Importing PreparedStatement for parameterized SQL
import java.sql.SQLException; // Importing SQLException for database error handling
import java.util.ArrayList; // Importing ArrayList for dynamic query building
import java.util.List; // Importing List interface

import MainControllers.DBController; // Importing the singleton database controller

/**
 * The DBSubscriberDetails class provides methods to update personal 
 * information across the 'subscriber' and 'user' relational tables.
 */
public class DBSubscriberDetails { // Start of DBSubscriberDetails class definition

    /**
     * Updates subscriber personal details in the database.
     * Logic: Updates 'subscriber' table for username and 'user' table for contact info.
     * * @param userId   The unique identifier of the user
     * @param username The new username string (can be null/empty)
     * @param phone    The new phone number string (can be null/empty)
     * @param email    The new email address string (can be null/empty)
     * @return true if at least one database row was updated, false otherwise.
     */
    public boolean updateSubscriberDetails(int userId, String username, String phone, String email) { // Method start
        
        // Flag to track if any update operation successfully modified a row
        boolean updated = false; // Initializing state tracker

        try { // Beginning of the main database operation block
            
            // Retrieve the active database connection from the shared singleton controller
            Connection conn = DBController.getInstance().getConnection(); // Get connection

            // --- STEP 1: Update 'subscriber' Table (Username Logic) ---
            
            // Verification: Check if a new username was actually provided and is not whitespace
            if (username != null && !username.isBlank()) { // Start of username check
                
                // SQL query to update the username based on the unique user ID
                String sqlSubscriber = "UPDATE subscriber SET username = ? WHERE user_id = ?"; // SQL string
                
                // Preparing the statement for execution with resources management
                try (PreparedStatement ps = conn.prepareStatement(sqlSubscriber)) { // Initialize PreparedStatement
                    
                    // Binding the new username value to the first placeholder
                    ps.setString(1, username); // Set parameter 1
                    
                    // Binding the user ID to the second placeholder for the WHERE clause
                    ps.setInt(2, userId); // Set parameter 2
                    
                    // Execute the update and update the master flag if rows were affected
                    updated |= ps.executeUpdate() > 0; // Bitwise OR assignment for success tracking
                    
                } // End of subscriber statement try-block
                
            } // End of username update check

            // --- STEP 2: Update 'user' Table (Contact Info Logic - Dynamic SQL) ---
            
            // Lists to dynamically construct the SQL string and maintain parameter values
            List<String> fields = new ArrayList<>(); // To store column assignments (e.g., "email = ?")
            List<Object> values = new ArrayList<>(); // To store the values to be bound later

            // Logic check: Should we update the phone number?
            if (phone != null && !phone.isBlank()) { // Check for valid phone input
                fields.add("phone_number = ?"); // Add field assignment to list
                values.add(phone); // Add value to list
            } // End of phone check

            // Logic check: Should we update the email address?
            if (email != null && !email.isBlank()) { // Check for valid email input
                fields.add("email = ?"); // Add field assignment to list
                values.add(email); // Add value to list
            } // End of email check

            // Execution phase: Only proceed if there is at least one field to update
            if (!fields.isEmpty()) { // Start of dynamic user update
                
                // Constructing the SQL string by joining the fields list into a single comma-separated string
                String sqlUser = "UPDATE user SET " + String.join(", ", fields) + " WHERE user_id = ?"; // SQL builder
                
                // Preparing the dynamic statement for the 'user' table
                try (PreparedStatement ps = conn.prepareStatement(sqlUser)) { // Initialize PreparedStatement
                    
                    // Index tracker for binding parameters correctly
                    int paramIndex = 1; // Start at the first parameter
                    
                    // Iterate through the collected values and bind them to the statement
                    for (Object value : values) { // Start of binding loop
                        
                        // Binding the generic object to the current index
                        ps.setObject(paramIndex++, value); // Set value and increment index
                        
                    } // End of binding loop
                    
                    // Bind the user ID as the final parameter for the WHERE clause
                    ps.setInt(paramIndex, userId); // Bind identifying ID
                    
                    // Execute the multi-field update and update the master flag
                    updated |= ps.executeUpdate() > 0; // Bitwise OR assignment for cumulative success
                    
                } // End of user statement try-block
                
            } // End of dynamic fields check

        } catch (SQLException e) { // Handling database-level exceptions
            
            // Output the full technical stack trace for debugging and error logging
            e.printStackTrace(); // Printing error details
            
            // Return false immediately if a database error occurs during the process
            return false; // Error exit
            
        } // End of catch block

        // Return the final result: true if any record was touched, false if no changes were made
        return updated; // Final status return
        
    } // End of updateSubscriberDetails method
    
} // End of DBSubscriberDetails class definition