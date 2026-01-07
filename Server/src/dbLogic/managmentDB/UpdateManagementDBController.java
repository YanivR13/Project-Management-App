package dbLogic.managmentDB; // Defining the package for database management logic

import java.sql.Connection; // Importing Connection for database connectivity
import java.sql.PreparedStatement; // Importing PreparedStatement for parameterized SQL queries
import java.sql.ResultSet; // Importing ResultSet to handle query results
import java.sql.SQLException; // Importing SQLException for database error handling
import java.sql.Statement; // Importing Statement to retrieve generated keys
import java.time.LocalDate; // Importing LocalDate for modern date handling
import java.util.Date; // Importing Date for legacy support if needed
import java.util.Map; // Importing Map for storing day-to-range associations
import MainControllers.DBController; // Importing the singleton DB controller
import MainControllers.ServerController;
import common.TimeRange; // Importing the TimeRange domain model
import dbLogic.restaurantDB.WaitingListController;

/**
 * Controller for managing database updates for the restaurant management system.
 * This class handles regular and special operating hours updates using SQL transactions.
 */
public class UpdateManagementDBController { // Start of the UpdateManagementDBController class

    /**
     * Updates or inserts regular operating hours for a restaurant.
     * Uses a transaction to ensure all days are updated together.
     */
    public static boolean updateRegularHours(int restaurantId, Map<String, TimeRange> newHours) { // Start method
        // Retrieve the active database connection from the singleton controller
        Connection conn = DBController.getInstance().getConnection(); // Get connection
        
        // SQL to insert a time range only if it does not already exist (ignoring duplicates)
        String insertRangeSql = "INSERT IGNORE INTO time_range (open_time, close_time) VALUES (?, ?)"; // SQL string
        
        // SQL to find the unique ID of a specific time range
        String findIdSql = "SELECT time_range_id FROM time_range WHERE open_time = ? AND close_time = ?"; // SQL string
        
        // SQL to perform an UPSERT: Insert new hours or update if the day already exists for this restaurant
        String updateDaySql = "INSERT INTO restaurant_regular_hours (restaurant_id, day_of_week, time_range_id) " +
                             "VALUES (?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE time_range_id = ?"; // SQL string

        try { // Start of the main database transaction block
            // Disable auto-commit to manually manage the transaction boundaries
            conn.setAutoCommit(false); // Set auto-commit to false

            // Iterate through the map containing days of the week and their respective time ranges
            for (Map.Entry<String, TimeRange> entry : newHours.entrySet()) { // Start of loop
                String day = entry.getKey(); // Extract the day name (key)
                TimeRange range = entry.getValue(); // Extract the TimeRange object (value)

                // STEP A: Ensure the time range exists in the time_range table
                try (PreparedStatement pstmt = conn.prepareStatement(insertRangeSql)) { // Prepare statement
                    pstmt.setString(1, range.getOpenTime()); // Bind opening time
                    pstmt.setString(2, range.getCloseTime()); // Bind closing time
                    pstmt.executeUpdate(); // Execute the insert (ignored if exists)
                } // End of inner try

                // STEP B: Retrieve the ID of the time range (either the existing one or the one just inserted)
                int timeRangeId = -1; // Initialize the ID variable
                try (PreparedStatement pstmt = conn.prepareStatement(findIdSql)) { // Prepare statement
                    pstmt.setString(1, range.getOpenTime()); // Bind opening time
                    pstmt.setString(2, range.getCloseTime()); // Bind closing time
                    try (ResultSet rs = pstmt.executeQuery()) { // Execute query
                        if (rs.next()) { // If a result is found
                            timeRangeId = rs.getInt("time_range_id"); // Capture the ID
                        } // End if
                    } // End result set try
                } // End find ID try

                // STEP C: Perform the Upsert into the regular hours table
                if (timeRangeId != -1) { // If a valid time range ID was acquired
                    try (PreparedStatement pstmt = conn.prepareStatement(updateDaySql)) { // Prepare statement
                        pstmt.setInt(1, restaurantId); // Bind restaurant ID
                        pstmt.setString(2, day); // Bind day of the week
                        pstmt.setInt(3, timeRangeId); // Bind time range ID for insert
                        pstmt.setInt(4, timeRangeId); // Bind time range ID for update (on duplicate)
                        pstmt.executeUpdate(); // Execute the UPSERT
                    } // End upsert try
                } // End valid ID check
            } // End of the days iteration loop

            // Commit all changes to the database as a single atomic unit
            conn.commit(); // Execute commit
            return true; // Return success

        } catch (SQLException e) { // Catch any database errors during the process
            try { // Attempt to rollback in case of an error
                if (conn != null) { // Check if connection is alive
                    conn.rollback(); // Undo all changes in this transaction
                } // End connection check
            } catch (SQLException ex) { // Catch rollback failure
                ex.printStackTrace(); // Log the rollback exception
            } // End rollback try
            e.printStackTrace(); // Log the original SQL exception
            return false; // Return failure
        } finally { // Finalize block to restore connection state
            try { // Attempt to reset auto-commit
                conn.setAutoCommit(true); // Re-enable auto-commit for future operations
            } catch (SQLException e) { // Catch reset failure
                e.printStackTrace(); // Log the error
            } // End reset try
        } // End of finally block
    } // End of updateRegularHours method

