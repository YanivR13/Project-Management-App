package dbLogic.systemLogin; // Defining the package for login database logic

import MainControllers.DBController; // Importing the singleton database controller
import dbLogic.ILoginDatabase; // Importing the login database interface
import java.sql.*; // Importing standard SQL classes for JDBC

/**
 * The DBOccasionalConnection class manages the data access logic for Guest (Occasional) customers.
 * It implements the ILoginDatabase interface to provide authentication and registration services.
 */
public class DBOccasionalConnection implements ILoginDatabase { // Class start

    /**
     * Authenticates an occasional customer by verifying their username and contact details.
     */
    @Override // Implementing method from ILoginDatabase interface
    public int verifyOccasional(String username, String contact) { // Method start
        // Retrieve the shared database connection from the singleton instance
        Connection conn = DBController.getInstance().getConnection(); // Get connection
        
        // SQL query: Join occasional_customer with user table to verify identity and contact info
        String sql = "SELECT oc.user_id FROM occasional_customer oc " + // Select user_id
                     "JOIN user u ON oc.user_id = u.user_id " + // Join on common ID
                     "WHERE oc.username = ? AND (u.phone_number = ? OR u.email = ?)"; // Filter by credentials
        
        // Use try-with-resources to ensure the PreparedStatement is closed automatically
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // Start try block
            // Bind the username parameter to the first placeholder
            pstmt.setString(1, username); // Set parameter 1
            // Bind the contact info (phone/email) to the second placeholder
            pstmt.setString(2, contact); // Set parameter 2
            // Bind the same contact info to the third placeholder (Email check)
            pstmt.setString(3, contact); // Set parameter 3
            
            // Execute the query and capture the results in a ResultSet
            try (ResultSet rs = pstmt.executeQuery()) { // Start inner try for ResultSet
                // Check if a matching record was found in the database
                if (rs.next()) { // Start if result exists
                    // Return the unique user_id found for this guest
                    return rs.getInt("user_id"); // Return ID
                } // End if
            } // End of ResultSet try
        } catch (SQLException e) { // Catch block for SQL errors
            // Print the technical stack trace for server-side debugging
            e.printStackTrace(); // Log error
        } // End of main try block
        
