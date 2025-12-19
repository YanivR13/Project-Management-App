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

    public void setClient(ChatClient client) {
        this.client = client;
    }

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
            
            // Temporary for UI Testing
            navigateToMenu(event);
        }
    }

    @FXML
    void clickBack(ActionEvent event) {
        navigateToPortal(event);
    }

    private void navigateToMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml"));
            Parent root = loader.load();
            SubscriberMenuController controller = loader.getController();
            controller.setClient(client);
            switchScene(event, root, "Subscriber Dashboard");
        } catch (Exception e) {
            appendLog("Navigation Error: " + e.getMessage());
        }
    }

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

    private void switchScene(ActionEvent event, Parent root, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/style.css").toExternalForm());
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void display(String message) {
        appendLog(message);
    }

    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}