package dbLogic.cardReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import MainControllers.DBController;


public class CardReaderDBController {

    /**
     * פונקציה 1: אימות מנוי ובדיקת סטטוס הזמנה פעילה.
     * מחברת בין subscriber ל-reservation דרך user_id.
     */
    public boolean validateSubscriber(String id) {
        String query = "SELECT r.status FROM reservation r " +
                       "JOIN subscriber s ON r.user_id = s.user_id " +
                       "WHERE s.subscriber_id = ? AND r.status = 'ACTIVE'";
        
        Connection conn = DBController.getInstance().getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean found = rs.next();
                System.out.println("Login check for Subscriber ID " + id + ": " + (found ? "SUCCESS" : "FAILED"));
                return found;
            }
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * פונקציה 2: שליפת קודים אבודים מטבלת ה-reservation.
     */
    public List<String> getLostConfirmationCodes(String id) {
        List<String> activeCodes = new ArrayList<>();
        // עדכון השאילתה: חיפוש כפול (גם לפי מזהה מנוי וגם לפי מזהה משתמש)
        String query = "SELECT r.confirmation_code, r.reservation_datetime FROM reservation r " +
                       "LEFT JOIN subscriber s ON r.user_id = s.user_id " +
                       "WHERE (s.subscriber_id = ? OR r.user_id = ?) AND r.status = 'ACTIVE'";

        Connection conn = DBController.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id); // יחפש 1028
            pstmt.setString(2, id); // יחפש 28
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    activeCodes.add("Code: " + rs.getString("confirmation_code"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activeCodes;
    }
    
    
    /**
     * פונקציה 3: אימות קוד הגעה ועדכון סטטוס ל-COMPLETED בטבלת reservation.
     */
    //abc
    public String verifyConfirmationCode(String code, String subscriberID) {
        // שאילתה לבדיקת קיום הקוד עבור המנוי הספציפי
        String checkQuery = "SELECT r.confirmation_code FROM reservation r " +
                            "JOIN subscriber s ON r.user_id = s.user_id " +
                            "WHERE r.confirmation_code = ? AND s.subscriber_id = ? AND r.status = 'ACTIVE'";
        
        // שאילתה לעדכון הסטטוס
        String updateQuery = "UPDATE reservation r " +
                             "JOIN subscriber s ON r.user_id = s.user_id " +
                             "SET r.status = 'COMPLETED' " +
                             "WHERE r.confirmation_code = ? AND s.subscriber_id = ?";

        Connection conn = DBController.getInstance().getConnection();

        try {
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setString(1, code);
                pstmt.setString(2, subscriberID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement updatePstmt = conn.prepareStatement(updateQuery)) {
                            updatePstmt.setString(1, code);
                            updatePstmt.setString(2, subscriberID);
                            updatePstmt.executeUpdate();
                        }
                        return "Success: Welcome to the restaurant!";
                    } else {
                        return "Error: Invalid or expired code.";
                    }
                }
            }
        } catch (SQLException e) {
            return "Database Error: " + e.getMessage();
        }
    }
}