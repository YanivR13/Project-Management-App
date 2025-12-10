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

/**
 * Controller for the Order Management GUI.
 * Responsible for handling user actions, sending requests to the server,
 * and updating the UI based on server responses.
 */
public class OrderFrameController implements ChatIF, Initializable {

    // --- UI Components injected from FXML ---
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

    /**
     * The client responsible for communication with the server.
     * Set by ClientUI when the application starts.
     */
    private ChatClient client;

    /**
     * Initializes the controller when the FXML screen is loaded.
     * This runs before any interaction occurs.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtResult.setEditable(false);
        txtResult.setText("Welcome! Connect to server to start...\n");
    }

    /**
     * Injects the ChatClient instance created in ClientUI.
     * This allows the controller to send messages to the server.
     *
     * @param client the connected ChatClient
     */
    public void setClient(ChatClient client) {
        this.client = client;
    }

    /**
     * Triggered when the Update button is pressed.
     * Validates input, constructs an update request, and sends it to the server.
     *
     * Expected message format:
     * ["update", id, date, guests]
     */
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

    /**
     * Triggered when the Load Orders button is pressed.
     * Sends a "display" request to the server to fetch all orders.
     */
    @FXML
    void clickLoad(ActionEvent event) {
        if (client != null) {
            client.handleMessageFromClientUI("display");
            display("Sent display request...");
        } else {
            display("Error: Client not connected.");
        }
    }

    /**
     * Displays messages in the TextArea.
     * Uses Platform.runLater with a standard anonymous Runnable.
     */
    @Override
    public void display(Object message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // Case 1: Server sent a list of orders
                if (message instanceof ArrayList) {
                    ArrayList<String> orders = (ArrayList<String>) message;

                    txtResult.appendText("\n=== Orders List (" + orders.size() + ") ===\n");
                    for (String order : orders) {
                        txtResult.appendText(order + "\n");
                    }
                    txtResult.appendText("======================\n");
                }
                // Case 2: Normal text message
                else {
                    txtResult.appendText(message.toString() + "\n");
                }
            }
        });
    }
}
