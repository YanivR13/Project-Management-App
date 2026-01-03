package dbLogic.restaurantDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.Duration;
import MainControllers.DBController;

/**
 * VisitController handles the arrival logic and seating transactions.
 */
public class VisitController {

    public static String processTerminalArrival(long code) {
        Connection conn = DBController.getInstance().getConnection();
        try {
            // 1. Search in confirmed reservations
            String resQuery = "SELECT * FROM reservation WHERE confirmation_code = ? AND status = 'ACTIVE'";
            try (PreparedStatement ps = conn.prepareStatement(resQuery)) {
                ps.setLong(1, code);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return handleReservationTiming(conn, rs, code);
                }
            }

            // 2. Search in notified waiting list entries
            String waitQuery = "SELECT * FROM waiting_list_entry WHERE confirmation_code = ? AND status = 'NOTIFIED'";
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

    private static String handleReservationTiming(Connection conn, ResultSet rs, long code) throws SQLException {
        Timestamp scheduledTs = rs.getTimestamp("reservation_datetime");
        int guests = rs.getInt("number_of_guests");
        int userId = rs.getInt("user_id");
        long diffMinutes = Duration.between(scheduledTs.toLocalDateTime(), LocalDateTime.now()).toMinutes();

        if (diffMinutes < -60) return "TOO_EARLY";

        int tableId = findSuitableTable(guests);
        if (tableId != -1) {
            // Transaction Start: Seat the customer
            return proceedToSeating(conn, code, tableId, userId, "reservation");
        } else {
            updateStatus(conn, "reservation", "WAITING_AT_RESTAURANT", code);
            return "TABLE_NOT_READY_WAIT";
        }
    }

    private static String handleWaitingListArrival(Connection conn, ResultSet rs, long code) throws SQLException {
        int tableId = findSuitableTable(rs.getInt("number_of_guests"));
        if (tableId != -1) {
            return proceedToSeating(conn, code, tableId, rs.getInt("user_id"), "waiting_list_entry");
        }
        return "TABLE_NOT_READY_WAIT";
    }

    /**
     * The Seating Transaction: Updates all tables atomically.
     */
    private static String proceedToSeating(Connection conn, long code, int tableId, int userId, String sourceTable) throws SQLException {
        try {
            conn.setAutoCommit(false); // Start transaction

            // 1. Create a new Bill and get its ID
            int billId = createNewBill(conn);

            // 2. Update status to 'ARRIVED'
            updateStatus(conn, sourceTable, "ARRIVED", code);

            // 3. Mark the table as occupied ('false' for is_available)
            updateTableAvailability(conn, tableId, "false");

            // 4. Create the Visit record linking everything
            insertVisitRecord(conn, code, tableId, userId, billId);

            conn.commit(); // Save all changes safely
            return "SUCCESS_TABLE_" + tableId;

        } catch (SQLException e) {
            conn.rollback(); // Undo everything if ANY step fails
            e.printStackTrace();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }


    private static int findSuitableTable(int guests) throws SQLException {
        String query = "SELECT table_id FROM `table` WHERE is_available = 'true' AND capacity >= ? ORDER BY capacity ASC LIMIT 1";
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
            ps.setString(1, isAvailable);
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
        String sql = "INSERT INTO visit (confirmation_code, table_id, user_id, bill_id, start_time, status) VALUES (?, ?, ?, ?, NOW(), 'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, code);
            ps.setInt(2, tableId);
            ps.setInt(3, userId);
            ps.setInt(4, billId);
            ps.executeUpdate();
        }
    }
}