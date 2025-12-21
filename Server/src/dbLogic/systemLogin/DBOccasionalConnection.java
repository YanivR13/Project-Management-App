package dbLogic.systemLogin;

import MainControllers.DBController;
import java.sql.*;

/**
 * Database logic for Occasional Customers based on linked tables:
 * 'user' (contact info) and 'occasional_customer' (username).
 */
public class DBOccasionalConnection {

    /**
     * Verifies occasional customer login by joining 'occasional_customer' and 'user'.
     * @param username The entered username.
     * @param contact The entered phone or email.
     * @return true if credentials match across both tables.
     */
    public boolean verifyOccasional(String username, String contact) {
        Connection conn = DBController.getInstance().getConnection();
        
        // שאילתה שמחברת את שתי הטבלאות ובודקת אם ה-contact תואם לטלפון או למייל
        String sql = "SELECT oc.* FROM occasional_customer oc " +
                     "JOIN user u ON oc.user_id = u.user_id " +
                     "WHERE oc.username = ? AND (u.phone_number = ? OR u.email = ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, contact);
            pstmt.setString(3, contact);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Resets the username by identifying the user via the 'user' table first.
     */
    public String resetUsername(String contact, String newUsername) {
        Connection conn = DBController.getInstance().getConnection();
        
        // 1. מציאת ה-user_id לפי טלפון או מייל בטבלת user
        String findUserIdSql = "SELECT user_id FROM user WHERE phone_number = ? OR email = ?";
        // 2. בדיקה ששם המשתמש החדש לא תפוס ב-occasional_customer
        String checkUserSql = "SELECT * FROM occasional_customer WHERE username = ?";
        // 3. עדכון שם המשתמש בטבלה הנכונה
        String updateSql = "UPDATE occasional_customer SET username = ? WHERE user_id = ?";

        try {
            int userId = -1;

            // שלב א: מציאת ה-ID
            try (PreparedStatement pstmt = conn.prepareStatement(findUserIdSql)) {
                pstmt.setString(1, contact);
                pstmt.setString(2, contact);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                } else {
                    return "ERROR: Contact info not found.";
                }
            }

            // שלב ב: בדיקת זמינות שם המשתמש
            try (PreparedStatement pstmt = conn.prepareStatement(checkUserSql)) {
                pstmt.setString(1, newUsername);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return "ERROR: Username already taken.";
            }

            // שלב ג: ביצוע העדכון בטבלת ה-occasional_customer
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, newUsername);
                pstmt.setInt(2, userId);
                int rows = pstmt.executeUpdate();
                return (rows > 0) ? "RESET_USERNAME_SUCCESS" : "ERROR: Update failed.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR: DB Error: " + e.getMessage();
        }
    }
    
    public String registerNewOccasional(String username, String contact) {
        Connection conn = DBController.getInstance().getConnection();
        
        // הדפסת דיבאג לטרמינל של השרת - כדי שתראה מה הגיע מהלקוח
        System.out.println("Registering: User=" + username + ", Contact=" + contact);

        // --- 1. ולידציה של שם משתמש ---
        if (username == null || username.length() > 10) {
            return "ERROR: Username too long. Maximum 10 characters allowed.";
        }

        if (contact == null || contact.isEmpty()) {
            return "ERROR: Contact field cannot be empty.";
        }

        boolean isEmail = false;
        boolean isPhone = false;

        // --- 2. Switch Case לזיהוי סוג פרטי הקשר ---
        switch (contact.charAt(0)) {
            case '0': 
                // המשתמש התחיל ב-0? אנחנו בודקים פורמט טלפון
                if (contact.matches("^0\\d{9}$")) {
                    isPhone = true;
                } else {
                    return "ERROR: Invalid Phone. Must be exactly 10 digits and contain only numbers.";
                }
                break;

            default: 
                // כל תו אחר? אנחנו בודקים פורמט אימייל
                if (contact.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$")) {
                    isEmail = true;
                } else {
                    return "ERROR: Invalid Email format. Must contain '@' and end with '.com'.";
                }
                break;
        }

        // --- 3. לוגיקת בסיס הנתונים (SQL) ---
        String checkUserSql = "SELECT * FROM occasional_customer WHERE username = ?";
        String checkContactSql = "SELECT * FROM user WHERE phone_number = ? OR email = ?";
        String insertUserSql = "INSERT INTO user (phone_number, email) VALUES (?, ?)";
        String insertOccSql = "INSERT INTO occasional_customer (user_id, username) VALUES (?, ?)";

        try {
            conn.setAutoCommit(false); // התחלת טרנזקציה

            // א. בדיקה אם שם המשתמש קיים
            try (PreparedStatement pstmt = conn.prepareStatement(checkUserSql)) {
                pstmt.setString(1, username);
                if (pstmt.executeQuery().next()) {
                    conn.rollback();
                    return "ERROR: Username already exists.";
                }
            }

            // ב. בדיקה אם המייל/טלפון כבר רשומים
            try (PreparedStatement pstmt = conn.prepareStatement(checkContactSql)) {
                pstmt.setString(1, contact);
                pstmt.setString(2, contact);
                if (pstmt.executeQuery().next()) {
                    conn.rollback();
                    return "ERROR: Contact info already registered.";
                }
            }

            // ג. הכנסה לטבלת user
            int userId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                if (isEmail) {
                    pstmt.setNull(1, java.sql.Types.VARCHAR); 
                    pstmt.setString(2, contact);
                } else {
                    pstmt.setString(1, contact);
                    pstmt.setNull(2, java.sql.Types.VARCHAR);
                }
                
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) userId = rs.getInt(1);
            }

            // ד. קישור לטבלת occasional_customer
            if (userId != -1) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertOccSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, username);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            return "REGISTRATION_SUCCESS";

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return "ERROR: Database failure: " + e.getMessage();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}