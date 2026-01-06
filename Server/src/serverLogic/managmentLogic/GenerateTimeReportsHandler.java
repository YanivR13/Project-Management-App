package serverLogic.managmentLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.ServiceResponse;
import common.ServiceResponse.ServiceStatus;
import dbLogic.managmentDB.reportsDBController;
import ocsf.server.ConnectionToClient;

public class GenerateTimeReportsHandler {

    public void handle(ArrayList<Object> messageList, ConnectionToClient client) {
        try {
            // חילוץ החודש מההודעה
            String month = (String) messageList.get(2);
            
            // קריאה ל-DB (בצורה סטטית כפי שביקשת)
            List<Map<String, Object>> reportData = reportsDBController.getTimeReportData(month);
            
            // שליחת התשובה ללקוח
            // אנחנו שולחים את המילה "REPORT" בתוך המחרוזת כדי שהקונטרולר שלך יזהה את זה
            client.sendToClient(new ServiceResponse(
                ServiceStatus.UPDATE_SUCCESS, 
                "REPORT_TIME_DATA:" + reportData.toString()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient(new ServiceResponse(ServiceStatus.INTERNAL_ERROR, "Failed to gen report"));
            } catch (Exception ignored) {}
        }
    }
}