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

/**
 * Controller class for the Occasional (Guest) Customer Registration interface.
 * This class serves as the logic layer for creating new guest profiles, implementing 
 * client-side input validation before transmitting data to the Bistro Server.
 * * <p>It implements {@link ChatIF} to allow the {@link client.ChatClient} to 
 * push server responses (success or error) back to this UI component.</p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public class OccasionalRegistrationController implements ChatIF {
    
    /** The network communication client for OCSF. */
    private ChatClient client;

    /** Input fields for user credentials and contact information. */
    @FXML private TextField txtNewUser, txtNewContact;
    
    /** UI Console for logging status updates and validation feedback. */
    @FXML private TextArea txtLog;

    /**
     * Injects the persistent ChatClient into this controller and registers
     * this class as the active UI for handling server messages.
     * * @param client The active network client instance.
     */
    public void setClient(ChatClient client) {
        this.client = client;
        if (client != null) {
            // Register this controller as the message handler for server responses
            client.setUI(this);
            appendLog("Ready for new guest registration.");
        }
    }

    /**
     * Event handler for the 'Create Account' action.
     * Orchestrates three layers of client-side validation:
     * 1. Existence check (Fields must not be blank).
     * 2. Business constraints (Username length <= 10).
     * 3. Formatting/Type detection (Phone vs. Email based on the first character).
     * * @param event The ActionEvent triggered by the UI button.
     */
    @FXML
    void clickSubmitRegistration(ActionEvent event) {
        // Retrieve and trim inputs to remove accidental leading/trailing whitespaces
        String user = txtNewUser.getText().trim();
        String contact = txtNewContact.getText().trim();

        // Layer 1: Mandatory Field Check
        if (user.isEmpty() || contact.isEmpty()) {
            appendLog("Error: All fields are required.");
            return;
        }

        // Layer 2: Business Constraint - Username fits the Database VARCHAR(10) limit
        if (user.length() > 10) {
            appendLog("Error: Username must be 10 characters or less.");
            return;
        }

        // --- LAYER 3: TYPE IDENTIFICATION & FORMAT VALIDATION ---
        
        // Identify the contact type by inspecting the first character
        char firstChar = contact.charAt(0);

        if (Character.isDigit(firstChar)) {
            /** * Scenario A: Input starts with a digit. System expects a PHONE NUMBER.
             * Logic: Must be exactly 10 characters AND contain only digits (Regex: \d+).
             */
            if (contact.length() != 10 || !contact.matches("\\d+")) {
                appendLog("Error: You started with a number. Phone must be exactly 10 digits.");
                return;
            }
        } 
        else {
            /** * Scenario B: Input starts with a letter/symbol. System expects an EMAIL.
             * Logic: Must contain the '@' symbol to be considered a valid identifier.
             */
            if (!contact.contains("@")) {
                appendLog("Error: You started with a letter. Email must contain '@'.");
                return;
            }
        }
        
        // --- END OF VALIDATION ---

        // Encapsulate validated data into the protocol format (Command + Payload)
        ArrayList<String> message = new ArrayList<>();
        message.add("REGISTER_OCCASIONAL"); // Server command
        message.add(user);                  // New username
        message.add(contact);               // Phone or Email identifier

        // Transmit to server via the OCSF AbstractClient
        if (client != null) {
            appendLog("Sending registration request for: " + user);
            client.handleMessageFromClientUI(message);
        }
    }

    /**
     * HOOK METHOD (ChatIF): Receives and processes server responses.
     * Uses Platform.runLater to ensure UI components are updated on the JavaFX Application Thread.
     * * @param message The data object (typically a String) received from the server.
     */
    @Override
    public void display(Object message) {
        Platform.runLater(() -> {
            if (message != null) {
                String response = message.toString();
                appendLog("Server Response: " + response);

                // Handle successful database transaction (Insertion into 'user' and 'occasional_customer' tables)
                if (response.equals("REGISTRATION_SUCCESS")) {
                    appendLog("SUCCESS: Account created! You can now go back and login.");
                    
                    // Reset UI fields for potential subsequent registrations
                    txtNewUser.clear();
                    txtNewContact.clear();
                }
            }
        });
    }

    /**
     * Navigates back to the Guest Login portal.
     * Re-injects the ChatClient into the target controller to maintain the socket connection.
     * * @param event The ActionEvent from the 'Back' button.
     */
    @FXML
    void clickBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/OccasionalFXML/OccasionalLoginFrame.fxml"));
            Parent root = loader.load();
            
            // Pass the persistent client reference back to the Login controller
            ((OccasionalLoginController)loader.getController()).setClient(client);

            // Configure the scene transition on the existing window
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            
            // Re-apply global CSS styling for visual consistency
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            appendLog("Navigation Error: " + e.getMessage());
        }
    }

    /**
     * Appends a message to the UI Logger in a thread-safe manner.
     * @param message The text string to display in the log.
     */
    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}