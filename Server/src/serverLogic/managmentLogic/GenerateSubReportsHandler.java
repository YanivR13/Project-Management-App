package serverLogic.managmentLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import dbLogic.managmentDB.reportsDBController;
import ocsf.server.ConnectionToClient;

public class GenerateSubReportsHandler {
    public void handle(ArrayList<Object> messageList, ConnectionToClient client) {
        String month = (String) messageList.get(1);
        ArrayList<Object> response = new ArrayList<>();
        response.add("RECEIVE_SUBSCRIBER_REPORTS"); // פקודה חזרה ללקוח

        try {
            List<Map<String, Object>> data = reportsDBController.getSubReportData(month);
            response.add(data);
            client.sendToClient(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.add(new ArrayList<Map<String, Object>>()); // רשימה ריקה במקרה שגיאה
            try { client.sendToClient(response); } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}