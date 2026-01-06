package dbLogic.managmentDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import MainControllers.DBController;

public class reportsDBController {

    /**
     * שליפת נתוני איחורים וזמנים לפי חודש
     */
    public static List<Map<String, Object>> getTimeReportData(String month) throws Exception {
        List<Map<String, Object>> reportList = new ArrayList<>();
        
        // שאילתה שמחברת הזמנה, ביקור וחשבונית
        //TIMESTAMPDIFF מחשב את ההפרש בדקות בין זמן ההזמנה לזמן הכניסה בפועל
        String sql = "SELECT r.reservation_datetime, v.start_time, b.payment_time, " +
                     "TIMESTAMPDIFF(MINUTE, r.reservation_datetime, v.start_time) AS delay_minutes " +
                     "FROM reservation r " +
                     "JOIN visit v ON r.confirmation_code = v.confirmation_code " +
                     "LEFT JOIN bill b ON v.bill_id = b.bill_id " +
                     "WHERE MONTH(r.reservation_datetime) = ?";

        Connection conn = DBController.getInstance().getConnection();
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, month);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("reserved", rs.getTimestamp("reservation_datetime"));
                    row.put("arrival", rs.getTimestamp("start_time"));
                    row.put("departure", rs.getTimestamp("payment_time"));
                    row.put("delay", rs.getInt("delay_minutes"));
                    reportList.add(row);
                }
            }
        }
        return reportList;
    }
}