package clientGUI.Controllers.SubscriberControlls;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.ArrayList;


import client.ChatClient;
import common.ChatIF;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

public class EditSubscriberDetailsController implements ChatIF {
    private ChatClient client;
    private int userId;
    
    // ===== Text Fields =====
    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtEmail;

    // ===== Buttons =====
    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    public void setClient(ChatClient client) {
        this.client = client;
        client.setUI(this);
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @FXML
    private void clickCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void clickSave(ActionEvent event) {

        if (client == null) {
            System.out.println("Error: client is null");
            return;
        }

        String username = txtUsername.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        // אם הכל ריק – אין מה לעדכן
        if (username.isEmpty() && phone.isEmpty() && email.isEmpty()) {
            showAlert(
                "No Changes",
                "Please enter at least one field to update.",
                Alert.AlertType.INFORMATION
            );
            return;
        }

        ArrayList<Object> msg = new ArrayList<>();
        msg.add("UPDATE_SUBSCRIBER_DETAILS");
        msg.add(userId);

        // שדות ריקים נשלחים כ-null
        msg.add(username.isEmpty() ? null : username);
        msg.add(phone.isEmpty() ? null : phone);
        msg.add(email.isEmpty() ? null : email);

        client.handleMessageFromClientUI(msg);
    }

    @Override
    public void display(Object message) {

        Platform.runLater(() -> {

            if (message instanceof ArrayList<?>) {
                ArrayList<?> data = (ArrayList<?>) message;

                String command = data.get(0).toString();

                if ("EDIT_DETAILS_RESULT".equals(command)) {

                    String result = data.get(1).toString();

                    if ("SUCCESS".equals(result)) {
                        showAlert(
                            "Profile Updated",
                            "Your personal details were updated successfully.",
                            Alert.AlertType.INFORMATION
                        );
                        closeWindow();
                    }
                    else if ("NO_CHANGES".equals(result)) {
                        showAlert(
                            "No Changes",
                            "No details were updated.",
                            Alert.AlertType.INFORMATION
                        );
                    }
                }
            }
            else if (message instanceof String &&
                     "ERROR_EDITING_DETAILS".equals(message)) {

                showAlert(
                    "Error",
                    "An error occurred while updating your details.",
                    Alert.AlertType.ERROR
                );
            }
        });
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

}
