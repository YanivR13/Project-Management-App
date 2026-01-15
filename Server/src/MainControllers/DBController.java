package MainControllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The DBController class serves as the central database manager for the server-side application.
 * It encapsulates the JDBC connection logic and provides a unified gateway to the 
 * MySQL database.
 * * <p>Design Pattern: <b>Singleton</b>.
 * This ensures that only one database connection instance exists across the entire 
 * server application, preventing resource exhaustion and maintaining data consistency.</p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public class DBController {

    /** * The single, static instance of DBController.
     */
    private static DBController instance;
    
    /** * The persistent JDBC {@link Connection} object used for all SQL operations.
     */
    private Connection conn;
    
    /**
     * Retrieves the single instance of the DBController.
     * Uses lazy initialization to create the instance only when first requested.
     * * @return the singleton {@link DBController} instance.
     */
    public static DBController getInstance() {
        if (instance == null) {
            instance = new DBController();
        }
        return instance;
    }

    /**
     * Establishes a physical connection to the MySQL database using the JDBC driver.
     * * <p>Connection Parameters:
     * <ul>
     * <li><b>Port:</b> 3307 (Custom MySQL port)</li>
     * <li><b>Schema:</b> prototypedb</li>
     * <li><b>Timezone:</b> Asia/Jerusalem</li>
     * <li><b>SSL:</b> Disabled</li>
     * </ul>
     * </p>
     * * @throws SQLException if the connection attempt fails or the driver is not found.
     */
    public void connectToDB() throws SQLException {
        // Prevent re-connecting if an active connection already exists
        if (conn != null) {
            return; 
        }

        /**
         * The connection string includes:
         * - allowLoadLocalInfile: Permits loading local files into the DB.
         * - serverTimezone: Aligns Java LocalDateTime with the database's clock.
         */
        conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/prototypedb?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true",
            "root",
<<<<<<< HEAD
            "Ss5157110!"
=======
            "Rochlin99!"
>>>>>>> branch 'development' of https://github.com/YanivR13/Project-Management-App.git
        );

        System.out.println("SQL connection succeed");
    }
    
    /**
     * Provides the global connection object to other DAOs and Controllers.
     * This is used by classes like 'CreateOrderController' to prepare statements.
     * * @return the active {@link java.sql.Connection} object.
     */
    public Connection getConnection() {
        return conn;
    }
    
    /**
     * Gracefully terminates the database connection.
     * This method should be invoked during the server shutdown sequence (e.g., when 
     * the 'Server Stop' button is clicked in the GUI).
     * * @throws SQLException if a database access error occurs during closure.
     */
    public void closeConnection() throws SQLException {
        // Verify that the connection exists and is still open before attempting to close
        if (conn != null && !conn.isClosed()) {
            conn.close();
            System.out.println("SQL connection closed successfully.");
        }
    }
}