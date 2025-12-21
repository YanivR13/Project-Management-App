package serverLogic;

import dbLogic.systemLogin.DBOccasionalConnection;
import ocsf.server.ConnectionToClient;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Handler for resetting the username of an occasional customer.
 */
public class OccasionalRestUsernameHandler {

    /**
     * Handles the reset request.
     * @param data ArrayList containing [COMMAND, contactInfo, newUsername]
     * @param client The connection to the specific client.
     */
    public void handle(ArrayList<String> data, ConnectionToClient client) {
        try {
            // חילוץ הנתונים מהרשימה
            String contactInfo = data.get(1);
            String newUsername = data.get(2);

            // פנייה לשכבת בסיס הנתונים
            DBOccasionalConnection db = new DBOccasionalConnection();
            String result = db.resetUsername(contactInfo, newUsername);

            // שליחת התשובה חזרה ללקוח (הצלחה או הודעת שגיאה ספציפית)
            client.sendToClient(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(client, "ERROR: Internal Server Error during reset.");
        }
    }

    private void sendError(ConnectionToClient client, String msg) {
        try {
            client.sendToClient(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}