package client;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import common.ChatIF;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class OrderFrameController implements ChatIF, Initializable {

    // --- חיבור לרכיבים ב-SceneBuilder (לפי ה-fx:id שנתת) ---
    @FXML
    private TextField txtId;

    @FXML
    private TextField txtDate;

    @FXML
    private TextField txtGuests;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnLoad;

    @FXML
    private TextArea txtResult;
    
    // משתנה להחזקת המנוע הלוגי
    private ChatClient client;

    // --- מתודה לאתחול ראשוני (רצה כשהמסך עולה) ---
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtResult.setEditable(false); // שלא יוכלו להקליד בתוצאות
        txtResult.setText("Welcome! Connect to server to start...");
    }

    // --- פונקציה לחיבור הלוגיקה (נקראת מה-Main) ---
    public void setClient(ChatClient client) {
        this.client = client;
    }

    // --- לחיצה על כפתור Update ---
    @FXML
    void clickUpdate(ActionEvent event) {
        String id = txtId.getText();
        String date = txtDate.getText();
        String guests = txtGuests.getText();

        // בדיקה שכל השדות מלאים
        if (id.isEmpty() || date.isEmpty() || guests.isEmpty()) {
            display("Error: Please fill all fields (ID, Date, Guests).");
            return;
        }

        // יצירת הרשימה לשליחה (כמו שעשינו בקונסול)
        // סדר האיברים חשוב - חייב להיות מתואם עם מה שהשרת מצפה!
        ArrayList<String> msg = new ArrayList<>();
        msg.add("update"); // מילת מפתח לשרת (אם הוא תומך בזה)
        msg.add(id);
        msg.add(date);
        msg.add(guests);

        // שליחה לשרת
        if (client != null) {
            client.handleMessageFromClientUI(msg);
            display("Sent update request for Order ID: " + id);
        } else {
            display("Error: Client not connected.");
        }
    }

    // --- לחיצה על כפתור Load Orders ---
    @FXML
    void clickLoad(ActionEvent event) {
        if (client != null) {
            // שליחת פקודת "display" (כמו שכתבנו בקונסול)
            client.handleMessageFromClientUI("display");
            display("Sent display request...");
        } else {
            display("Error: Client not connected.");
        }
    }

    // --- מימוש החוזה ChatIF (הצגת הודעות מהשרת) ---
    @Override
    public void display(String message) {
        // ב-JavaFX אסור לגעת במסך מתוך תהליך (Thread) אחר (כמו זה של התקשורת).
        // הפקודה Platform.runLater דואגת שזה יקרה בצורה בטוחה.
        Platform.runLater(() -> {
            txtResult.appendText("\n" + message);
        });
    }
}