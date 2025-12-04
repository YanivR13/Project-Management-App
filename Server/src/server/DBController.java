package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBController {
    
    public static Connection conn;

    /**
     * Connects the server to the MySQL database.
     */
    public static void connectToDB() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/prototypedb?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false", "root", "Rochlin99!");
            System.out.println("SQL connection succeed");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    /**
     * Inserts a new order into the 'orders' table.
     */
    public static void insertOrderToDB(Object msg) {
        ArrayList<String> list = (ArrayList<String>) msg;
        try {
            PreparedStatement ps = conn.prepareStatement("insert into orders values(?,?,?,?,?,?)");
            ps.setInt(1, Integer.parseInt(list.get(0))); // order_number (PK)
            ps.setDate(2, java.sql.Date.valueOf(list.get(1))); // order_date
            ps.setInt(3, Integer.parseInt(list.get(2))); // number_of_guests
            ps.setInt(4, Integer.parseInt(list.get(3))); // confirmation_code
            ps.setInt(5, Integer.parseInt(list.get(4))); // subscriber_id (FK)
            ps.setDate(6, java.sql.Date.valueOf(list.get(5))); // date_of_placing_order

            ps.executeUpdate();
            System.out.println("Order inserted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all orders from the DB and formats them as strings.
     */
    public static ArrayList<String> getAllOrders() {
        ArrayList<String> list = new ArrayList<>();
        Statement stmt;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM orders;");

            while (rs.next()) {
                String orderNum = rs.getString("order_number");
                String orderDate = rs.getString("order_Date");
                String numGuests = rs.getString("number_of_guests");
                String confCode = rs.getString("confirmation_code");
                String subId = rs.getString("subscriber_id");
                String placedDate = rs.getString("date_of_placing_order");

                String str = String.format("Order: %s | Date: %s | Guests: %s | Code: %s | SubID: %s | Placed: %s",
                        orderNum, orderDate, numGuests, confCode, subId, placedDate);

                list.add(str);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Updates an existing order's date and number of guests based on ID.
     * Returns true if successful, false otherwise.
     */
    public static boolean updateOrder(String id, String date, String guests) {
        Statement stmt;
        try {
            stmt = conn.createStatement();
            // בניית השאילתה עם שמות העמודות המדויקים שלך
            String query = String.format("UPDATE orders SET order_Date='%s', number_of_guests='%s' WHERE order_number='%s'",
                    date, guests, id);

            stmt.executeUpdate(query);
            return true; // העדכון עבר בהצלחה
            
        } catch (SQLException e) {
            System.out.println("SQL Error during update: " + e.getMessage());
            e.printStackTrace();
            return false; // העדכון נכשל
        }
    }
}