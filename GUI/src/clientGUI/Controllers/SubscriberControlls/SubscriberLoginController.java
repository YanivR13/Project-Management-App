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

public class SubscriberLoginController implements ChatIF {

    private ChatClient client;

    @FXML private TextField txtSubscriberID;
    @FXML private Button btnLogin;
    @FXML private Button btnBack;
    @FXML private TextArea txtLog;

    public void setClient(ChatClient client) {
        this.client = client;
        if (client != null) {
            client.setUI(this); 
        }
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
            appendLog("Attempting verification...");
            client.handleMessageFromClientUI(message);
        }
    }

    @FXML
    void clickBack(ActionEvent event) {
        navigateToPortal(event);
    }

    @Override
    public void display(Object message) {
        if (message != null) {
            String response = message.toString();
            appendLog(response);
            if (response.equals("LOGIN_SUCCESS")) {
                Platform.runLater(() -> navigateToMenu());
            }
        }
    }

    private void navigateToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml"));
            Parent root = loader.load();
            SubscriberMenuController controller = loader.getController();
            controller.setClient(client);
            
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root);
            // תיקון נתיב CSS
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            
            stage.setTitle("Subscriber Dashboard");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            appendLog("Navigation Error: " + e.getMessage());
            e.printStackTrace();
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
            appendLog("Error: " + e.getMessage());
        }
    }

    private void switchScene(ActionEvent event, Parent root, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        // תיקון נתיב CSS
        scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}