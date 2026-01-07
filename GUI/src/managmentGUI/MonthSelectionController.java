package managmentGUI;

import java.io.IOException;
import java.util.ArrayList;

import client.ChatClient;              // <<< זה ה-client האמיתי של OCSF
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

/**
 * Controller for Month Selection screen.
 * Responsibility: choose month and send request to server via OCSF client.
 */
public class MonthSelectionController {

    /** OCSF client reference injected from ManagerDashboardController */
    private ChatClient client;          // ✔ לא Object!

    @FXML
    private ComboBox<String> monthCombo;

    @FXML
    private Button timeRepBtn;

    @FXML
    private Button subRepBtn;

    @FXML
    private Button btnback;

    /** Called by the opening controller to inject the connected client */
    public void setClient(ChatClient client) {
        this.client = client;
    }

    /** Close the current window */
    @FXML
    void closeWin(ActionEvent event) {
        Stage stage = (Stage) btnback.getScene().getWindow();
        stage.close();
    }

    /**
     * Send request to generate Time Report for the selected month.
     */
    @FXML
    void generateTimeReports(ActionEvent event) {

        String selectedMonth = monthCombo.getValue();

        if (selectedMonth == null) {
            showPopup("Error", "Please select month first");
            return;
        }

        ArrayList<Object> message = new ArrayList<>();

        message.add("GET_TIME_REPORTS");
        message.add(1);                 // מזהה מסעדה – כפי שקבענו
        message.add(selectedMonth);     // >>> החודש שהמנהל בחר!

        if (client != null) {
            client.handleMessageFromClientUI(message);
        }
    }

    /**
     * Send request to generate Subscriber Report for the selected month.
     */
    @FXML
    void generateSubscriberReports(ActionEvent event) {
        String selectedMonth = monthCombo.getValue();
        if (selectedMonth == null) {
            // אופציונלי: הצגת הודעה למשתמש לבחור חודש
            return;
        }

        // יצירת ההודעה לשרת
        ArrayList<Object> message = new ArrayList<>();
        message.add("GET_SUBSCRIBER_REPORTS"); // הפקודה שהגדרנו ב-ServerController
        message.add(selectedMonth);

        try {
            client.sendToServer(message); // שליחה לשרת
            // (אופציונלי) סגירת חלון הבחירה לאחר הלחיצה
            // ((Stage)subRepBtn.getScene().getWindow()).close(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Popup helper for not-found or validation messages */
    private void showPopup(String title, String text) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.show();
    }
}
