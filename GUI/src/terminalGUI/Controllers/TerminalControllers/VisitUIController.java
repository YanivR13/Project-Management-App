package terminalGUI.Controllers.TerminalControllers;

import java.util.ArrayList;

import client.ChatClient;
import common.ChatIF;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Arrival Terminal screen.
 * This class handles the logic for entering a confirmation code and 
 * processing the check-in result from the server.
 */
public class VisitUIController implements ChatIF {
	
    private ChatClient client;

    // FXML injected UI components
    @FXML private TextField txtCode; // Field for entering confirmation code
    @FXML private Button btnVerify; // Button to start arrival process
    @FXML private Button btnBack; // Back to main terminal menu
    @FXML private Label lblStatus; // For displaying status messages
    
    /**
     * Injects the shared ChatClient instance into the controller.
     *
     * @param client Active network client
     */
    public void setClient(ChatClient client) {
        this.client = client;
    }


    /**
     * Handles the click on the "I'm Here" verify button.
     */
    @FXML
    void onVerifyClicked(ActionEvent event) {
        String codeStr = txtCode.getText().trim();

        // 1. Validation: check if empty
        if (codeStr.isEmpty()) {
            showAlert("Warning", "Please enter your confirmation code.", AlertType.WARNING);
            return;
        }

        try {
            // 2. Format validation: parse to long
            long code = Long.parseLong(codeStr);
            
            // 3. Prepare the message for the server [Command, Data]
            ArrayList<Object> message = new ArrayList<>();
            message.add("PROCESS_TERMINAL_ARRIVAL");
            message.add(code);
            
            // 4. Send request to server
            client.handleMessageFromClientUI(message);
            lblStatus.setText("Verifying arrival...");

        } catch (NumberFormatException e) {
            showAlert("Error", "Code must be numeric.", AlertType.ERROR);
        }
    }

    /**
     * Callback method called by ChatClient when the server responds.
     */
    @Override
    public void display(Object message) {
        if (message instanceof String) {
            String response = (String) message;
            
            // Ensure UI updates happen on the JavaFX Application Thread
            Platform.runLater(() -> {
                handleServerResponse(response);
            });
        }
    }

    /**
     * Internal logic to process the string response from the Server.
     */
    private void handleServerResponse(String response) {
        lblStatus.setText(""); // Clear waiting message

        if (response.equals("INVALID_CODE")) {
            showAlert("Arrival Failed", "Invalid confirmation code or booking not confirmed.", AlertType.ERROR);
        } 
        else if (response.equals("TOO_EARLY")) {
            showAlert("Too Early", "You arrived more than 60 minutes early. Please come back later.", AlertType.INFORMATION);
        } 
        else if (response.equals("TABLE_NOT_READY_WAIT")) {
            showAlert("Welcome", "Your table is not ready yet. Please wait, we will notify you via SMS.", AlertType.INFORMATION);
            // Optional: returnToMainMenu();
        } 
        else if (response.startsWith("SUCCESS_TABLE_")) {
            String tableId = response.split("_")[2]; // Extract table number from the string
            showAlert("Welcome!", "Your table is ready! Please proceed to Table #" + tableId, AlertType.CONFIRMATION);
            // Optional: Transition to a "Enjoy your meal" screen
        } 
        else if (response.equals("DATABASE_ERROR")) {
            showAlert("System Error", "Communication with database failed. Please see the host.", AlertType.ERROR);
        }
    }

    /**
     * Helper for standardized alerts.
     */
    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void onBackClicked(ActionEvent event) {
    	
    	try {
            // Load the Terminal Menu screen [cite: 2]
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/Terminal/TerminalMenuFrame.fxml"));
            Parent root = loader.load();

            // Inject ChatClient into the menu controller
            TerminalMenuController controller = loader.getController();
            controller.setClient(client);

            // Switch scene
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Bistro - Service Terminal");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load menu screen.", AlertType.ERROR);
        }

    }
    
    /**
     * Called when the client connection is ready. 
     * Registers this controller to receive server messages.
     */
    public void onClientReady() {
        if (client != null) {
            client.setUI(this); // Register this controller as the active UI
        }
    }
    
}