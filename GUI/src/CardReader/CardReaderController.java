package CardReader; 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardReaderController {
    
    // נתוני חיבור ל-Database (וודא שהסיסמה תואמת למה שהגדרת ב-Workbench)
    private final String url = "jdbc:mysql://localhost:3306/prototypedb";
    private final String username = "root";
    private final String password = "Ya212104483"; 

    private String currentSubscriberID;

    /**
     * פונקציה 1: אימות מנוי
     * בודקת אם ה-ID קיים בטבלת subscriber לפי עמודת subscriber_id
     */
    public boolean validateSubscriber(String id) {
        // השאילתה משתמשת בשם העמודה המדויק שראינו ב-Workbench
        String query = "SELECT * FROM subscriber WHERE subscriber_id = ?"; 
        
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                this.currentSubscriberID = id;
                return true; // נמצאה התאמה ב-DB
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // לא נמצא מנוי או שיש שגיאת חיבור
    }

    /**
     * פונקציה 2: איבדתי קוד אישור 
     * שולפת את כל הקודים עבור המנוי המחובר
     */
    public List<String> getLostConfirmationCodes() {
        List<String> activeCodes = new ArrayList<>();

        String query = "SELECT confirmation_code, order_number FROM orders WHERE subscriber_id = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentSubscriberID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String code = rs.getString("confirmation_code"); 
                String orderNum = rs.getString("order_number");   
                activeCodes.add("Order #" + orderNum + " | Confirmation Code: " + code);
            }
        } catch (SQLException e) {
            System.err.println("Database Error (Lost Code): " + e.getMessage());
        }
        
        return activeCodes;
    }
    
    /**
     * פונקציה 3: אימות קוד אישור והגעה
     * בודקת שהקוד פעיל (ACTIVE) ומעדכנת אותו ל-COMPLETED
     */
    public String verifyConfirmationCode(String code) {

    	String checkQuery = "SELECT * FROM orders WHERE confirmation_code = ? AND subscriber_id = ? AND status = 'ACTIVE'";
        String updateQuery = "UPDATE orders SET status = 'COMPLETED' WHERE confirmation_code = ? AND subscriber_id = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            // שלב א: בדיקת קיום הקוד בסטטוס ACTIVE
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setString(1, code);
                pstmt.setString(2, currentSubscriberID);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    // שלב ב: עדכון הסטטוס ב-DB (הקוד נוצל)
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateQuery)) {
                        updatePstmt.setString(1, code);
                        updatePstmt.setString(2, currentSubscriberID);
                        updatePstmt.executeUpdate();
                    }
                    return "Success: Welcome to the restaurant!";
                } else {
                    return "Error: Invalid or expired code.";
                }
            }
        } catch (SQLException e) {
            return "Database Error: " + e.getMessage();
        }
    }
}