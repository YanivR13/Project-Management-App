package dbLogic.managmentDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import MainControllers.DBController;

public class reportsDBController {

    /**
     * שליפת נתוני איחורים וזמנים לפי חודש
     */
	public static List<Map<String, Object>> getTimeReportData(String monthStr) throws Exception {
	    List<Map<String, Object>> reportList = new ArrayList<>();
	    
	    int selectedMonth;
	    try { selectedMonth = Integer.parseInt(monthStr); } 
	    catch (Exception e) { selectedMonth = java.time.Month.valueOf(monthStr.toUpperCase()).getValue(); }

	    // שאילתה המשלבת 3 טבלאות: reservation, visit, bill
	    String sql = "SELECT DATE(v.start_time) AS date, " +
	                 "AVG(TIMESTAMPDIFF(MINUTE, r.reservation_datetime, v.start_time)) AS avg_delay, " +
	                 "AVG(TIMESTAMPDIFF(MINUTE, v.start_time, b.payment_time)) AS avg_duration, " + // זמן שהייה מהתשלום
	                 "HOUR(v.start_time) AS arrival_hour, " +
	                 "HOUR(b.payment_time) AS departure_hour " + // שעת עזיבה מהתשלום
	                 "FROM visit v " +
	                 "JOIN reservation r ON v.confirmation_code = r.confirmation_code " +
	                 "JOIN bill b ON v.bill_id = b.bill_id " + // חיבור לחשבונית
	                 "WHERE MONTH(v.start_time) = ? AND YEAR(v.start_time) = 2026 " +
	                 "GROUP BY date, arrival_hour, departure_hour";

	    Connection conn = MainControllers.DBController.getInstance().getConnection();
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, selectedMonth);
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                Map<String, Object> row = new HashMap<>();
	                row.put("date", rs.getDate("date").toString());
	                row.put("delay", rs.getDouble("avg_delay"));
	                row.put("duration", rs.getDouble("avg_duration"));
	                row.put("arr_hour", rs.getInt("arrival_hour"));
	                row.put("dep_hour", rs.getInt("departure_hour"));
	                reportList.add(row);
	            }
	        }
	    }
	    return reportList;
	}
	
	public static List<Map<String, Object>> getSubReportData(String monthStr) throws Exception {
	    List<Map<String, Object>> reportList = new ArrayList<>();
	    
	    // המרת שם החודש למספר
	    int selectedMonth;
	    try {
	        selectedMonth = Integer.parseInt(monthStr);
	    } catch (NumberFormatException e) {
	        selectedMonth = java.time.Month.valueOf(monthStr.toUpperCase()).getValue();
	    }

	    // לוגיקת בחירת שנה (2026 אם כבר עבר, אחרת 2025)
	    int currentMonth = java.time.LocalDate.now().getMonthValue();
	    int yearToFetch = (selectedMonth <= currentMonth) ? 2026 : 2025;

	    // שאילתה שסופרת הזמנות והמתנות לכל יום בחודש שבו היו נתונים
	    String sql = "SELECT report_date, SUM(is_res) as res_count, SUM(is_wait) as wait_count FROM (" +
	                 "  SELECT DATE(reservation_datetime) as report_date, 1 as is_res, 0 as is_wait FROM reservation " +
	                 "  WHERE MONTH(reservation_datetime) = ? AND YEAR(reservation_datetime) = ? " +
	                 "  UNION ALL " +
	                 "  SELECT DATE(entry_time) as report_date, 0 as is_res, 1 as is_wait FROM waiting_list_entry " +
	                 "  WHERE MONTH(entry_time) = ? AND YEAR(entry_time) = ? " +
	                 ") AS combined GROUP BY report_date ORDER BY report_date ASC";

	    Connection conn = MainControllers.DBController.getInstance().getConnection();
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, selectedMonth);
	        ps.setInt(2, yearToFetch);
	        ps.setInt(3, selectedMonth);
	        ps.setInt(4, yearToFetch);
	        
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                Map<String, Object> row = new HashMap<>();
	                row.put("date", rs.getDate("report_date").toString());
	                row.put("reservations", rs.getInt("res_count"));
	                row.put("waiting", rs.getInt("wait_count"));
	                reportList.add(row);
	            }
	        }
	    }
	    return reportList;
	}
}