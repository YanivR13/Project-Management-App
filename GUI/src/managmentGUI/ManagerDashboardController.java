package managmentGUI;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
    
    @FXML
    public void openMonthSelection(ActionEvent event) {
        try {
            // טעינת הקובץ של מסך בחירת חודש
        	FXMLLoader loader = new FXMLLoader(
        		    getClass().getResource("/managmentGUI/ActionsFXML/monthSelection.fxml")
        		);

            Parent root = loader.load();
            
            MonthSelectionController ctrl = loader.getController();
            ctrl.setClient(this.client); 
            
            // יצירת חלון חדש
            Stage stage = new Stage();
            stage.setTitle("Month Selection");
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (Exception e) {
            // אם לא נפתח – תוצג הודעה במסך עצמו
            System.out.println("Failed to open month screen: " + e.getMessage());
            e.printStackTrace();
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