package clientGUI.Controllers.OccasionalControlls;

import java.util.ArrayList;
import client.ChatClient;
import clientGUI.Controllers.RemoteLoginController;
import common.ChatIF;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Boundary class for occasional customers (guests).
 * Handles both login and registration requests by sending data to the Client (Control).
 */
public class OccasionalLoginController implements ChatIF {

    private ChatClient client;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtContact;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnRegister;

    @FXML
    private Button btnBack;

    @FXML
    private TextArea txtLog;

    /**
     * Injects the communication client into this controller.
     * @param client The active ChatClient instance.
     */
    public void setClient(ChatClient client) {
        this.client = client;
    }

    /**
     * Sends a login request to the server using username and contact info.
     * Format: ["LOGIN_OCCASIONAL", username, contact]
     */
    @FXML
    void clickLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String contact = txtContact.getText();

        if (username.isEmpty() || contact.isEmpty()) {
            appendLog("Error: Username and Contact info (Phone/Email) are required.");
            return;
        }

        ArrayList<String> message = new ArrayList<>();
        message.add("LOGIN_OCCASIONAL");
        message.add(username);
        message.add(contact);

        if (client != null) {
            appendLog("Sending login request for user: " + username);
            client.handleMessageFromClientUI(message);
            
            // Temporary for UI Testing: Transition to menu
            // In final version, this happens after server confirmation
            navigateToMenu(event);
        } else {
            appendLog("System Error: Client connection not initialized.");
        }
    }

    /**
     * Sends a registration request for a new occasional customer.
     */
    @FXML
    void clickRegister(ActionEvent event) {
        String username = txtUsername.getText();
        String contact = txtContact.getText();

        if (username.isEmpty() || contact.isEmpty()) {
            appendLog("Error: Username and Contact info are required for registration.");
            return;
        }

        ArrayList<String> message = new ArrayList<>();
        message.add("REGISTER_OCCASIONAL");
        message.add(username);
        message.add(contact);

        if (client != null) {
            appendLog("Sending registration request for: " + username);
            client.handleMessageFromClientUI(message);
        }
    }

    @FXML
    void clickBack(ActionEvent event) {
        navigateToPortal(event);
    }

    private void navigateToMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/OccasinalFXML/OccasionalMenuFrame.fxml"));
            Parent root = loader.load();
            OccasionalMenuController controller = loader.getController();
            controller.setClient(client);
            switchScene(event, root, "Guest Menu");
        } catch (Exception e) {
            appendLog("Navigation Error: " + e.getMessage());
        }
    }

    private void navigateToPortal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml"));
            Parent root = loader.load();
            RemoteLoginController controller = loader.getController();
            controller.setClient(client);
            switchScene(event, root, "Bistro - Remote Access Portal");
        } catch (Exception e) {
            appendLog("Navigation Error: " + e.getMessage());
        }
    }

    private void switchScene(ActionEvent event, Parent root, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/style.css").toExternalForm());
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void display(Object message) {
        appendLog(message);
    }

    public void appendLog(Object message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}