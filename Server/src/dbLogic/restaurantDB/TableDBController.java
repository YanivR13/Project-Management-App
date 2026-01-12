package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import MainControllers.DBController;

/**
 * Database access controller for restaurant table management.
 * 
 * This class centralizes all database operations related to tables,
 * including availability queries, capacity calculations, and table state updates.
 */
public class TableDBController {

    /**
     * Retrieves a list of available table IDs that can accommodate
     * the given number of guests.
     *
     * The result is ordered by table capacity in ascending order,
     * enabling a best-fit seating strategy (smallest suitable table first).
     *
     * @param numberOfGuests Required seating capacity
     * @return List of candidate table IDs sorted by capacity
     */
    public static List<Integer> getCandidateTables(int numberOfGuests) {

        List<Integer> tableIds = new ArrayList<>();

        String sql =
            "SELECT table_id " +
            "FROM `table` " +
            "WHERE capacity >= ? AND is_available = 1 " +
            "ORDER BY capacity ASC";

        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, numberOfGuests);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tableIds.add(rs.getInt("table_id"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tableIds;
    }

    /**
     * Calculates the total seating capacity of the restaurant
     * by summing the capacity of all tables.
     *
     * @return Total restaurant seating capacity
     */
    public static int getRestaurantMaxCapacity() {

        String sql = "SELECT COALESCE(SUM(capacity), 0) FROM `table`";

        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Calculates the total seating capacity currently occupied
     * by unavailable tables (is_available = 0).
     *
     * This represents capacity already taken by active visits.
     *
     * @return Total occupied seating capacity
     * @throws SQLException if a database error occurs
     */
    public static int getUnavailableCapacity() throws SQLException {

        String sql =
            "SELECT COALESCE(SUM(capacity), 0) " +
            "FROM `table` " +
            "WHERE is_available = 0";

        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    /**
     * Marks a specific table as unavailable.
     * Used when a table is assigned to an active visit.
     *
     * @param tableId ID of the table to mark as unavailable
     * @throws Exception if a database error occurs
     */
    public static void setTableUnavailable(int tableId) throws Exception {

        String sql =
            "UPDATE `table` " +
            "SET is_available = 0 " +
            "WHERE table_id = ?";

        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tableId);
            ps.executeUpdate();
        }
    }

    /**
     * Retrieves the seating capacity of a specific table.
     *
     * @param tableId Table identifier
     * @return Seating capacity of the table
     * @throws SQLException if the table does not exist
     */
    public static int getTableCapacity(int tableId) throws SQLException {

        String sql = "SELECT capacity FROM `table` WHERE table_id = ?";

        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tableId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("capacity");
                }
            }
        }

        throw new SQLException("Table not found: table_id=" + tableId);
    }

    /**
     * Retrieves all tables currently stored in the system.
     * Each table includes its ID, seating capacity,
     * and availability status.
     *
     * @return List of all tables ordered by table ID
     */
    public static List<common.Table> getAllTables() {

        List<common.Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM `table` ORDER BY table_id ASC";

        Connection conn = DBController.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                common.Table t = new common.Table(
                    rs.getInt("table_id"),
                    rs.getInt("capacity"),
                    rs.getBoolean("is_available")
                );
                tables.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tables;
    }

    /**
     * Generates the next available positive table ID.
     * Ensures table IDs always start from 1,
     * even if a reserved archive table (-1) exists.
     *
     * @return Next available table ID
     */
    private static int getNextTableId() {

        String sql = "SELECT MAX(table_id) FROM `table`";
        Connection conn = DBController.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int maxId = rs.getInt(1);
                return Math.max(1, maxId + 1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 1;
    }

    /**
     * Adds a new table to the system with the given seating capacity.
     * After a successful insertion, the waiting list logic is triggered
     * to attempt seating waiting guests.
     *
     * @param capacity Number of seats for the new table
     * @return true if the table was added successfully, false otherwise
     */
    public static boolean addNewTable(int capacity) {
    	
        final int DEFAULT_RESTAURANT_ID = 1; 
        
        int nextId = getNextTableId();
        String sqlTable = "INSERT INTO `table` (table_id, capacity, is_available) VALUES (?, ?, 1)";
        String sqlRestaurantTable = "INSERT INTO `restaurant_table` (restaurant_id, table_id) VALUES (?, ?)";

        Connection conn = DBController.getInstance().getConnection();
        
        try {
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmt1 = conn.prepareStatement(sqlTable)) {
                pstmt1.setInt(1, nextId);
                pstmt1.setInt(2, capacity);
                pstmt1.executeUpdate();
            }

            try (PreparedStatement pstmt2 = conn.prepareStatement(sqlRestaurantTable)) {
                pstmt2.setInt(1, DEFAULT_RESTAURANT_ID); 
                pstmt2.setInt(2, nextId);
                pstmt2.executeUpdate();
            }

            conn.commit(); 

            VisitController.handleTableFreed(nextId);
            
            System.out.println("Table " + nextId + " added successfully to both tables.");
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

    /**
     * Deletes a table from the system using a transactional process.
     * Existing visit records are reassigned to a dummy archive table (-1)
     * to preserve historical data and avoid foreign key violations.
     *
     * @param tableId ID of the table to delete
     * @return true if deletion succeeded, false otherwise
     */
    public static boolean deleteTable(int tableId) {

        Connection conn = DBController.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            String updateVisitsSql =
                "UPDATE `visit` SET table_id = -1 WHERE table_id = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(updateVisitsSql)) {
                ps1.setInt(1, tableId);
                ps1.executeUpdate();
            }
            
            String deleteFromRestaurantTableSql = "DELETE FROM `restaurant_table` WHERE table_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(deleteFromRestaurantTableSql)) {
                ps2.setInt(1, tableId);
                ps2.executeUpdate();
            }

            String deleteTableSql =
                "DELETE FROM `table` WHERE table_id = ?";
            try (PreparedStatement ps3 = conn.prepareStatement(deleteTableSql)) {
                ps3.setInt(1, tableId);
                int affected = ps3.executeUpdate();
                conn.commit();
                return affected > 0;
            }

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;

        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * Updates the seating capacity of an existing table.
     *
     * @param tableId ID of the table to update
     * @param newCapacity New seating capacity
     * @return true if updated successfully, false otherwise
     */
    public static boolean updateTableCapacity(int tableId, int newCapacity) {

        String sql = "UPDATE `table` SET capacity = ? WHERE table_id = ?";

        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newCapacity);
            pstmt.setInt(2, tableId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
