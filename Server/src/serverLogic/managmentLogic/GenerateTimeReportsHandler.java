package serverLogic.managmentLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.ServiceResponse;
import common.ServiceResponse.ServiceStatus;
import dbLogic.managmentDB.reportsDBController;
import ocsf.server.ConnectionToClient;

/**
 * Handler האחראי על הפקת דו"ח זמנים (איחורים ממוצעים ליום)
 */
public class GenerateTimeReportsHandler {

    /**
     * מתודת הטיפול עבור פקודת GET_TIME_REPORTS
     */
	public void handle(ArrayList<Object> messageList, ConnectionToClient client) {
        try {
            // 1. חילוץ החודש מההודעה
            String month = (String) messageList.get(2);
            
            // 2. קריאה ל-DB לקבלת הנתונים
            List<Map<String, Object>> reportData = reportsDBController.getTimeReportData(month);
            
            // 3. יצירת רשימה למענה ללקוח
            ArrayList<Object> response = new ArrayList<>();
            
            if (reportData == null || reportData.isEmpty()) {
                response.add("REPORT_ERROR");
                response.add("No data found for the selected month.");
            } else {
                // אנחנו מוסיפים "תגית" כדי שהלקוח ידע איזה דוח זה
                response.add("REPORT_TIME_DATA_SUCCESS"); 
                response.add(reportData); // הרשימה עצמה
            }

            // 4. שליחה ישירה של ה-ArrayList
            client.sendToClient(response);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                ArrayList<Object> err = new ArrayList<>();
                err.add("REPORT_ERROR");
                err.add("Server failed to generate report.");
                client.sendToClient(err);
            } catch (Exception ignored) {}
        }
    }
}