package serverLogic.managmentLogic;

import java.sql.*;
import java.util.ArrayList;
import common.Subscriber;
import MainControllers.DBController;

public class SubscriberDBController {
    
    public static ArrayList<Subscriber> getAllSubscribers() {
        ArrayList<Subscriber> list = new ArrayList<>();
        String query = "SELECT user_id, subscriber_id, username, qr_code FROM subscriber";
        
        Connection conn = DBController.getInstance().getConnection();
        
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new Subscriber(
                    rs.getInt("user_id"),
                    rs.getInt("subscriber_id"),
                    rs.getString("username"),
                    rs.getString("qr_code")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list; 
    }
}