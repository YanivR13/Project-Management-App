package serverGUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.application.Platform; 

import server.ServerController;
import common.ServerIF;

public class ServerPortFrameController implements ServerIF {

    @FXML
    private Button btnStart;

    @FXML
    private Button btnExit;

    @FXML
    private TextArea txtLog;

    private ServerController server;

    @FXML
    public void clickStart(ActionEvent event) {
        // מונע יצירה כפולה של השרת
        if (server != null) {
            appendLog("Server is already running!");
            return;
        }

        appendLog("Attempting to start server...");

        // יצירת השרת והעברת 'this' (המחלקה הזו) כדי שיוכל להדפיס לוגים
        server = new ServerController(5555, this);
        
        // חיבור למסד הנתונים
        // הערה: ייתכן שהמסך יקפא לחצי שנייה בזמן החיבור - זה תקין
        server.connectToDB();
        
        try {
            // הפקודה הזו של OCSF מריצה את השרת ברקע לבד
            server.listen(); 
            appendLog("Server started listening on port 5555");
            
        } catch (Exception e) {
            appendLog("Error starting server: " + e.getMessage());
        }
    }

    @FXML
    public void clickExit(ActionEvent event) {
        appendLog("Exiting...");
        
        // אם השרת רץ, נסגור אותו לפני היציאה
        if(server != null) {
            try {
                server.close();
            } catch(Exception e) {
                // לא קריטי ביציאה
            }
        }
        System.exit(0);
    }

    // מימוש הממשק ServerIF
    @Override
    public void appendLog(String msg) {
        // שימוש ב-Platform.runLater הוא חובה ב-JavaFX כשמישהו אחר (השרת) קורא לפונקציה
        // זה מבטיח שהכתיבה למסך תתבצע בצורה תקינה
        Platform.runLater(() -> {
            txtLog.appendText(msg + "\n");
        });
    }
}