package CardReader; 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardReaderController {
    
  
    private final String url = "jdbc:mysql://localhost:3306/prototypedb";
    private final String username = "root";
    private final String password = "Ya212104483"; 

    private String currentSubscriberID;

    /**
     * Function 1: Subscriber validation
     * Checks whether the ID exists in the subscriber table according to the subscriber_id column
     */

    public boolean validateSubscriber(String id) {
       
        String query = "SELECT * FROM subscriber WHERE subscriber_id = ?"; 
        
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                this.currentSubscriberID = id;
                return true; // A match was found in the DB

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // No subscriber was found or there is a connection error

    }
    
    
    /**
     * Function 2: I lost the verification code
     * Retrieves all codes for the connected subscriber
     */

    public List<String> getLostConfirmationCodes() {
        List<String> activeCodes = new ArrayList<>();

        String query = "SELECT confirmation_code, order_number FROM orders WHERE subscriber_id = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentSubscriberID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String code = rs.getString("confirmation_code"); 
                String orderNum = rs.getString("order_number");   
                activeCodes.add("Order #" + orderNum + " | Confirmation Code: " + code);
            }
        } catch (SQLException e) {
            System.err.println("Database Error (Lost Code): " + e.getMessage());
        }
        
        return activeCodes;
    }
    
    /**
     * Function 3: Verification of confirmation code and check-in
     * Checks that the code is active (ACTIVE) and updates it to COMPLETED
     */

    public String verifyConfirmationCode(String code) {

    	String checkQuery = "SELECT * FROM orders WHERE confirmation_code = ? AND subscriber_id = ? AND status = 'ACTIVE'";
        String updateQuery = "UPDATE orders SET status = 'COMPLETED' WHERE confirmation_code = ? AND subscriber_id = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
        	// Step 1: Check if the code exists with ACTIVE status

            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setString(1, code);
                pstmt.setString(2, currentSubscriberID);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                	// Step 2: Update the status in the DB (the code has been used)

                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateQuery)) {
                        updatePstmt.setString(1, code);
                        updatePstmt.setString(2, currentSubscriberID);
                        updatePstmt.executeUpdate();
                    }
                    return "Success: Welcome to the restaurant!";
                } else {
                    return "Error: Invalid or expired code.";
                }
            }
        } catch (SQLException e) {
            return "Database Error: " + e.getMessage();
        }
    }
}