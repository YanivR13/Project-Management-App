package clientGUI.Controllers.SubscriberControlls;

import java.util.ArrayList;
import client.ChatClient;
import clientGUI.Controllers.RemoteLoginController;
import common.ChatIF;
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
import javafx.application.Platform;

/**
 * Controller for the Subscriber login screen.
 * Handles identification via unique Subscriber ID.
 * Implements ChatIF to handle responses from the server.
 */
public class SubscriberLoginController implements ChatIF {

    private ChatClient client;

    @FXML
    private TextField txtSubscriberID;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnBack;

    @FXML
    private TextArea txtLog;

    /**
     * Injects the client instance into this controller.
     * @param client The ChatClient instance used for communication.
     */
    public void setClient(ChatClient client) {
        this.client = client;
    }

    /**
     * Triggered when the user clicks the Login button.
     * Validates input and sends a login request to the server.
     */
    @FXML
    void clickLogin(ActionEvent event) {
        String subID = txtSubscriberID.getText();

        if (subID.isEmpty()) {
            appendLog("Error: Subscriber ID is required.");
            return;
        }

        ArrayList<String> message = new ArrayList<>();
        message.add("LOGIN_SUBSCRIBER");
        message.add(subID);

        if (client != null) {
            appendLog("Attempting to verify Subscriber ID: " + subID);
            client.handleMessageFromClientUI(message);
            
            // Temporary navigation for UI testing until server validation logic is finalized
            navigateToMenu(event);
        }
    }

    /**
     * Navigates back to the Remote Access Portal.
     */
    @FXML
    void clickBack(ActionEvent event) {
        navigateToPortal(event);
    }

    /**
     * Loads the Subscriber Menu (Dashboard).
     */
    private void navigateToMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml"));
            Parent root = loader.load();
            
            // Get the controller and inject the client
            SubscriberMenuController controller = loader.getController();
            controller.setClient(client);
            
            switchScene(event, root, "Subscriber Dashboard");
        } catch (Exception e) {
            appendLog("Navigation Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads the initial Remote Access Portal screen.
     */
    private void navigateToPortal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml"));
            Parent root = loader.load();
            
            RemoteLoginController portalCtrl = loader.getController();
            portalCtrl.setClient(client);
            
            switchScene(event, root, "Bistro - Remote Access Portal");
        } catch (Exception e) {
            appendLog("Navigation Error: " + e.getMessage());
        }
    }

    /**
     * Helper method to handle the physical stage/scene switching.
     */
    private void switchScene(ActionEvent event, Parent root, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/style.css").toExternalForm());
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Implementation of the ChatIF interface.
     * Updated to handle Object instead of String to match the interface contract.
     * @param message The message object received from the server.
     */
    @Override
    public void display(Object message) {
        if (message != null) {
            appendLog(message.toString());
        }
    }

    /**
     * Appends text to the on-screen log area within the JavaFX thread.
     * @param message The text to display in the log.
     */
    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}