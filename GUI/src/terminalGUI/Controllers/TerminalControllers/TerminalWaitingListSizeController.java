package terminalGUI.Controllers.TerminalControllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;

import client.ChatClient;
import common.ChatIF;
import common.LoginSource;
import common.ServiceResponse;
import common.ServiceResponse.ServiceStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ocsf.server.ConnectionToClient;

public class TerminalWaitingListSizeController implements ChatIF {

    // Reference to the active terminal client connection
    private ChatClient client;
    
    @FXML
    private TextField txtDiners; // Input field for number of diners
    
    @FXML
    private Label lblError; // Label for displaying validation and server errors

    /**
     * Injects the client and registers this controller as the UI listener.
     */
    public void setClient(ChatClient client) {
        this.client = client;
        if (client != null) {
            client.setUI(this);
        }
    }

    /**
     * Navigates back to the terminal main menu screen.
     */
    @FXML
    private void backToTerminal(ActionEvent event) {
        try {
            // Load the terminal menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/Terminal/TerminalMenuFrame.fxml"));
            Parent root = loader.load();

            // Pass the client reference to the terminal menu controller
            Object controller = loader.getController();
            if (controller instanceof TerminalMenuController) {
                ((TerminalMenuController) controller).setClient(this.client);
            }

            // Replace the current scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load terminal menu");
        }
    }

    /**
     * Validates diner count input and sends a request to join the waiting list.
     */
    @FXML
    private void handleContinue(ActionEvent event) {
        int diners;

        // Input validation: empty field
        if (txtDiners.getText().isEmpty()) {
            showError("Please enter number of diners");
            return;
        }

        // Input validation: numeric value
        try {
            diners = Integer.parseInt(txtDiners.getText());
        } catch (NumberFormatException e) {
            showError("Number of diners must be a number");
            return;
        }

        // Input validation: positive number
        if (diners <= 0) {
            showError("Number of diners must be positive");
            return;
        }

        // Build protocol message
        ArrayList<Object> msg = new ArrayList<>();
        msg.add("JOIN_WAITING_LIST"); // Server command
        msg.add(diners);              // Number of diners

        // Send request to server
        if (client != null) {
            client.handleMessageFromClientUI(msg);
        } else {
            showError("No server connection");
        }
    }
    
    /**
     * Displays a validation or server error message on the screen.
     */
    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    /**
     * Entry point for handling incoming server messages.
     */
    @Override
    public void display(Object message) {
        Platform.runLater(() -> handleServerMessage(message));
    }
    
    /**
     * Handles server responses for waiting list requests.
     * Supports immediate entry, waiting list addition, and error cases.
     */
    @SuppressWarnings("unchecked")
    private void handleServerMessage(Object message) {

        // Ignore unexpected message formats
        if (!(message instanceof ServiceResponse)) {
            return;
        }

        ServiceResponse response = (ServiceResponse) message;

        // Handle logical and system-level errors
        if (response.getStatus() == ServiceStatus.INTERNAL_ERROR) {
            String code = response.getData().toString();

            if ("ALREADY_IN_LIST".equals(code)) {
                showError("You are already on the waiting list.");
            }
            else if ("RESTAURANT_CLOSED".equals(code)) {
                showError("The restaurant is currently closed – you cannot join the waiting list.");
            }
            else {
                showError("Error: " + code);
            }

            return;
        }

        // Ignore non-success responses
        if (response.getStatus() != ServiceStatus.UPDATE_SUCCESS) {
            return;
        }

        Object data = response.getData();

        // Immediate entry scenario
        if (data instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) data;

            if ("IMMEDIATE".equals(map.get("mode"))) {
                long confirmationCode = ((Number) map.get("confirmationCode")).longValue();
                int tableId = ((Number) map.get("tableId")).intValue();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Table Available");
                alert.setHeaderText(null);
                alert.setContentText(
                    "A table is available – you can enter now.\n\n" +
                    "Table number: " + tableId + "\n" +
                    "Confirmation code: " + confirmationCode);
                alert.showAndWait();
                return;
            }
        }

        // Waiting list entry scenario
        String confirmationCode = data.toString();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Waiting List");
        alert.setHeaderText(null);
        alert.setContentText(
            "You have been added to the waiting list.\n\n" +
            "Confirmation code: " + confirmationCode);
        alert.showAndWait();
    }
}
