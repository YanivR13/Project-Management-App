package managmentGUI;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * קונטרולר עבור דשבורד מנהל.
 * יורש את כל הפונקציונליות של נציג (נציג שירות) ומוסיף יכולות הפקת דוחות.
 */
public class ManagerDashboardController extends RepresentativeDashboardController {

    /**
     * מתודה המתבצעת כאשר הלקוח מוכן.
     * אנו משתמשים במימוש של מחלקת האב כדי לרשום את ה-UI ולטעון מסך התחלתי.
     */
    @Override
    public void onClientReady() {
        super.onClientReady();// הפעלת הלוגיקה הקיימת של הנציג 
        appendLog("Manager Mode Active: Additional reporting tools enabled.");
    }

    /**
     * מתודה להנפקת דוחות זמנים.
     * שולחת בקשה לשרת לקבלת נתוני פעילות המסעדה.
     */
    @FXML
    void generateTimeReports(ActionEvent event) {
        // בניית הפרוטוקול לשליחה לשרת (דומה למבנה של עדכון שעות) 
        ArrayList<Object> message = new ArrayList<>();
        message.add("GET_TIME_REPORTS"); // פקודה לשרת
        message.add(1); // מזהה המסעדה (כפי שנעשה ב-UpdateRegularHours) 

        appendLog("Requesting Time Reports from server...");
        
        if (client != null) {
            client.handleMessageFromClientUI(message); // שליחה דרך ה-OCSF
        }
    } 

    
     // מתודת להנפקת דוחות מנויים.
     // שולחת בקשה לשרת לקבלת סטטיסטיקות על מנויי המערכת.
     
    @FXML
    void generateSubscriberReports(ActionEvent event) {
        ArrayList<Object> message = new ArrayList<>();
        message.add("GET_SUBSCRIBER_REPORTS");
        message.add(1); // מזהה המסעדה

        appendLog("Requesting Subscriber Reports from server...");

        if (client != null) {
            client.handleMessageFromClientUI(message);
        }
    }
    
    @Override
    public void display(Object message) {
        // 1. בדיקה אם המסר הוא פלט ששייך לדוחות (למשל מחרוזת פשוטה מהשרת)
        if (message instanceof String && ((String) message).contains("REPORT")) {
            String reportData = (String) message;
            
            // הצגת הפלט בלוגר של המנהל
            Platform.runLater(() -> {
                appendLog("RECEIVED REPORT DATA:");
                appendLog(reportData);
            });
        } 
        // 2. אם זה לא דוח, שלח את זה לטיפול של מחלקת האב (הנציג)
        else {
            super.display(message); // זה יפעיל את הלוגיקה המקורית של הנציג עבור ServiceResponse וכו'
        }
    }
}