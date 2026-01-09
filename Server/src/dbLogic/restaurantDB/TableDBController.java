package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import MainControllers.DBController;

public class TableDBController {

	public static List<Integer> getCandidateTables(int numberOfGuests) {

	    List<Integer> tableIds = new ArrayList<>();

	    String sql =
	        "SELECT table_id FROM `table` WHERE capacity >= ? AND is_available = 1";

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
     * Retrieves a complete list of all tables currently stored in the database.
     * The results are ordered by their unique table ID in ascending order.
     * * @return A List of Table objects containing ID, capacity, and availability status.
     */
    public static List<common.Table> getAllTables() {
        List<common.Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM `table` ORDER BY table_id ASC";
        Connection conn = DBController.getInstance().getConnection();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
            	// Map the current row in the ResultSet to a new Table domain object
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
     * Calculates the next table ID while ensuring it starts from at least 1.
     * Even if the "archive" table (-1) exists, this logic guarantees that 
     * new real tables will receive a positive ID (1, 2, 3...).
     * * @return The next available positive ID.
     */
    private static int getNextTableId() {
        String sql = "SELECT MAX(table_id) FROM `table`";
        Connection conn = DBController.getInstance().getConnection();
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                int maxId = rs.getInt(1);
                
                // If the only table is -1, maxId + 1 would be 0.
                // We use Math.max to ensure our first real table starts at ID 1.
                return Math.max(1, maxId + 1);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while generating next table ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Default starting point if the table is completely empty
        return 1; 
    }


    /**
     * Adds a new table to the system using manual ID management.
     * After a successful insertion, it triggers the waiting list logic to 
     * automatically check if waiting diners can fit the new table.
     * * @param capacity The number of seats for the new table.
     * @return true if the table was added successfully, false otherwise.
     */
    public static boolean addNewTable(int capacity) {
    	// Calculate the next available ID (used because DB is not set to Auto-Increment)
        int nextId = getNextTableId(); 
        
        // Insert table as available (is_available = 1)
        String sql = "INSERT INTO `table` (table_id, capacity, is_available) VALUES (?, ?, 1)";
        Connection conn = DBController.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nextId);
            pstmt.setInt(2, capacity);
            
            int rowsAffected = pstmt.executeUpdate();
            boolean isSuccess = rowsAffected > 0;

            if (isSuccess) {
            	// If the new table is added, immediately try to seat guests from the waiting list
                VisitController.handleTableFreed(nextId);
            }
            
            return isSuccess;
            
        } catch (SQLException e) {
            System.err.println("SQL Error (Add Table): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a table from the database using a transaction.
     * To prevent Foreign Key violations while preserving history, it reassigns 
     * existing visits to a dummy ID (-1) before deleting the table record.
     * * @param tableId The ID of the table to be deleted.
     * @return true if the deletion was successful, false otherwise.
     */
    public static boolean deleteTable(int tableId) {
        Connection conn = DBController.getInstance().getConnection();
        
        try {
            conn.setAutoCommit(false); // התחלת עסקה (Transaction)

            // Step A: Reassign historical visits to table ID -1 (the "archive" table)
            // This satisfies DB constraints without deleting visit records
            String updateVisitsSql = "UPDATE `visit` SET table_id = -1 WHERE table_id = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(updateVisitsSql)) {
                ps1.setInt(1, tableId);
                ps1.executeUpdate();
            }

           // Step B: Safely delete the actual table record from the 'table' table
            String deleteTableSql = "DELETE FROM `table` WHERE table_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(deleteTableSql)) {
                ps2.setInt(1, tableId);
                int affectedRows = ps2.executeUpdate();

                conn.commit(); 
                return affectedRows > 0;
            }

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("SQL Error (Soft Transfer & Delete): " + e.getMessage());
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    

    /**
     * Updates the seating capacity of an existing table.
     * * @param tableId The ID of the table to update.
     * @param newCapacity The new number of seats.
     * @return true if updated successfully, false otherwise.
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
