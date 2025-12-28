package serverLogic.menuLogic;

import java.util.ArrayList;

import dbLogic.systemLogin.DBSubscriberDetails;
import ocsf.server.ConnectionToClient;

/**
 * Handles server-side logic for editing subscriber personal details.
 * Delegates the actual database update to DBSubscriberDetails.
 */
public class EditDetailsHandler {

    /**
     * Incoming message format:
     * [0] "EDIT_SUBSCRIBER_DETAILS"
     * [1] userId
     * [2] username (may be empty)
     * [3] phone (may be empty)
     * [4] email (may be empty)
     */
    public void handle(ArrayList<Object> data, ConnectionToClient client) {

        try {
            int userId = (int) data.get(1);
            String username = (String) data.get(2);
            String phone = (String) data.get(3);
            String email = (String) data.get(4);

            // Delegate to DB layer
            DBSubscriberDetails db = new DBSubscriberDetails();
            boolean updated = db.updateSubscriberDetails(userId, username,phone,email);

            // Response back to client
            ArrayList<Object> response = new ArrayList<>();
            response.add("EDIT_DETAILS_RESULT");
            response.add(updated ? "SUCCESS" : "NO_CHANGES");

            client.sendToClient(response);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient("ERROR_EDITING_DETAILS");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
