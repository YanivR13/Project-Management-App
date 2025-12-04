package clientGUI;

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
        txtResult.setEditable(false); 
        txtResult.setText("Welcome! Connect to server to start...\n");
        
        // --- ניסיון חיבור אוטומטי לשרת ---
        try {
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

        if (id.isEmpty() || date.isEmpty() || guests.isEmpty()) {
            display("Error: Please fill all fields (ID, Date, Guests).");
            return;
        }

        ArrayList<String> msg = new ArrayList<>();
        msg.add("update"); 
        msg.add(id);
        msg.add(date);
        msg.add(guests);

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
    
 // --- מימוש ממשק ChatIF המעודכן ---
    @Override
    public void display(Object message) {
        Platform.runLater(() -> {
            // בדיקה 1: האם קיבלנו רשימה של הזמנות מהשרת?
            if (message instanceof ArrayList) {
                ArrayList<String> orders = (ArrayList<String>) message;
                txtResult.appendText("\n=== Orders List (" + orders.size() + ") ===\n");
                
                // השרת כבר שולח מחרוזות מעוצבות, אז פשוט נדפיס אותן
                for (String order : orders) {
                    txtResult.appendText(order + "\n");
                }
                txtResult.appendText("======================\n");
            }
            // בדיקה 2: האם זו סתם הודעת טקסט רגילה?
            else {
                txtResult.appendText(message.toString() + "\n");
            }
        });
    }


}