        // Return -1 if no matching user was found or an exception occurred
        return -1; // Default failure return
    } // End of verifyOccasional method

    /**
     * Updates the username for an existing occasional customer with strict validation.
     */
    public String resetUsername(String contact, String newUsername) { // Method start
        // Retrieve the database connection
        Connection conn = DBController.getInstance().getConnection(); // Get connection
        
        // Query to find the internal user_id based on phone or email
        String findUserSql = "SELECT user_id FROM user WHERE phone_number = ? OR email = ?"; // SQL string
        // Query to check if the new desired username is already occupied
        String checkUsernameSql = "SELECT user_id FROM occasional_customer WHERE username = ?"; // SQL string
        // Query to perform the actual update
        String updateSql = "UPDATE occasional_customer SET username = ? WHERE user_id = ?"; // SQL string

        try { // Start of main logic try block
            
            // --- STEP 1: Identification ---
            int userId = -1; // Initialize the user ID variable
            try (PreparedStatement pstmt = conn.prepareStatement(findUserSql)) { // Start finding ID
                pstmt.setString(1, contact); // Bind contact to param 1
                pstmt.setString(2, contact); // Bind contact to param 2
                ResultSet rs = pstmt.executeQuery(); // Execute query
                if (rs.next()) { // If user found
                    userId = rs.getInt("user_id"); // Retrieve the ID
                } else { // If no user found
                    return "ERROR: Contact info not found."; // Return descriptive error
                } // End if-else
            } // End identification try

            // --- STEP 2: Uniqueness Check ---
            try (PreparedStatement pstmt = conn.prepareStatement(checkUsernameSql)) { // Start uniqueness check
                pstmt.setString(1, newUsername); // Bind new name
                if (pstmt.executeQuery().next()) { // If name already exists
                    return "ERROR: Username '" + newUsername + "' is already taken."; // Return error
                } // End if
            } // End uniqueness try

            // --- STEP 3: Database Execution ---
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) { // Start update execution
                pstmt.setString(1, newUsername); // Bind new name
                pstmt.setInt(2, userId); // Bind identifying ID
                int affectedRows = pstmt.executeUpdate(); // Execute update
                
                // Return success if at least one row was modified
                if (affectedRows > 0) { // Check affected rows
                    return "RESET_USERNAME_SUCCESS"; // Success return
                } else { // If update failed to find row
                    return "ERROR: Could not update username."; // Return error
                } // End if-else
            } // End update execution try

        } catch (SQLException e) { // Catch database failures
            e.printStackTrace(); // Print technical error
            return "ERROR: Database failure - " + e.getMessage(); // Return technical message
        } // End of main try-catch
    } // End of resetUsername method

    /**
     * Handles guest registration using atomic transactions.
     */
    public String registerNewOccasional(String username, String contact) { // Method start
        // Retrieve the database connection
        Connection conn = DBController.getInstance().getConnection(); // Get connection

        // SQL commands for validation and multi-table insertion
        String checkUserSql = "SELECT * FROM occasional_customer WHERE username = ?"; // SQL string
        String checkContactSql = "SELECT * FROM user WHERE phone_number = ? OR email = ?"; // SQL string
        String insertUserSql = "INSERT INTO user (phone_number, email) VALUES (?, ?)"; // SQL string
        String insertOccSql = "INSERT INTO occasional_customer (user_id, username) VALUES (?, ?)"; // SQL string

        try { // Start of registration try block
            
            // TRANSACTION START: Disable auto-commit to ensure atomicity
            conn.setAutoCommit(false); // Begin manual transaction

            // --- Phase 1: Pre-validation of identity uniqueness ---
            try (PreparedStatement pstmt = conn.prepareStatement(checkUserSql)) { // Check username
                pstmt.setString(1, username); // Bind name
                if (pstmt.executeQuery().next()) { // If exists
                    conn.rollback(); // Undo potential changes
                    return "ERROR: Username already exists."; // Return error
                } // End if
            } // End username check try

            try (PreparedStatement pstmt = conn.prepareStatement(checkContactSql)) { // Check contact info
                pstmt.setString(1, contact); // Bind contact to param 1
                pstmt.setString(2, contact); // Bind contact to param 2
                if (pstmt.executeQuery().next()) { // If exists
                    conn.rollback(); // Undo potential changes
                    return "ERROR: Contact info already exists."; // Return error
                } // End if
            } // End contact check try

            // --- Phase 2: Metadata Creation (User Table) ---
            int userId = -1; // Initialize generated ID holder
            try (PreparedStatement pstmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) { // Insert with key retrieval
                // Logic: Assign value to either phone or email based on content
                if (contact.contains("@")) { // Check if input is an email
                    pstmt.setNull(1, Types.VARCHAR); // Set phone to NULL
                    pstmt.setString(2, contact); // Set email to value
                } else { // Otherwise treat as phone
                    pstmt.setString(1, contact); // Set phone to value
                    pstmt.setNull(2, Types.VARCHAR); // Set email to NULL
                } // End if-else
                
                pstmt.executeUpdate(); // Execute metadata creation
                
                // Retrieve the auto-generated primary key from MySQL
                ResultSet rs = pstmt.getGeneratedKeys(); // Get keys
                if (rs.next()) { // If key returned
                    userId = rs.getInt(1); // Extract the numeric ID
                } // End if
            } // End metadata try

            // --- Phase 3: Identity Linkage (Occasional Customer Table) ---
            if (userId != -1) { // If metadata was created successfully
                try (PreparedStatement pstmt = conn.prepareStatement(insertOccSql)) { // Link ID to username
                    pstmt.setInt(1, userId); // Bind generated ID
                    pstmt.setString(2, username); // Bind desired name
                    pstmt.executeUpdate(); // Execute linkage
                } // End linkage try
            } else { // If user_id retrieval failed
                conn.rollback(); // Abort entire transaction
                return "ERROR: Failed to create user profile."; // Return failure
            } // End if-else

            // --- Phase 4: Finalization ---
            conn.commit(); // Permanently save all changes in the transaction
            return "REGISTRATION_SUCCESS"; // Return success signal

        } catch (SQLException e) { // Handle exceptions
            // TRANSACTION FAILURE: Roll back all changes to prevent corrupted data
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // Perform rollback
            e.printStackTrace(); // Print technical error
            return "ERROR: " + e.getMessage(); // Return exception details
        } finally { // Final cleanup
            // Restoring default database behavior
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); } // Re-enable auto-commit
        } // End of main block
    } // End of registerNewOccasional method

    // --- Interface Compatibility Layer ---

    /**
     * Implementation for the ILoginDatabase interface registration logic.
     */
    @Override // Overriding method from interface
    public boolean registerOccasional(String username, String phone, String email) { // Method start
        // Refactored logic: Select which contact string is available
        String contact = (phone != null && !phone.isEmpty()) ? phone : email; // Determine active contact string
        // Invoke the primary registration method and compare result to success string
        return "REGISTRATION_SUCCESS".equals(registerNewOccasional(username, contact)); // Return boolean result
    } // End method

    /**
     * Interface stub: Subscriber verification is not handled by this Occasional-specific class.
     */
    @Override // Implementing interface member
    public int verifySubscriber(long subID) { // Method start
        // Return -1 to indicate that this implementation does not support subscriber login
        return -1; // Standard failure code
    } // End method
    
} // End of DBOccasionalConnection class