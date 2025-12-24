package dbLogic.systemLogin;

import MainControllers.DBController;
import dbLogic.ILoginDatabase;
import java.sql.*;

/**
 * The DBOccasionalConnection class manages the data access logic for Guest (Occasional) customers.
 * It coordinates data across two relational tables: 'user' (shared metadata) and 
 * 'occasional_customer' (guest-specific credentials).
 * * This class implements the {@link ILoginDatabase} interface to provide standard 
 * authentication and registration services within the login subsystem.
 * * @author Software Engineering Student
 * @version 1.0
 */
public class DBOccasionalConnection implements ILoginDatabase {

    /**
     * Authenticates an occasional customer by verifying their username and contact details.
     * Logic: Performs a JOIN between 'occasional_customer' and 'user' to ensure the 
     * username matches the provided phone number or email address.
     * * @param username The guest's unique identifier.
     * @param contact The guest's phone or email provided during login.
     * @return The unique 'user_id' if verification passes, or -1 if no match is found.
     */
    @Override
    public int verifyOccasional(String username, String contact) {
        Connection conn = DBController.getInstance().getConnection();
        
        // Relational Query: Links the guest identity to the core contact information
        String sql = "SELECT oc.user_id FROM occasional_customer oc " +
                     "JOIN user u ON oc.user_id = u.user_id " +
                     "WHERE oc.username = ? AND (u.phone_number = ? OR u.email = ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, contact);
            pstmt.setString(3, contact);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Updates the username for an existing occasional customer.
     * This method follows a strict validation flow:
     * 1. Verifies the existence of the contact information.
     * 2. Checks for username uniqueness to prevent collisions.
     * 3. Performs the update in the 'occasional_customer' table.
     * * @param contact The registered phone or email associated with the user.
     * @param newUsername The new desired username.
     * @return A status code (e.g., "RESET_USERNAME_SUCCESS") or a descriptive error.
     */
    public String resetUsername(String contact, String newUsername) {
        Connection conn = DBController.getInstance().getConnection();
        
        // Modular SQL queries for step-by-step validation
        String findUserSql = "SELECT user_id FROM user WHERE phone_number = ? OR email = ?";
        String checkUsernameSql = "SELECT user_id FROM occasional_customer WHERE username = ?";
        String updateSql = "UPDATE occasional_customer SET username = ? WHERE user_id = ?";

        try {
            // STEP 1: Identification - Find the internal UserID via the contact identifier
            int userId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(findUserSql)) {
                pstmt.setString(1, contact);
                pstmt.setString(2, contact);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                } else {
                    return "ERROR: Contact info not found.";
                }
            }

            // STEP 2: Uniqueness - Ensure the new username is not already claimed
            try (PreparedStatement pstmt = conn.prepareStatement(checkUsernameSql)) {
                pstmt.setString(1, newUsername);
                if (pstmt.executeQuery().next()) {
                    return "ERROR: Username '" + newUsername + "' is already taken.";
                }
            }

            // STEP 3: Execution - Update the database record
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, newUsername);
                pstmt.setInt(2, userId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    return "RESET_USERNAME_SUCCESS";
                } else {
                    return "ERROR: Could not update username.";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR: Database failure - " + e.getMessage();
        }
    }

    /**
     * Handles the dual-table registration process for a new guest.
     * Uses ATOMIC TRANSACTIONS (Commit/Rollback) to ensure that a 'user' record 
     * is only created if the 'occasional_customer' record succeeds, preventing orphaned rows.
     * * Process:
     * 1. Check uniqueness (Username and Contact).
     * 2. Insert into 'user' table and retrieve generated ID.
     * 3. Insert into 'occasional_customer' table using the new ID.
     * * @param username The desired guest username.
     * @param contact The phone number or email string.
     * @return REGISTRATION_SUCCESS or descriptive error string.
     */
    public String registerNewOccasional(String username, String contact) {
        Connection conn = DBController.getInstance().getConnection();

        String checkUserSql = "SELECT * FROM occasional_customer WHERE username = ?";
        String checkContactSql = "SELECT * FROM user WHERE phone_number = ? OR email = ?";
        String insertUserSql = "INSERT INTO user (phone_number, email) VALUES (?, ?)";
        String insertOccSql = "INSERT INTO occasional_customer (user_id, username) VALUES (?, ?)";

        try {
            // Initiate Transaction: Disable auto-commit to manage multi-step operations manually
            conn.setAutoCommit(false);

            // Phase 1: Pre-validation of identity uniqueness
            try (PreparedStatement pstmt = conn.prepareStatement(checkUserSql)) {
                pstmt.setString(1, username);
                if (pstmt.executeQuery().next()) {
                    conn.rollback();
                    return "ERROR: Username already exists.";
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(checkContactSql)) {
                pstmt.setString(1, contact);
                pstmt.setString(2, contact);
                if (pstmt.executeQuery().next()) {
                    conn.rollback();
                    return "ERROR: Contact info already exists.";
                }
            }

            // Phase 2: Metadata Creation - Create record in 'user' table
            int userId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                // Detect contact type: Email contains '@', otherwise treated as phone
                if (contact.contains("@")) {
                    pstmt.setNull(1, Types.VARCHAR);
                    pstmt.setString(2, contact);
                } else {
                    pstmt.setString(1, contact);
                    pstmt.setNull(2, Types.VARCHAR);
                }
                pstmt.executeUpdate();
                
                // Retrieve the auto-incremented primary key generated by MySQL
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    userId = rs.getInt(1);
                }
            }

            // Phase 3: Identity Creation - Link UserID to the guest username
            if (userId != -1) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertOccSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, username);
                    pstmt.executeUpdate();
                }
            } else {
                conn.rollback();
                return "ERROR: Failed to create user profile.";
            }

            // Phase 4: Finalization - Commit all changes to the database
            conn.commit();
            return "REGISTRATION_SUCCESS";

        } catch (SQLException e) {
            // Failure Management: Roll back changes if any step in the transaction fails
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        } finally {
            // Restore default database behavior
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --- Interface implementation methods for system-wide compatibility ---

    @Override
    public boolean registerOccasional(String username, String phone, String email) {
        String contact = (phone != null && !phone.isEmpty()) ? phone : email;
        return "REGISTRATION_SUCCESS".equals(registerNewOccasional(username, contact));
    }

    @Override
    public int verifySubscriber(long subID) {
        // Method is part of ILoginDatabase but not supported by this specific implementation
        return -1; 
    }
}