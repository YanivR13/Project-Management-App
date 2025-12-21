package serverLogic;

import dbLogic.systemLogin.DBSubscriberConnection;
import ocsf.server.ConnectionToClient;
import java.util.ArrayList;
import java.io.IOException;

public class SubscriberLoginHandler {
    public void handle(ArrayList<String> data, ConnectionToClient client) {
        try {
            // Extract Subscriber ID from the list
            long subID = Long.parseLong(data.get(1));

            // Use the DB Logic layer to verify
            DBSubscriberConnection db = new DBSubscriberConnection();
            boolean isValid = db.verifySubscriber(subID);

            if (isValid) {
                client.sendToClient("LOGIN_SUCCESS");
            } else {
                client.sendToClient("ERROR: Subscriber ID not found.");
            }
        } catch (Exception e) {
            sendError(client, "ERROR: Internal Server Error");
        }
    }

    private void sendError(ConnectionToClient client, String msg) {
        try { client.sendToClient(msg); } catch (IOException e) { e.printStackTrace(); }
    }
}