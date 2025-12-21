package MainControllers;

import java.sql.SQLException;
import java.util.ArrayList;

import common.ServerIF;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

// הוספת ה-Imports עבור כל ה-Handlers ב-ServerLogic
import serverLogic.SubscriberLoginHandler;
import serverLogic.OccasionalLoginHandler;
import serverLogic.OccasionalRegistrationHandler;
import serverLogic.OccasionalRestUsernameHandler; // ה-Handler החדש שביקשת

/**
 * ServerController extends the OCSF AbstractServer and represents the
 * logical server component. It handles the routing of messages to specific handlers.
 */
public class ServerController extends AbstractServer {

    private ServerIF serverUI;

    /**
     * Constructs a new server instance.
     *
     * @param port the port number to listen on
     * @param serverUI the interface used to display messages in the server GUI
     */
    public ServerController(int port, ServerIF serverUI) {
        super(port);
        this.serverUI = serverUI;
    }

    /**
     * Initializes the database connection when the server starts.
     */
    @Override
    protected void serverStarted() {
        serverUI.appendLog("Server started.");

        try {
            // התחברות ל-DB דרך ה-Singleton
            DBController dbController = DBController.getInstance();
            dbController.connectToDB();

            serverUI.appendLog("Connected to database successfully.");
        } catch (SQLException e) {
            serverUI.appendLog("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Closes the database connection when the server stops.
     */
    @Override
    protected void serverStopped() {
        serverUI.appendLog("Server has stopped.");

        try {
            DBController.getInstance().closeConnection();
            serverUI.appendLog("Database connection closed.");
        } catch (SQLException e) {
            serverUI.appendLog("Error closing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        serverUI.appendLog("Client connected: IP = " + ip);
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        serverUI.appendLog("Client disconnected: " + client);
    }

    /**
     * Main message handler. Routes ArrayList commands to the appropriate logic handlers.
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        // רישום ההודעה הנכנסת בלוג של השרת
        serverUI.appendLog("Message received: " + msg + " from " + client);

        if (msg instanceof ArrayList) {
            @SuppressWarnings("unchecked")
            ArrayList<String> messageList = (ArrayList<String>) msg;
            
            if (messageList.isEmpty()) return;

            String command = messageList.get(0);

            switch (command) {
                case "LOGIN_SUBSCRIBER":
                    // ניתוב לטיפול בכניסת מנוי
                    new SubscriberLoginHandler().handle(messageList, client);
                    break;

                case "LOGIN_OCCASIONAL":
                    // ניתוב לטיפול בכניסת לקוח מזדמן
                    new OccasionalLoginHandler().handle(messageList, client);
                    break;

                case "RESET_OCCASIONAL_USERNAME":
                    // ניתוב לטיפול באיפוס שם משתמש ללקוח מזדמן (מחלקה חדשה)
                    new OccasionalRestUsernameHandler().handle(messageList, client);
                    break;

                case "REGISTER_OCCASIONAL":
                    // ניתוב לטיפול ברישום לקוח מזדמן חדש
                    new OccasionalRegistrationHandler().handle(messageList, client);
                    break;

                default:
                    serverUI.appendLog("Unknown command received: " + command);
                    try {
                        client.sendToClient("ERROR: Unknown Command");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } else {
            serverUI.appendLog("Received unexpected message type: " + msg.getClass().getSimpleName());
        }
    }
}