    /**
     * Updates or inserts special operating hours for a specific date.
     */
    public static boolean updateSpecialHours(int restaurantId, LocalDate date, String open, String close) { // Start method
        // Retrieve the database connection
        Connection conn = DBController.getInstance().getConnection(); // Get connection
        
        try { // Start transaction block
            // Disable auto-commit to start a manual transaction
            conn.setAutoCommit(false); // Begin transaction

            // STEP 1: Use helper method to get existing ID or create a new time range
            int timeRangeId = getOrCreateTimeRange(open, close); // Call helper
            
            // Logic validation: Check if the ID retrieval was successful
            if (timeRangeId == -1) { // If ID is invalid
                throw new SQLException("Failed to retrieve or create time_range_id"); // Trigger exception
            } // End ID check

            // STEP 2: Execute Upsert logic for the special hours table
            String sql = "INSERT INTO restaurant_special_hours (restaurant_id, special_date, time_range_id) " +
                         "VALUES (?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE time_range_id = ?"; // SQL string

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // Prepare statement
                pstmt.setInt(1, restaurantId); // Bind restaurant ID
                pstmt.setDate(2, java.sql.Date.valueOf(date)); // Bind the specific date
                pstmt.setInt(3, timeRangeId); // Bind time range ID for insert
                pstmt.setInt(4, timeRangeId); // Bind time range ID for update
                pstmt.executeUpdate(); // Execute the update
            } // End statement try

            // STEP 3: Finalize the transaction
            conn.commit(); // Save all changes
            return true; // Return success

        } catch (SQLException e) { // Handle errors
            try { // Rollback logic
                if (conn != null) { // If connection exists
                    conn.rollback(); // Rollback changes
                } // End check
            } catch (SQLException ex) { // Rollback error
                ex.printStackTrace(); // Print trace
            } // End rollback catch
            e.printStackTrace(); // Print original trace
            return false; // Return failure
        } finally { // Cleanup
            try { // Restoration
                conn.setAutoCommit(true); // Re-enable auto-commit
            } catch (SQLException e) { // Restoration error
                e.printStackTrace(); // Print trace
            } // End restore try
        } // End finally
    } // End of updateSpecialHours method

    /**
     * Helper method to find an existing time range ID or create a new one if missing.
     */
    private static int getOrCreateTimeRange(String open, String close) throws SQLException { // Start method
        // Access the connection
        Connection conn = DBController.getInstance().getConnection(); // Get connection
        
        // Search query to check for existing identical ranges
        String selectSql = "SELECT time_range_id FROM time_range WHERE open_time = ? AND close_time = ?"; // SQL string
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) { // Prepare statement
            pstmt.setString(1, open); // Bind open time
            pstmt.setString(2, close); // Bind close time
            ResultSet rs = pstmt.executeQuery(); // Execute search
            if (rs.next()) { // If found
                return rs.getInt(1); // Return the existing ID immediately
            } // End if
        } // End select try

        // Insertion query to create the range if it wasn't found
        String insertSql = "INSERT INTO time_range (open_time, close_time) VALUES (?, ?)"; // SQL string
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) { // Prepare statement
            pstmt.setString(1, open); // Bind open time
            pstmt.setString(2, close); // Bind close time
            if (pstmt.executeUpdate() > 0) { // If row was inserted
                ResultSet rs = pstmt.getGeneratedKeys(); // Get the new ID
                if (rs.next()) { // If ID exists
                    return rs.getInt(1); // Return the newly generated ID
                } // End ID if
            } // End update if
        } // End insert try
        
        return -1; // Return -1 if both lookup and insertion failed
    } // End of getOrCreateTimeRange method
    
    /**
     * Deletes all special operating hour overrides for a specific restaurant.
     * This effectively resets the restaurant to follow only its regular weekly schedule.
     * * @param restaurantId The unique ID of the restaurant.
     * @return true if the deletion was successful and committed; false otherwise.
     */
    public static boolean deleteAllSpecialHours(int restaurantId) { // Start of the method
        // Retrieve the active database connection from the singleton controller
        Connection conn = DBController.getInstance().getConnection(); // Get connection

        // SQL command to remove all records matching the restaurant ID from the special hours table
        String sql = "DELETE FROM restaurant_special_hours WHERE restaurant_id = ?"; //

        try { // Start transaction block
            // Disable auto-commit to manually control the transaction boundary
            conn.setAutoCommit(false); //

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // Prepare statement
                // Bind the restaurant ID to the placeholder
                pstmt.setInt(1, restaurantId); 

                // Execute the deletion query
                pstmt.executeUpdate(); 
            } // End statement try

            // Commit the transaction to persist the changes in the database
            conn.commit(); //
            return true; // Return success

        } catch (SQLException e) { // Handle potential database errors
            try { // Rollback logic in case of failure to maintain data integrity
                if (conn != null) { 
                    conn.rollback(); //
                } 
            } catch (SQLException ex) { 
                ex.printStackTrace(); 
            } 
            e.printStackTrace(); // Log the original exception details
            return false; // Return failure
        } finally { // Cleanup block
            try { // Restore the connection state for future operations
                conn.setAutoCommit(true); //
            } catch (SQLException e) { 
                e.printStackTrace(); 
            } 
        } // End finally
    } // End of deleteAllSpecialHours method

    /**
     * Alert when a guest has stayed for more than 2 hours.
     * Fixed: Now checks 'visit' table for actual 'start_time' instead of reservation time.
     */
    public static void checkStayDurationAlerts() {
        // השאילתה המעודכנת: בודקת בטבלת visit לפי start_time
        // הצטרפנו (JOIN) לטבלת reservation רק כדי שנוכל להציג את ה-user_id בהתראה אם תרצה
        String sql = "SELECT v.table_id, v.user_id, v.start_time, v.confirmation_code " +
                     "FROM visit v " +
                     "WHERE v.status = 'ARRIVED' " + // הנחה: 'ARRIVED' הוא הסטטוס ב-visit כשהם יושבים
                     "AND TIMESTAMPDIFF(MINUTE, v.start_time, NOW()) >= 120";

        // Singleton
        Connection conn = DBController.getInstance().getConnection();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
         
            while (rs.next()) {
                int tableId = rs.getInt("table_id");
                int userId = rs.getInt("user_id");
                String startTime = rs.getString("start_time");
                String confCode = rs.getString("confirmation_code");

                // הדפסת התראה ללוג של השרת
             // יצירת הודעת התראה ללוגר
                String alertMsg = String.format("[STAY ALERT] Table %d (User %d) has exceeded 2 hours. (Started: %s)", 
                                                tableId, userId, startTime);
                
                // שליחה ללוגר ב-Server UI
                ServerController.log(alertMsg);
                
                // כאן תוכל להוסיף לוגיקה נוספת, כמו שליחת התראה למלצר או למנהל
            }
        } catch (SQLException e) { 
            System.err.println("Error checking stay durations: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    /**
     * Auto cancel reservations if the guest is 15 minutes late.
     * If a reservation is canceled, it triggers the Waiting List logic.
     */
    /**
     * Auto cancel reservations if the guest is 15 minutes late.
     * A guest is considered late if they have an ACTIVE reservation 
     * but no corresponding entry in the 'visit' table after 15 minutes.
     */
    public static void cancelLateReservations() {
        // שאילתה שמוצאת הזמנות שזמנן עבר ואין להן ביקור תואם בטבלת visit
        String findLateSql = "SELECT r.confirmation_code FROM reservation r " +
                             "LEFT JOIN visit v ON r.confirmation_code = v.confirmation_code " +
                             "WHERE r.status = 'ACTIVE' " +
                             "AND v.confirmation_code IS NULL " + 
                             "AND TIMESTAMPDIFF(MINUTE, r.reservation_datetime, NOW()) > 15";

        String cancelSql = "UPDATE reservation SET status = 'NOSHOW' WHERE confirmation_code = ?";

        Connection conn = DBController.getInstance().getConnection();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(findLateSql)) {

            while (rs.next()) {
                String confCode = rs.getString("confirmation_code");

                // עדכון הסטטוס ל-NOSHOW
                try (PreparedStatement pstmt = conn.prepareStatement(cancelSql)) {
                    pstmt.setString(1, confCode);
                    int affected = pstmt.executeUpdate();

                    if (affected > 0) {
                    	ServerController.log("[AUTO-CANCEL] Reservation " + confCode + " marked as NOSHOW (15+ min late).");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during auto-cancel process: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a new subscriber in the system using an atomic SQL transaction.
     * @param phone The 10-digit phone number.
     * @param email The customer's email.
     * @return A Long (the new subscriber_id) if successful, or a String error message if a duplicate exists.
     */
    public static Object createNewSubscriber(String phone, String email) { // Method start
        // Get connection from the singleton controller
        Connection conn = DBController.getInstance().getConnection(); 
        
        // SQL 1: Check if phone already exists
        String checkSql = "SELECT user_id FROM user WHERE phone_number = ?";
        
        // SQL 2: Insert into user table and get back the user_id
        String insertUserSql = "INSERT INTO user (phone_number, email) VALUES (?, ?)";
        
        // SQL 3: Insert into subscriber table
        // username and qr_code remain NULL as requested. status is set to 'Active'.
        String insertSubSql = "INSERT INTO subscriber (user_id, subscriber_id, status) VALUES (?, ?, 'subscriber')";

        try { // Start transaction block
            conn.setAutoCommit(false); // Disable auto-commit for atomicity

            // --- STEP 1: Check for existing phone number ---
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, phone);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) { // If a record is found
                        return "Phone number already exists in the system."; // Return error message
                    }
                }
            }

            // --- STEP 2: Insert into 'user' table ---
            int newUserId = -1;
            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, phone);
                userStmt.setString(2, email);
                userStmt.executeUpdate();
                
                try (ResultSet keys = userStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        newUserId = keys.getInt(1); // Capture the auto-generated user_id
                    }
                }
            }

            if (newUserId == -1) throw new SQLException("Failed to generate User ID.");

            // --- STEP 3: Insert into 'subscriber' table ---
            // We will generate the subscriber_id based on a simple timestamp/random logic or DB sequence
            long generatedSubId = (long)(Math.random() * 900000) + 100000; // Example 6-digit subscriber ID

            try (PreparedStatement subStmt = conn.prepareStatement(insertSubSql)) {
                subStmt.setInt(1, newUserId); // Foreign key to user table
                subStmt.setLong(2, generatedSubId); // The unique subscriber_id
                subStmt.executeUpdate();
            }

            // --- STEP 4: Finalize Transaction ---
            conn.commit(); // Save changes
            return generatedSubId; // Return the ID to the handler

        } catch (SQLException e) { // Catch any SQL errors
            try {
                if (conn != null) conn.rollback(); // Undo changes on failure
            } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return "Critical database error occurred.";
        } finally { // Restore connection state
            try {
                conn.setAutoCommit(true); //
            } catch (SQLException e) { e.printStackTrace(); }
        } // End finally
    } // End method

} // End of class



