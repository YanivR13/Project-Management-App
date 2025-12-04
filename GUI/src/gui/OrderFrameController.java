package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import client.ChatClient;
import common.ChatIF;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class OrderFrameController implements ChatIF, Initializable {

    // --- חיבור לרכיבים ב-SceneBuilder ---
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
        txtResult.setText("Welcome! Connect to server to start...\n");
        
        // --- ניסיון חיבור אוטומטי לשרת ---
        try {
            // יוצר חיבור לשרת (מקומי, פורט 5555), ומעביר את 'this' כמי שיקבל את התשובות
            this.client = new ChatClient("localhost", 5555, this);
            display("Client connected successfully!");
        } catch (Exception e) {
            display("Error: Could not connect to server. Make sure Server is running.");
        }
    }

    // --- פונקציה לחיבור הלוגיקה (במידה ורוצים לחבר ידנית מבחוץ) ---
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

        // יצירת הרשימה לשליחה
        ArrayList<String> msg = new ArrayList<>();
        msg.add("update"); // פקודה לשרת
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
            client.handleMessageFromClientUI("display");
            display("Sent display request...");
        } else {
            display("Error: Client not connected.");
        }
    }

    // --- מימוש ממשק ChatIF: הצגת רשימת הזמנות ---
    @Override
    public void displayOrders(ArrayList<ArrayList<String>> orders) {
        StringBuilder sb = new StringBuilder();
        
        // כותרת כללית
        sb.append("=== Orders List (" + orders.size() + ") ===\n\n");

        for (ArrayList<String> row : orders) {
            // פירוק הנתונים לפי האינדקסים (בהנחה שזה הסדר ב-DB)
            String orderId = row.get(0);
            String orderDate = row.get(1);
            String guests = row.get(2);
            String confirmCode = row.get(3);
            String subId = row.get(4);
            String createdDate = row.get(5);

            // בניית תצוגה יפה
            sb.append("Order ID:       ").append(orderId).append("\n");
            sb.append("Date:           ").append(orderDate).append("\n");
            sb.append("Num of Guests:  ").append(guests).append("\n");
            sb.append("Subscriber ID:  ").append(subId).append("\n");
            sb.append("Confirm Code:   ").append(confirmCode).append("\n");
            sb.append("Created On:     ").append(createdDate).append("\n");
            sb.append("--------------------------------------\n");
        }
        
        // עדכון המסך בתוך התהליך הגרפי
        Platform.runLater(() -> {
            txtResult.setText(sb.toString()); 
        });
    }

    // --- מימוש ממשק ChatIF: הצגת הודעות כלליות ---
    @Override
    public void display(String message) {
        Platform.runLater(() -> {
             // appendText מוסיף לסוף הטקסט הקיים (טוב ללוגים)
             txtResult.appendText(message + "\n");
        });
    }
}