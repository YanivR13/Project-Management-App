package server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

// OCSF imports
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class OrderServer extends AbstractServer {

    // Variable for storing the database connection
    private static Connection conn;

    /**
     * Constructor: Receives the port and passes it to the parent class (AbstractServer)
     */
    public OrderServer(int port) {
        super(port);
    }

    /**
     * This method handles any messages received from the client.
     * It is the "brain" of the server processing.
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);

        // Check if the message is an ArrayList (as required by the assignment)
        if (msg instanceof ArrayList) {
            // Cast the message to an ArrayList of Strings
            ArrayList<String> data = (ArrayList<String>) msg;
            
            // Call the method to update the database with the received data
            updateOrderInDB(data);
            
            try {
                // Send a confirmation message back to the client
                client.sendToClient("Server: Order updated successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Private method to handle the database update logic.
     * It parses the data and executes the SQL UPDATE query.
     */
    private void updateOrderInDB(ArrayList<String> data) {
        try {
            // Ensure the connection is active
            if (conn == null) {
                connectToDB();
            }
            
            // SQL Update Query
            // Note: In a real scenario, we would use the specific Order ID.
            // Here, we update order_number = 1 for testing purposes.
            String query = "UPDATE `Order` SET order_date = ?, number_of_guests = ? WHERE order_number = 1";
            
            // Using PreparedStatement to prevent SQL Injection
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, data.get(0)); // First element: Order Date
            pstmt.setInt(2, Integer.parseInt(data.get(1))); // Second element: Number of Guests
            
            // Execute the update
            pstmt.executeUpdate();
            System.out.println("SQL Update Executed Successfully.");
            
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        } catch (Exception e) {
             System.out.println("General Error: " + e.getMessage());
        }
    }

    /**
     * This method is called automatically when the server starts listening.
     */
    @Override
    protected void serverStarted() {
        System.out.println("Server listening for connections on port " + getPort());
        // Establish database connection as soon as the server starts
        connectToDB();
    }

    /**
     * This method is called automatically when the server stops listening.
     */
    @Override
    protected void serverStopped() {
        System.out.println("Server has stopped listening for connections.");
        try {
            // Close the database connection if it is open
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Establishes a connection to the MySQL database using JDBC.
     */
    public static void connectToDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("Driver definition succeed");
        } catch (Exception ex) {
            System.out.println("Driver definition failed");
        }

        try {
            // הכתובת המעודכנת והבטוחה ביותר:
            // 1. פורט 3307
            // 2. אזור זמן Jerusalem
            // 3. ביטול SSL ואישור מפתחות
        	String dbUrl = "jdbc:mysql://localhost:3307/braude_prototype?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";
            
            // *** וודא שהסיסמה כאן היא הסיסמה הנכונה שלך! ***
            conn = DriverManager.getConnection(dbUrl, "root", "1234"); 
            
            System.out.println("SQL connection succeed");
        } catch (SQLException ex) {
            // הדפסה מפורטת יותר של השגיאה
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQL State: " + ex.getSQLState());
            System.out.println("Vendor Error: " + ex.getErrorCode());
            ex.printStackTrace(); // זה יתן לנו את כל הפרטים אם זה נכשל
        }
    }
    
    /**
     * The main method to run the server.
     */
    public static void main(String[] args) {
        int port = 5555; // The default port used in OCSF examples
        OrderServer sv = new OrderServer(port);
        
        try {
            sv.listen(); // Start listening for connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }
    }
}