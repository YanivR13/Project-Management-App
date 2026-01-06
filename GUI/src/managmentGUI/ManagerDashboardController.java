package managmentGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.ServiceResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
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
        if (message instanceof ServiceResponse) {
            ServiceResponse res = (ServiceResponse) message;
            Object data = res.getData();

            // בדיקה: האם המידע שהגיע הוא נתונים של דוח זמנים?
            if (data instanceof List && !((List<?>) data).isEmpty()) {
                // אנחנו מניחים שהשרת שלח List של Maps כפי שתכננו
                Platform.runLater(() -> {
                    showGraph((List<Map<String, Object>>) data);
                });
            } 
            else {
                // אם זה לא דוח, שלח לטיפול הרגיל של הנציג (הדפסת הודעות הצלחה/שגיאה)
                super.display(message);
            }
        }
    }
    
 // בתוך ManagerDashboardController
    public void showGraph(List<Map<String, Object>> reportData) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TimeReportGraph.fxml"));
                Parent root = loader.load();
                
                // השגת הקונטרולר של הגרף
                TimeReportGraphController graphCtrl = loader.getController();
                
                // יצירת סדרת נתונים לגרף
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Minutes of Delay");

                for (Map<String, Object> entry : reportData) {
                    // לוקחים את התאריך והאיחור
                    String date = entry.get("reserved").toString(); // או פורמט יפה יותר
                    int delay = (int) entry.get("delay");
                    
                    series.getData().add(new XYChart.Data<>(date, delay));
                }

                graphCtrl.getBarChart().getData().add(series);
                
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}