package dbLogic.restaurantDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import MainControllers.DBController;

public class CancelWaitingListController {

    /**
     * Cancels a waiting list entry for a specific user.
     * Only works if the current status is 'WAITING'.
     * @return 1 if successful, 0 if no active WAITING entry found, -1 on error.
     */
    public static int cancelWaitingEntry(int userId) {
        String query = "UPDATE waiting_list_entry SET status = 'CANCELLED' " +
                       "WHERE user_id = ? AND status = 'WAITING'";
        
        Connection conn = DBController.getInstance().getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            
            // If rowsAffected > 0, it means an entry was found and updated
            return rowsAffected > 0 ? 1 : 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}