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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OccasionalLoginController implements ChatIF {

    private ChatClient client;

    @FXML private TextField txtUsername, txtContact;
    @FXML private TextField txtForgotContact, txtNewUsername;
    @FXML private TextArea txtLog;
    @FXML private VBox paneLogin, paneForgot;
    @FXML private Button btnLogin;

    public void setClient(ChatClient client) {
        this.client = client;
        if (client != null) {
            client.setUI(this);
        }
    }

    @FXML
    void clickLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String contact = txtContact.getText();

        if (username.isEmpty() || contact.isEmpty()) {
            appendLog("Error: Username and Contact are required.");
            return;
        }

        ArrayList<String> message = new ArrayList<>();
        message.add("LOGIN_OCCASIONAL");
        message.add(username);
        message.add(contact);

        if (client != null) {
            appendLog("Verifying guest details for: " + username);
            client.handleMessageFromClientUI(message);
        }
    }

    @FXML
    void showForgotArea(ActionEvent event) {
        paneLogin.setVisible(false);
        paneLogin.setManaged(false);
        paneForgot.setVisible(true);
        paneForgot.setManaged(true);
    }

    @FXML
    void hideForgotArea(ActionEvent event) {
        paneForgot.setVisible(false);
        paneForgot.setManaged(false);
        paneLogin.setVisible(true);
        paneLogin.setManaged(true);
    }

    @FXML
    void clickSubmitForgot(ActionEvent event) {
        String oldContact = txtForgotContact.getText();
        String newUsername = txtNewUsername.getText();

        if (oldContact.isEmpty() || newUsername.isEmpty()) {
            appendLog("Error: Both fields are required to reset username.");
            return;
        }

        ArrayList<String> message = new ArrayList<>();
        message.add("RESET_OCCASIONAL_USERNAME");
        message.add(oldContact);
        message.add(newUsername);

        if (client != null) {
            appendLog("Requesting username reset for contact: " + oldContact);
            client.handleMessageFromClientUI(message);
        }
    }

    @Override
    public void display(Object message) {
        if (message != null) {
            String response = message.toString();
            appendLog(response);

            if (response.equals("LOGIN_OCCASIONAL_SUCCESS")) {
                Platform.runLater(() -> navigateToMenu());
            }
            
            if (response.equals("RESET_USERNAME_SUCCESS")) {
                Platform.runLater(() -> {
                    appendLog("Success! You can now log in with your new username.");
                    hideForgotArea(null);
                });
            }
        }
    }

    private void navigateToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml"));
            Parent root = loader.load();
            
            OccasionalMenuController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root);
            // תיקון נתיב CSS
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            stage.setTitle("Guest Dashboard");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            appendLog("Navigation Error: " + e.getMessage());
        }
    }

    @FXML
    void clickRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/OccasionalFXML/OccasionalRegistrationFrame.fxml"));
            Parent root = loader.load();
            OccasionalRegistrationController controller = loader.getController();
            controller.setClient(client);
            switchScene(event, root, "Register New Guest");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clickBack(ActionEvent event) {
        navigateToPortal(event);
    }

    private void navigateToPortal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml"));
            Parent root = loader.load();
            RemoteLoginController controller = loader.getController();
            controller.setClient(client);
            switchScene(event, root, "Bistro - Remote Access Portal");
        } catch (Exception e) {
            e.printStackTrace();
            appendLog("Navigation Error: " + e.getMessage());
        }
    }

    private void switchScene(ActionEvent event, Parent root, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        
        try {
            // תיקון נתיב CSS
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS not found: " + e.getMessage());
        }
        
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}