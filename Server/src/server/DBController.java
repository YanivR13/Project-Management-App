package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Handles all database operations for the server.
 * Responsible for connecting to MySQL and executing CRUD operations on the 'orders' table.
 */
public class DBController {

    /** 
     * A single shared database connection used by the server.
     */
    public static Connection conn;

    /**
     * Establishes a connection to the MySQL database.
     * This method must be called before any DB operation.
     *
     * @throws SQLException if the connection fails
     */
    public static void connectToDB() throws SQLException {
        conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/prototypedb?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false",
            "root",
            "Eden2701@"
        );
        System.out.println("SQL connection succeed");
    }

    /**
     * Inserts a new order into the database.
     * Expects an ArrayList<String> containing all fields in the correct order.
     *
     * @param msg data received from the client, representing the order fields
     */
    public static void insertOrderToDB(Object msg) {
        ArrayList<String> list = (ArrayList<String>) msg;

        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO orders VALUES (?,?,?,?,?,?)");

            ps.setInt(1, Integer.parseInt(list.get(0)));                 // order_number (PK)
            ps.setDate(2, java.sql.Date.valueOf(list.get(1)));           // order_date
            ps.setInt(3, Integer.parseInt(list.get(2)));                 // number_of_guests
            ps.setInt(4, Integer.parseInt(list.get(3)));                 // confirmation_code
            ps.setInt(5, Integer.parseInt(list.get(4)));                 // subscriber_id (FK)
            ps.setDate(6, java.sql.Date.valueOf(list.get(5)));           // date_of_placing_order

            ps.executeUpdate();
            System.out.println("Order inserted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all orders from the database and formats them into readable strings.
     *
     * @return a list of formatted order strings to send back to the client
     */
    public static ArrayList<String> getAllOrders() {
        ArrayList<String> list = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM orders");

            while (rs.next()) {
                String formatted = String.format(
                    "Order: %s | Date: %s | Guests: %s | Code: %s | SubID: %s | Placed: %s",
                    rs.getString("order_number"),
                    rs.getString("order_Date"),
                    rs.getString("number_of_guests"),
                    rs.getString("confirmation_code"),
                    rs.getString("subscriber_id"),
                    rs.getString("date_of_placing_order")
                );

                list.add(formatted);
            }

            rs.close();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Updates the date and number of guests for an order by its ID.
     * If the ID does not exist, no rows will be updated and the method returns false.
     *
     * @param id the order_number to update
     * @param date the new date value
     * @param guests the new number of guests
     * @return true if the update succeeded, false if the ID does not exist or an error occurred
     */
    public static boolean updateOrder(String id, String date, String guests) {
        try {
            Statement stmt = conn.createStatement();

            String query = String.format(
                "UPDATE orders SET order_Date='%s', number_of_guests='%s' WHERE order_number='%s'",
                date, guests, id
            );

            int rows = stmt.executeUpdate(query);  // how many rows were updated?

            return rows > 0;  // rows == 0 → ID does not exist

        } catch (SQLException e) {
            System.out.println("SQL Error during update: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
