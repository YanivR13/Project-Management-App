package serverLogic;

import dbLogic.systemLogin.DBOccasionalConnection;
import ocsf.server.ConnectionToClient;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Handler for the Occasional Customer login process.
 */
public class OccasionalLoginHandler {

    /**
     * Handles the login verification.
     * @param data ArrayList: [LOGIN_OCCASIONAL, username, contact]
     * @param client The client connection.
     */
    public void handle(ArrayList<String> data, ConnectionToClient client) {
        try {
            String username = data.get(1);
            String contact = data.get(2);

            DBOccasionalConnection db = new DBOccasionalConnection();
            boolean isValid = db.verifyOccasional(username, contact);

            if (isValid) {
                // שליחת הודעת הצלחה ספציפית למזדמן
                client.sendToClient("LOGIN_OCCASIONAL_SUCCESS");
            } else {
                client.sendToClient("ERROR: Invalid username or contact information.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient("ERROR: Internal server error during login.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}