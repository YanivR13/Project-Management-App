package dbLogic.restaurantDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.Duration;
import MainControllers.DBController;

/**
 * VisitController handles the arrival logic and seating transactions at the Terminal.
 * Logic: Prioritizes NOTIFIED guests and ensures fair table allocation.
 */
public class VisitController {

    /**
     * Entry point for Terminal arrival.
     * @param code The confirmation code entered by the customer.
     * @return Status message for the UI.
     */
    public static String processTerminalArrival(long code) {
        Connection conn = DBController.getInstance().getConnection();
        try {
            // 1. Search in confirmed reservations (Including Waiting and Notified statuses)
            String resQuery = "SELECT * FROM reservation WHERE confirmation_code = ? " +
                               "AND status IN ('ACTIVE', 'WAITING_AT_RESTAURANT', 'NOTIFIED')";
            try (PreparedStatement ps = conn.prepareStatement(resQuery)) {
                ps.setLong(1, code);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return handleReservationFlow(conn, rs, code);
                }
            }

            // 2. Search in notified/waiting list entries
            String waitQuery = "SELECT * FROM waiting_list_entry WHERE confirmation_code = ? " +
                                "AND status IN ('WAITING', 'NOTIFIED')";
            try (PreparedStatement ps = conn.prepareStatement(waitQuery)) {
                ps.setLong(1, code);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return handleWaitingListArrival(conn, rs, code);
                }
            }
            return "INVALID_CODE";
        } catch (SQLException e) {
            e.printStackTrace();
            return "DATABASE_ERROR";
        }
    }

    /**
     * Manages logic for pre-booked reservations based on arrival time and status.
     */
    private static String handleReservationFlow(Connection conn, ResultSet rs, long code) throws SQLException {
        String status = rs.getString("status");
        int guests = rs.getInt("number_of_guests");
        int userId = rs.getInt("user_id");

        // PRIORITY 1: Guest was already NOTIFIED. Seat them immediately.
        if (status.equals("NOTIFIED")) {
            int tableId = findSuitableTable(guests);
            if (tableId != -1) {
                return proceedToSeating(conn, code, tableId, userId, "reservation");
            }
            return "TABLE_NOT_READY_WAIT"; // Should not happen
        }

        // PRIORITY 2: Guest is already in 'WAITING_AT_RESTAURANT'. They must wait for SMS.
        if (status.equals("WAITING_AT_RESTAURANT")) {
            return "TABLE_NOT_READY_WAIT";
        }

        // PRIORITY 3: First arrival (Status: ACTIVE).
        Timestamp scheduledTs = rs.getTimestamp("reservation_datetime");
        long diffMinutes = Duration.between(scheduledTs.toLocalDateTime(), LocalDateTime.now()).toMinutes();

        // Enforce the 20-minute early arrival window
        if (diffMinutes < -20) {
            return "TOO_EARLY";
        }

        // Check if a table is available and NOT "promised" to a NOTIFIED guest
        if (isSeatingSafe(conn, guests)) {
            int tableId = findSuitableTable(guests);
            if (tableId != -1) {
                return proceedToSeating(conn, code, tableId, userId, "reservation");
            }
        }

        // No table or not safe? Move to waiting status and notify to wait for SMS.
        updateStatus(conn, "reservation", "WAITING_AT_RESTAURANT", code);
        return "TABLE_NOT_READY_WAIT";
    }

    /**
     * Manages walk-in arrivals. Only seats if status has been updated to 'NOTIFIED'.
     */
    private static String handleWaitingListArrival(Connection conn, ResultSet rs, long code) throws SQLException {
        String status = rs.getString("status");
        if (status.equals("NOTIFIED")) {
            int tableId = findSuitableTable(rs.getInt("number_of_guests"));
            if (tableId != -1) {
                return proceedToSeating(conn, code, tableId, rs.getInt("user_id"), "waiting_list_entry");
            }
        }
        // Customers in 'WAITING' status must wait for the notification trigger.
        return "TABLE_NOT_READY_WAIT";
    }

    /**
     * Protection Logic: Counts free tables vs guests already NOTIFIED to prevent 'stealing' tables.
     */
    private static boolean isSeatingSafe(Connection conn, int guests) throws SQLException {
        // Count total available tables
        ResultSet rsAvail = conn.prepareStatement("SELECT COUNT(*) FROM `table` WHERE is_available = 1 AND capacity >= " + guests).executeQuery();
        int available = rsAvail.next() ? rsAvail.getInt(1) : 0;

        // Count all guests (Reservations/Walk-ins) currently in 'NOTIFIED' status
        ResultSet rsNotified = conn.prepareStatement(
            "SELECT (SELECT COUNT(*) FROM reservation WHERE status = 'NOTIFIED' AND number_of_guests >= " + guests + ") + " +
            "(SELECT COUNT(*) FROM waiting_list_entry WHERE status = 'NOTIFIED' AND number_of_guests >= " + guests + ")"
        ).executeQuery();
        int notified = rsNotified.next() ? rsNotified.getInt(1) : 0;

        // Only safe if there is a surplus table available
        return (available > notified);
    }

    /**
     * Atomic Seating Transaction: Creates bill, updates table/status, and links visit.
     */
    private static String proceedToSeating(Connection conn, long code, int tableId, int userId, String sourceTable) throws SQLException {
        try {
            conn.setAutoCommit(false); // Start transaction

            // 1. Create a new Bill
            int billId = createNewBill(conn);

            // 2. Update status to 'ARRIVED'
            updateStatus(conn, sourceTable, "ARRIVED", code);

            // 3. Occupy the table
            updateTableAvailability(conn, tableId, "false");

            // 4. Create Visit record
            insertVisitRecord(conn, code, tableId, userId, billId);

            conn.commit(); 
            return "SUCCESS_TABLE_" + tableId;

        } catch (SQLException e) {
            conn.rollback(); 
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Checks if the freed table fits any priority reservations (Early arrivals).
     * If no priority match is found, it calls the WaitingListController.
     * @param tableId The ID of the vacated table.
     */
    public static void handleTableFreed(int tableId) {
        Connection conn = DBController.getInstance().getConnection();
        try {
            // 1. Get the capacity of the freed table
            int capacity = 0;
            String capQuery = "SELECT capacity FROM `table` WHERE table_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(capQuery)) {
                ps.setInt(1, tableId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) capacity = rs.getInt("capacity");
            }

            if (capacity <= 0) return;

            // 2. Search for Priority Guests (WAITING_AT_RESTAURANT) ordered by original reservation time
            String resQuery = "SELECT confirmation_code FROM reservation " +
                              "WHERE status = 'WAITING_AT_RESTAURANT' AND number_of_guests <= ? " +
                              "ORDER BY reservation_datetime ASC LIMIT 1";
            
            try (PreparedStatement psRes = conn.prepareStatement(resQuery)) {
                psRes.setInt(1, capacity);
                ResultSet rsRes = psRes.executeQuery();
                if (rsRes.next()) {
                    // Priority match found: Update to NOTIFIED
                    updateStatus(conn, "reservation", "NOTIFIED", rsRes.getLong("confirmation_code"));
                    System.out.println("[VisitController] Priority reservation notified.");
                    return; 
                }
            }

            // 3. No priority match: Delegate to WaitingListController for general walk-ins
            WaitingListController.handleTableFreed(tableId);

        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    /**
     * Checks the current status of a reservation or waiting list entry.
     * This is used by the client app to know when to show the "Table Ready" pop-up.
     * @param code The confirmation code to check.
     * @return The current status string (e.g., "WAITING", "NOTIFIED", "ARRIVED").
     */
    public static String checkCurrentStatus(long code) {
        Connection conn = DBController.getInstance().getConnection();
        // Query both tables to find the current status of the code
        String query = "SELECT status FROM reservation WHERE confirmation_code = ? " +
                       "UNION " +
                       "SELECT status FROM waiting_list_entry WHERE confirmation_code = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setLong(1, code);
            ps.setLong(2, code);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("status"); // Return status like 'NOTIFIED'
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "NOT_FOUND";
    }
    // --- Database Helper Methods ---

    private static int findSuitableTable(int guests) throws SQLException {
        String query = "SELECT table_id FROM `table` WHERE is_available = 1 AND capacity >= ? ORDER BY capacity ASC LIMIT 1";
        try (PreparedStatement ps = DBController.getInstance().getConnection().prepareStatement(query)) {
            ps.setInt(1, guests);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("table_id") : -1;
        }
    }

    private static void updateStatus(Connection conn, String table, String status, long code) throws SQLException {
        String sql = "UPDATE " + table + " SET status = ? WHERE confirmation_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, code);
            ps.executeUpdate();
        }
    }

    private static void updateTableAvailability(Connection conn, int id, String isAvailable) throws SQLException {
        String sql = "UPDATE `table` SET is_available = ? WHERE table_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	ps.setBoolean(1, false);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private static int createNewBill(Connection conn) throws SQLException {
        String sql = "INSERT INTO bill (base_amount, discount_percent, final_amount, is_paid) VALUES (0, 0, 0, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Failed to create bill record.");
    }
    

    private static void insertVisitRecord(Connection conn, long code, int tableId, int userId, int billId) throws SQLException {
        String sql = "INSERT INTO visit (confirmation_code, table_id, user_id, bill_id, start_time, status) " +
                     "VALUES (?, ?, ?, ?, NOW(), 'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, code);
            ps.setInt(2, tableId);
            ps.setInt(3, userId);
            ps.setInt(4, billId);
            ps.executeUpdate();
        }
    }
}