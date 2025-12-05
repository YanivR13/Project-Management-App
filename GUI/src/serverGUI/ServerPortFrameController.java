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
        // מניעת יצירת שרת כפול
        if (server != null) {
            appendLog("Server is already running!");
            return;
        }

        appendLog("Attempting to start server...");

        // יצירת השרת והעברת ה-GUI
        server = new ServerController(5555, this);

        try {
            // הפעלת השרת - מפעיל thread פנימי של OCSF
            server.listen();
            appendLog("Server started listening on port 5555");

            // *** אין חיבור ל-DB כאן! ***
            // החיבור יתבצע אוטומטית ב-serverStarted()

        } catch (Exception e) {
            appendLog("Error starting server: " + e.getMessage());
        }
    }

    @FXML
    public void clickExit(ActionEvent event) {
        appendLog("Exiting...");

        // אם השרת פעיל - נסגור אותו בצורה מסודרת
        if (server != null) {
            try {
                appendLog("Stopping server...");
                
                // קודם מפסיקים האזנה
                server.stopListening();
                
                // סוגרים את השרת (יקרא אוטומטית serverStopped())
                server.close();

            } catch (Exception e) {
                appendLog("Error while closing server: " + e.getMessage());
            }
        }

        // יציאה מהתוכנית
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