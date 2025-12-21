package clientGUI.Controllers.OccasionalControlls;

import java.util.ArrayList;
import client.ChatClient;
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

public class OccasionalRegistrationController implements ChatIF {

    private ChatClient client;

    @FXML private TextField txtNewUser, txtNewContact;
    @FXML private TextArea txtLog;

    public void setClient(ChatClient client) {
        this.client = client;
        if (client != null) client.setUI(this);
    }

    @FXML
    void clickSubmitRegistration(ActionEvent event) {
        String user = txtNewUser.getText();
        String contact = txtNewContact.getText();

        if (user.isEmpty() || contact.isEmpty()) {
            appendLog("Error: Both fields are required for registration.");
            return;
        }

        ArrayList<String> message = new ArrayList<>();
        message.add("REGISTER_OCCASIONAL");
        message.add(user);
        message.add(contact);

        if (client != null) {
            appendLog("Sending registration request for: " + user);
            client.handleMessageFromClientUI(message);
        }
    }

    @Override
    public void display(Object message) {
        if (message != null) {
            String response = message.toString();
            appendLog(response);

            if (response.equals("REGISTRATION_SUCCESS")) {
                Platform.runLater(() -> appendLog("Account created! You can now go back and login."));
            }
        }
    }

    @FXML
    void clickBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/OccasionalFXML/OccasionalLoginFrame.fxml"));
            Parent root = loader.load();
            OccasionalLoginController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            stage.setTitle("Occasional Customer Login");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}