package MainControllers;

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
        	    "jdbc:mysql://localhost:3307/prototypedb?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false",
        	    "root",
        	    "Rochlin99!"
        	);

        System.out.println("SQL connection succeed");
    }
    
    
    /**
     * Provides access to the established database connection for other logic classes.
     * * @return the active java.sql.Connection object
     */
    
    public Connection getConnection() {
        return conn;
    }
    
    /**
     * Safely closes the database connection.
     * This should be called when the server is shutting down.
     * * @throws SQLException if a database access error occurs
     */
    public void closeConnection() throws SQLException {
        // Check if connection exists and is not already closed
        if (conn != null && !conn.isClosed()) {
            conn.close();
            System.out.println("SQL connection closed successfully.");
        }
    }
}
