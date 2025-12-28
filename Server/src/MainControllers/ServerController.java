package MainControllers;

import java.io.IOException;
import java.sql.SQLException;
import serverLogic.menuLogic.*;
import serverLogic.serverRestaurant.RestaurantManager;
import java.util.ArrayList;

import common.Restaurant;
import common.ServerIF;
import common.ServiceResponse;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverLogic.serverLogin.OccasionalLoginHandler;
import serverLogic.serverLogin.OccasionalRegistrationHandler;
import serverLogic.serverLogin.OccasionalRestUsernameHandler;
import serverLogic.serverLogin.SubscriberLoginHandler;

/**
 * The ServerController class is the central communication hub for the Bistro Server.
 * It extends the OCSF {@link AbstractServer} to provide robust socket communication 
 * and multi-threaded client management.
 * * Primary Responsibilities:
 * 1. Lifecycle Management: Starting/Stopping the server and the DB connection.
 * 2. Resource Initialization: Pre-loading restaurant data into RAM for fast access.
 * 3. Command Routing: Parsing incoming ArrayList protocols and delegating to 
 * specialized business logic handlers.
 * * @author Software Engineering Student
 * @version 1.0
 * @see ocsf.server.AbstractServer
 */
public class ServerController extends AbstractServer {

    /** * Interface reference for UI logging. Allows the controller to push 
     * status updates to the Server GUI.
     */
    private ServerIF serverUI;

    /**
     * Constructs the server controller instance.
     * * @param port     The dedicated TCP port for the server to listen on (e.g., 5555).
     * @param serverUI The interface implementation for GUI logging.
     */
    public ServerController(int port, ServerIF serverUI) {
        super(port);
        this.serverUI = serverUI;
    }

    /**
     * HOOK METHOD: Executed automatically when the server starts listening for clients.
     * Performs critical infrastructure setup including Database connection and 
     * RAM cache initialization.
     */
    @Override
    protected void serverStarted() {
        serverUI.appendLog("Server started.");

        try {
            // STEP 1: Establish Database connectivity via the DB Singleton
            DBController dbController = DBController.getInstance();
            dbController.connectToDB();
            serverUI.appendLog("Connected to database successfully.");

            /**
             * STEP 2: RAM Optimization (Caching)
             * Pre-loads restaurant metadata, inventory, and hours into the RestaurantManager.
             * This avoids redundant SQL queries for every reservation availability check.
             * Defaults to Restaurant ID #1 for this system iteration.
             */
            if (RestaurantManager.initialize(1)) {
                serverUI.appendLog("Restaurant data initialized in RAM (Inventory & Hours).");
            } else {
                serverUI.appendLog("Warning: Restaurant data could not be loaded. Check if DB is empty.");
            }

        } catch (SQLException e) {
            serverUI.appendLog("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * HOOK METHOD: Executed when the server stops listening.
     * Ensures graceful termination of the database connection to prevent resource leaks.
     */
    @Override
    protected void serverStopped() {
        serverUI.appendLog("Server has stopped.");

        try {
            // Cleanly close the JDBC connection
            DBController.getInstance().closeConnection();
            serverUI.appendLog("Database connection closed.");
        } catch (SQLException e) {
            serverUI.appendLog("Error closing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Triggered when a new socket connection is accepted from a client.
     * @param client The specific connection handle.
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        serverUI.appendLog("Client connected: IP = " + ip);
    }

    /**
     * Triggered when a client closes their session or is disconnected.
     * @param client The specific connection handle.
     */
    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        serverUI.appendLog("Client disconnected: " + client);
    }

    /**
     * CORE ROUTING LOGIC: Processes all incoming objects from clients.
     * This method implements the Command Pattern approach, extracting a string 
     * command from the message protocol and delegating work to a dedicated handler class.
     * * Message Protocol: Expects an {@link ArrayList} where:
     * - Index 0: String command (e.g., "LOGIN_SUBSCRIBER")
     * - Index 1+: Optional payload data.
     * * @param msg    The incoming message object.
     * @param client The specific client that sent the message.
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
    	        
        // Log activity for server monitoring
        serverUI.appendLog("Message received: " + msg + " from " + client);

        // Protocol Validation: Ensure the message is an ArrayList as per system standard
        if (msg instanceof ArrayList) {
            @SuppressWarnings("unchecked")
            ArrayList<Object> messageList = (ArrayList<Object>) msg;
            
            if (messageList.isEmpty()) {
                serverUI.appendLog("Warning: Received empty ArrayList from " + client);
                return;
            }

            // Command extraction
            String command = (String) messageList.get(0);

            /**
             * Dispatcher Switch:
             * Routes the message to the appropriate functional handler class.
             * This keeps the ServerController code clean and maintainable.
             */
            switch (command) {
                case "LOGIN_SUBSCRIBER":
                    new SubscriberLoginHandler().handle(messageList, client);
                    break;

                case "LOGIN_OCCASIONAL":
                    new OccasionalLoginHandler().handle(messageList, client);
                    break;

                case "RESET_OCCASIONAL_USERNAME":
                    new OccasionalRestUsernameHandler().handle(messageList, client);
                    break;

                case "REGISTER_OCCASIONAL":
                    new OccasionalRegistrationHandler().handle(messageList, client);
                    break;
                    
                case "GET_RESERVATIONS_HISTORY":
                	new ReservationHistoryHandler().handle(messageList, client);
                	break;
                	
                case "GET_SUBSCRIBER_DETAILS":
                	new EditDetailsHandler().handle(messageList, client);
                	break;
                    
                case "GET_RESTAURANT_WORKTIMES":
                    try {
                        Restaurant rest = RestaurantManager.getInstance();
                        
                        if (rest != null) {
                            client.sendToClient(rest);
                            serverUI.appendLog("Successfully sent restaurant worktimes to " + client);
                        } else {
                            serverUI.appendLog("Error: Restaurant data is null in RAM!");
                            client.sendToClient(new ServiceResponse(
                                ServiceResponse.ReservationResponseStatus.INTERNAL_ERROR, 
                                "Server Error: Restaurant data not found."
                            ));
                        }
                    } catch (IOException e) {
                        serverUI.appendLog("Failed to transmit restaurant data: " + e.getMessage());
                    }
                    break;
                    
                case "CREATE_RESERVATION":
                    /**
                     * Routes to the CreateOrderHandler which manages table allocation 
                     * logic and interaction with the 'reservation' table.
                     */
                    serverUI.appendLog("Routing to CreateOrderHandler for Client: " + client);
                    new CreateOrderHandler().handle(messageList, client);
                    break;

                default:
                    // Protocol Fallback: Handle unrecognized commands
                    serverUI.appendLog("Unknown command received: " + command);
                    try {
                        client.sendToClient(new ServiceResponse(
                            ServiceResponse.ReservationResponseStatus.INTERNAL_ERROR, 
                            "ERROR: Unknown Command '" + command + "'"
                        ));
                    } catch (Exception e) {
                        serverUI.appendLog("Failed to send Error message: " + e.getMessage());
                    }
                    break;
            }
        } else {
            // Protocol Violation Fallback: Handle non-ArrayList objects
            serverUI.appendLog("Received unexpected message type: " + msg.getClass().getSimpleName());
            try {
                client.sendToClient(new ServiceResponse(
                    ServiceResponse.ReservationResponseStatus.INTERNAL_ERROR, 
                    "ERROR: Invalid Protocol (Expected ArrayList)"
                ));
            } catch (Exception e) {
                serverUI.appendLog("Error notifying client: " + e.getMessage());
            }
        }
    }
}