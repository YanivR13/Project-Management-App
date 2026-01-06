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
	    
	    // 1. חישוב השנה הרלוונטית
	    int selectedMonth = Integer.parseInt(monthStr);
	    LocalDate now = LocalDate.now(); // היום זה 2026-01-07
	    int currentMonth = now.getMonthValue();
	    int currentYear = now.getYear();
	    
	    int yearToFetch;
	    if (selectedMonth <= currentMonth) {
	        yearToFetch = currentYear;      // החודש כבר היה או קורה עכשיו ב-2026
	    } else {
	        yearToFetch = currentYear - 1;  // החודש עוד לא הגיע השנה, ניקח מ-2025
	    }

	    // 2. השאילתה המעודכנת עם סינון שנה
	    String sql = "SELECT r.reservation_datetime, v.start_time, b.payment_time, " +
	                 "TIMESTAMPDIFF(MINUTE, r.reservation_datetime, v.start_time) AS delay_minutes " +
	                 "FROM reservation r " +
	                 "JOIN visit v ON r.confirmation_code = v.confirmation_code " +
	                 "LEFT JOIN bill b ON v.bill_id = b.bill_id " +
	                 "WHERE MONTH(r.reservation_datetime) = ? AND YEAR(r.reservation_datetime) = ?";

	    Connection conn = DBController.getInstance().getConnection();
	    
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, selectedMonth);
	        ps.setInt(2, yearToFetch); // כאן אנחנו מזריקים את השנה שחישבנו
	        
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                Map<String, Object> row = new HashMap<>();
	                row.put("reserved", rs.getTimestamp("reservation_datetime"));
	                row.put("delay", rs.getInt("delay_minutes"));
	                // ... שאר השדות
	                reportList.add(row);
	            }
	        }
	    }
	    return reportList;
	}
}