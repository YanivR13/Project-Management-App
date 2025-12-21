package serverLogic;

import dbLogic.systemLogin.DBOccasionalConnection;
import ocsf.server.ConnectionToClient;
import java.util.ArrayList;

public class OccasionalRegistrationHandler {
    public void handle(ArrayList<String> data, ConnectionToClient client) {
        try {
            String user = data.get(1);
            String contact = data.get(2);

            DBOccasionalConnection db = new DBOccasionalConnection();
            String result = db.registerNewOccasional(user, contact);

            client.sendToClient(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}