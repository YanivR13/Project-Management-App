package Controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
            "jdbc:mysql://localhost:3307/prototypedb?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false",
            "root",
            "Rochlin99!"
        );
        System.out.println("SQL connection succeed");
    }
    
    public boolean save(Object entity) {
        // Logic to identify the object type and add it to the corresponding list
        return false;
    }
    
    /**
    * Purpose: Retrieves all stored entities of a specific type.
    * Receives: Class<T> entityType (e.g., Table.class).
    * Returns: List<T> (a list containing all instances of that type).
    */
   public <T> List<T> getAll(Class<T> entityType) {
       // Logic to filter the central storage and return all objects of the requested class
       return null;
   }
   
   /**
    * Purpose: Finds a specific entity using its unique identifier.
    * Receives: Class<T> entityType, Object id.
    * Returns: T (the found object, or null if it doesn't exist).
    */
   public <T> T findById(Class<T> entityType, Object id) {
       // Logic to iterate through the relevant list and match the ID field
       return null;
   }
   
   /**
    * Purpose: Deletes a specific entity from the system.
    * Receives: Object entity.
    * Returns: boolean (true if the entity was found and removed).
    */
   public boolean delete(Object entity) {
       // Search for the object in the storage and remove it
       return false;
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
