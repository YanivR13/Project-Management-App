package server;

import java.sql.SQLException;
import java.util.ArrayList;

import common.ServerIF;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

/**
 * ServerController extends the OCSF AbstractServer and represents the
 * logical server component. It handles:
 * - Starting and stopping the server
 * - Logging messages to the Server GUI
 * - Managing client connections
 * - Receiving and processing messages from clients
 * - Communicating with DBController for database operations
 */
public class ServerController extends AbstractServer {

    /**
     * Reference to the server GUI (or any class implementing ServerIF).
     * Used for displaying log messages.
     */
    private ServerIF serverUI;

    /**
     * Constructs a new server instance on a given port.
     *
     * @param port the port number to listen on
     * @param serverUI the GUI/log interface used to display messages
     */
    public ServerController(int port, ServerIF serverUI) {
        super(port);
        this.serverUI = serverUI;
    }

    /**
     * Called automatically by OCSF when the server begins listening.
     * Attempts to connect to the MySQL database and logs the results.
     */
    @Override
    protected void serverStarted() {
        serverUI.appendLog("Server started!");

        try {
            DBController.connectToDB();
            serverUI.appendLog("Connected to Database.");
        } catch (Exception e) {
            serverUI.appendLog("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Called automatically when the server stops listening.
     * Closes the database connection if open.
     */
    @Override
    protected void serverStopped() {
        serverUI.appendLog("Server has stopped.");

        try {
            if (DBController.conn != null && !DBController.conn.isClosed()) {
                DBController.conn.close();
                serverUI.appendLog("Database connection closed.");
            }
        } catch (Exception e) {
            serverUI.appendLog("Error closing database: " + e.getMessage());
        }
    }

    /**
     * Called automatically when a new client connects.
     * Logs the client's IP and host name.
     *
     * @param client the connection object representing the client
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        String host = client.getInetAddress().getHostName();

        serverUI.appendLog("Client connected: IP = " + ip + ", Host = " + host);
    }
    
    
    /**
     * Called automatically by OCSF when a client disconnects from the server.
     *
     * NOTE: This method will be relevant once we add logout functionality
     *       or client-side exit handling in the project.
     *
     * @param client The client connection that has just disconnected.
     */
    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        String host = client.getInetAddress().getHostName();

        serverUI.appendLog("Client disconnected: IP = " + ip + ", Host = " + host);
    }
    

    /**
     * Called whenever a message arrives from a client.
     * Handles two kinds of messages:
     * 1. A command string ("display")
     * 2. A list representing an update request (["update", id, date, guests])
     *
     * @param msg the message sent from the client
     * @param client the client that sent the message
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        serverUI.appendLog("Message received: " + msg + " from " + client);

        // Case 1: Client asks to display all orders
        if (msg instanceof String) {
            String command = (String) msg;

            if (command.equals("display")) {
                ArrayList<String> orders = DBController.getAllOrders();

                try {
                    client.sendToClient(orders);
                    serverUI.appendLog("Sent orders to client");
                } catch (Exception e) {
                    serverUI.appendLog("Error sending data to client: " + e.getMessage());
                }
            }
        }

        // Case 2: Client sends an update request
        else if (msg instanceof ArrayList) {
            ArrayList<String> list = (ArrayList<String>) msg;
            String command = list.get(0);

            if (command.equals("update")) {

                String id = list.get(1);
                String date = list.get(2);
                String guests = list.get(3);

                boolean success = DBController.updateOrder(id, date, guests);

                try {
                    if (success) {
                        client.sendToClient("Update successful for Order ID: " + id);
                        serverUI.appendLog("Updated Order " + id);
                    } else {
                        // ID does not exist
                        client.sendToClient("Error: Order ID " + id + " does not exist.");
                        serverUI.appendLog("Update failed - ID not found: " + id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
