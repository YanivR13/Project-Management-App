package dbLogic.systemLogin;

import MainControllers.DBController;
import dbLogic.ILoginDatabase;

import java.sql.*;

public class DBSubscriberConnection implements ILoginDatabase {

    @Override
    public boolean verifySubscriber(long subID) {
        String sql = "SELECT * FROM subscriber WHERE subscriber_id = ? AND status = 'Active'";
        Connection conn = DBController.getInstance().getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, subID);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // These are not applicable for subscribers, we can return false or throw an exception
    @Override public boolean verifyOccasional(String u, String c) { return false; }
    @Override public boolean registerOccasional(String u, String p, String e) { return false; }
}