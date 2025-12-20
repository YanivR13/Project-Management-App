package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entities.OccasionalCustomer;

/**
 * Handles all database operations for the server.
 * Responsible for connecting to MySQL and executing CRUD operations on the 'orders' table.
 */
public class DBController {

    /** 
     * The single instance of DBController (Singleton).
     */
    private static DBController instance;
    
    /** 
     * A single shared database connection used by the server.
     */
    private Connection conn;
    
    /**
     * Returns the single instance of DBController.
     */
    public static DBController getInstance() {
        if (instance == null) {
            instance = new DBController();
        }
        return instance;
    }
    

    /**
     * Establishes a connection to the MySQL database.
     * This method must be called before any DB operation.
     *
     * @throws SQLException if the connection fails
     */
    public void connectToDB() throws SQLException {
        if (conn != null) {
            return; // already connected
        }

        conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/bistrodb?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false",
            "root",
            "Eden2701@"
        );

        System.out.println("SQL connection succeed");
    }
    
    public void closeConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    public boolean save(Object entity) {
        // Logic to identify the object type and add it to the corresponding list
        return false;
    }
    
    
    /**
     * Retrieves all occasional customers from the database.
     *
     * Joins the `user` and `occasional_customer` tables and creates
     * OccasionalCustomer objects using the user's email and phone number.
     *
     * @return a list of all occasional customers
     */
    public List<OccasionalCustomer> getAllOccasionalCustomers() {
        List<OccasionalCustomer> customers = new ArrayList<>();

        String query = """
            SELECT u.email, u.phone_number
            FROM user u
            JOIN occasional_customer oc ON u.user_id = oc.user_id
        """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String email = rs.getString("email");
                String phone = rs.getString("phone_number");

                OccasionalCustomer customer =
                        new OccasionalCustomer(email, phone);

                customers.add(customer);
            }

        } catch (SQLException e) {
            System.out.println("SQL Error in getAllOccasionalCustomers: " + e.getMessage());
            e.printStackTrace();
        }

        return customers;
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
    public ArrayList<String> getAllOrders() {
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
    public boolean updateOrder(String id, String date, String guests) {
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
