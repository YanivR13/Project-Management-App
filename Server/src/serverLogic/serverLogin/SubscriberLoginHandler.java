package serverLogic.serverLogin;

import dbLogic.systemLogin.DBSubscriberConnection;
import ocsf.server.ConnectionToClient;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Specialized handler for the Subscriber login process on the server side.
 * This class processes "LOGIN_SUBSCRIBER" requests by validating credentials 
 * against the persistent database.
 * * <p>Role: Acts as a controller in the OCSF Command Pattern, decapsulating 
 * client messages and routing them to the {@link DBSubscriberConnection}.</p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public class SubscriberLoginHandler {

    /**
     * Executes the subscriber authentication logic.
     * * Message Protocol:
     * <ul>
     * <li><b>Incoming:</b> ArrayList containing [COMMAND, (String)subID]</li>
     * <li><b>Outgoing (Success):</b> ArrayList containing ["LOGIN_SUCCESS", (int)userId]</li>
     * <li><b>Outgoing (Failure):</b> Plain String message describing the error.</li>
     * </ul>
     * * @param data   The raw message data received from the client.
     * @param client The OCSF connection handle for the requesting client.
     */
    public void handle(ArrayList<Object> data, ConnectionToClient client) {
        try {
            /**
             * STEP 1: Data Extraction
             * The subscriber ID arrives as a String from the UI TextField. 
             * It is parsed to a long to match the BIGINT type in the MySQL 'subscriber' table.
             */
            long subID = Long.parseLong((String) data.get(1));

            /**
             * STEP 2: Database Verification
             * Delegates the SQL check to the specialized DB connector.
             * Returns the internal user_id (PK) if active, or -1 if authentication fails.
             */
            DBSubscriberConnection db = new DBSubscriberConnection();
            int userId = db.verifySubscriber(subID);

            /**
             * STEP 3: Response Dispatching
             */
            if (userId != -1) {
                // Success: Construct a list containing the status and the retrieved session ID
                ArrayList<Object> response = new ArrayList<>();
                response.add("LOGIN_SUCCESS");
                response.add(userId);
                
                // Transmit serialized object back to the client
                client.sendToClient(response);
            } else {
                // Logic Failure: Inform the UI that the ID is either invalid or inactive
                client.sendToClient("ERROR: Subscriber ID not found or inactive.");
            }
        } catch (NumberFormatException e) {
            // Handles cases where the input string cannot be converted to a numeric long
            sendError(client, "ERROR: Invalid ID format. Please enter numeric digits only.");
        } catch (Exception e) {
            // General exception safety for DB connectivity or OCSF stream issues
            e.printStackTrace();
            sendError(client, "ERROR: Internal Server Error during subscriber login.");
        }
    }

    /**
     * Utility method to safely push error notifications to the client GUI.
     * * @param client The client connection handle.
     * @param msg    The descriptive error message to be displayed in the client's txtLog.
     */
    private void sendError(ConnectionToClient client, String msg) {
        try { 
            client.sendToClient(msg); 
        } catch (IOException e) { 
            // Log catastrophic communication failures to the server console
            e.printStackTrace(); 
        }
    }
}