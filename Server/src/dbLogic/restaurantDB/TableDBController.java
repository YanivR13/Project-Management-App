package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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


}
