package server;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import common.ServerIF;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class ServerController extends AbstractServer {

    // שינוי: עכשיו אנחנו מחזיקים ממשק כללי ולא את ה-GUI הספציפי
    private ServerIF serverUI;

    // שינוי: הבנאי מקבל את הממשק
    public ServerController(int port, ServerIF serverUI) {
        super(port);
        this.serverUI = serverUI;
    }

    // ... שאר הקוד נשאר כמעט אותו דבר ...

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        serverUI.appendLog("Message received: " + msg + " from " + client);

        // אפשרות א': הלקוח שלח פקודה בודדת (כמו "display")
        if (msg instanceof String) {
            String command = (String) msg;
            if (command.equals("display")) {
                ArrayList<String> orders = DBController.getAllOrders();
                try {
                    client.sendToClient(orders);
                    serverUI.appendLog("Sent orders to client");
                } catch (Exception e) {
                    serverUI.appendLog("Error sending to client: " + e.getMessage());
                }
            }
        }
        
        // אפשרות ב': הלקוח שלח רשימה (כמו ["update", "123", "2025-01-01", "50"])
        else if (msg instanceof ArrayList) {
            ArrayList<String> list = (ArrayList<String>) msg;
            String command = list.get(0); // האיבר הראשון הוא הפקודה
            
            if (command.equals("update")) {
                // חילוץ הנתונים מהרשימה
                String id = list.get(1);
                String date = list.get(2);
                String guests = list.get(3);
                
                // ביצוע העדכון ב-DB
                boolean success = DBController.updateOrder(id, date, guests);
                
                try {
                    if (success) {
                        client.sendToClient("Update successful for Order ID: " + id);
                        serverUI.appendLog("Updated Order " + id);
                    } else {
                        client.sendToClient("Error: Update failed for Order ID: " + id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // ... שאר הפונקציות משתמשות ב-serverUI.appendLog וזה יעבוד מצוין ...
    
    public void connectToDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            DBController.conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/prototypedb?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false", "root", "Rochlin99!");
            serverUI.appendLog("SQL connection succeed");
        } catch (Exception ex) {
            serverUI.appendLog("DB Connection Failed! " + ex.getMessage());
        }
    }
    
    // ... אל תשכח את שאר ה-Override methods (serverStarted וכו') ...
}