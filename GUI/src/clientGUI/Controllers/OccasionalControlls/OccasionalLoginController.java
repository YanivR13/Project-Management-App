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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Occasional (Guest) login screen.
 * Handles temporary identification and registration navigation.
 */
public class OccasionalLoginController implements ChatIF {

    private ChatClient client;

    @FXML private TextField txtUsername, txtContact;
    @FXML private TextArea txtLog;

    public void setClient(ChatClient client) {
        this.client = client;
    }

    @FXML
    void clickLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String contact = txtContact.getText();

        if (username.isEmpty() || contact.isEmpty()) {
            appendLog("Error: All fields are mandatory.");
            return;
        }

        ArrayList<String> message = new ArrayList<>();
        message.add("LOGIN_OCCASIONAL");
        message.add(username);
        message.add(contact);

        if (client != null) {
            appendLog("Verifying guest details for: " + username);
            client.handleMessageFromClientUI(message);
            // Temporary navigation for UI testing
            navigateToMenu(event);
        }
    }

    @FXML
    void clickRegister(ActionEvent event) {
        appendLog("Navigating to Registration Module...");
        // Placeholder for future Registration screen
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
            switchScene(event, root, "Guest Dashboard");
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
        if (message != null) {
            appendLog(message.toString());
        }
    }

    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}