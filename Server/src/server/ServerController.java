package server;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import gui.ServerPortFrameController;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class ServerController extends AbstractServer {

    // מחקתי מכאן את private static Connection conn - אנחנו נשתמש בזה של DBController

    private ServerPortFrameController serverUI;

    public ServerController(int port, ServerPortFrameController serverUI) {
        super(port);
        this.serverUI = serverUI;
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        serverUI.appendLog("Message received: " + msg + " from " + client);

        if (msg instanceof String && ((String)msg).equals("display")) {
            
            // עטפתי ב-try-catch כדי לתפוס שגיאות שליפה (כמו NullPointer)
            try {
                // 1. שליפת הנתונים מה-DB
                ArrayList<ArrayList<String>> orders = DBController.getOrdersFromDB();
                
                // בדיקה אם חזר null (למניעת קריסה)
                if (orders == null) {
                    serverUI.appendLog("Error: DB returned null orders!");
                    return;
                }

                // 2. שליחה ללקוח
                client.sendToClient(orders);
                serverUI.appendLog("Sent " + orders.size() + " orders to client.");
                
            } catch (Exception e) {
                serverUI.appendLog("CRITICAL ERROR in display: " + e.toString());
                e.printStackTrace(); // ידפיס גם לקונסולה למטה ליתר ביטחון
            }
        }
        else if (msg instanceof ArrayList) {
            ArrayList<String> data = (ArrayList<String>) msg;
            
            if (data.get(0).equals("update")) {
                 data.remove(0); 
                 updateOrderInDB(data);
                 try {
                    client.sendToClient("Server: Order updated successfully!");
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    private void updateOrderInDB(ArrayList<String> data) {
        // גם כאן - נשתמש ב-DBController.conn ישירות אם צריך, 
        // אבל עדיף להעביר את כל הלוגיקה של ה-SQL לתוך DBController בעתיד.
        // כרגע נשאיר את זה פשוט:
        try {
            if (DBController.conn == null) connectToDB(); // שימוש במשתנה הנכון
            
            String query = "UPDATE `orders` SET order_date = ?, number_of_guests = ? WHERE order_number = ?";
            java.sql.PreparedStatement pstmt = DBController.conn.prepareStatement(query);
            
            pstmt.setString(1, data.get(1)); 
            pstmt.setInt(2, Integer.parseInt(data.get(2))); 
            pstmt.setInt(3, Integer.parseInt(data.get(0))); 
            
            pstmt.executeUpdate();
            serverUI.appendLog("Database updated via SQL.");
            
        } catch (Exception e) {
            serverUI.appendLog("DB Error: " + e.getMessage());
        }
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        String clientInfo = "Client connected: " + client.getInetAddress().getHostAddress() 
                          + " (" + client.getInetAddress().getHostName() + ")";
        serverUI.appendLog(clientInfo);
    }

    @Override
    protected void serverStarted() {
        serverUI.appendLog("Server listening for connections on port " + getPort());
        connectToDB();
    }

    @Override
    protected void serverStopped() {
        serverUI.appendLog("Server has stopped listening for connections.");
        try {
            if (DBController.conn != null) DBController.conn.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void connectToDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            // *** התיקון הקריטי: שים לב שאני מכניס את החיבור לתוך DBController.conn ***
            DBController.conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/prototypedb?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false", "root", "Rochlin99!");
            
            serverUI.appendLog("SQL connection succeed");
            
        } catch (Exception ex) {
            serverUI.appendLog("DB Connection Failed! " + ex.getMessage());
        }
    }
}