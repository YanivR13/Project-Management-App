package clientGUI.Controllers.SubscriberControlls; // Defining the package for subscriber controllers

import javafx.scene.control.Alert; // Importing Alert class for UI popups
import javafx.scene.control.Button; // Importing Button component
import javafx.scene.control.TextField; // Importing TextField component
import java.util.ArrayList; // Importing ArrayList for dynamic message lists
import client.ChatClient; // Importing the ChatClient for communication
import common.ChatIF; // Importing the interface for server responses
import javafx.fxml.FXML; // Importing FXML annotation for UI injection
import javafx.application.Platform; // Importing Platform for UI thread management
import javafx.event.ActionEvent; // Importing ActionEvent for button triggers
import javafx.scene.Node; // Importing Node for UI hierarchy access
import javafx.stage.Stage; // Importing Stage for window management

/**
 * Controller class for editing subscriber profile details.
 * It manages the input validation and updates the subscriber's information in the DB.
 */
public class EditSubscriberDetailsController implements ChatIF { // Class start implementing ChatIF

    // Reference to the active network client
    private ChatClient client;
    
    // Unique identifier for the logged-in user
    private int userId;
    
    // --- FXML UI Fields ---
    
    @FXML // Injection of the username input field
    private TextField txtUsername;

    @FXML // Injection of the phone number input field
    private TextField txtPhone;

    @FXML // Injection of the email input field
    private TextField txtEmail;

    @FXML // Injection of the save button
    private Button btnSave;

    @FXML // Injection of the cancel button
    private Button btnCancel;

    // --- Configuration Methods ---

    /**
     * Injects the client and registers this controller as the UI listener.
     */
    public void setClient(ChatClient client) { // Start setClient method
        this.client = client; // Assigning the client instance
        client.setUI(this); // Setting this class as the receiver for server messages
    } // End setClient method

    /**
     * Sets the user ID for identifying which subscriber to update.
     */
    public void setUserId(int userId) { // Start setUserId method
        this.userId = userId; // Assigning the user ID value
    } // End setUserId method
    
    // --- UI Action Handlers ---

    /**
     * Closes the current edit window without saving.
     */
    @FXML // Link to FXML action
    private void clickCancel(ActionEvent event) { // Start clickCancel method
        // Get the current window stage from the event source
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        // Close the stage immediately
        stage.close(); 
    } // End clickCancel method
    
    /**
     * Validates input fields and sends an update request to the server.
     */
    @FXML // Link to FXML action
    private void clickSave(ActionEvent event) { // Start clickSave method

        // Ensure the client connection is active before proceeding
        if (client == null) { // Check if client is null
            System.out.println("Error: client is null"); // Log technical error
            return; // Terminate execution
        } // End null check

        // Retrieve and trim values from all text fields
        String username = txtUsername.getText().trim(); // Get username text
        String phone = txtPhone.getText().trim(); // Get phone text
        String email = txtEmail.getText().trim(); // Get email text

        // Guard Clause: If all fields are empty, no update is necessary
        boolean isAllEmpty = (username.isEmpty() && phone.isEmpty() && email.isEmpty()); // Logic check
        
        if (isAllEmpty) { // If nothing was entered
            // Prompt the user to fill at least one field
            showAlert("No Changes", "Please enter at least one field to update.", Alert.AlertType.INFORMATION); 
            return; // Terminate execution
        } // End validation

        // Construct the message list for the server protocol
        ArrayList<Object> msg = new ArrayList<>(); // Initialize message list
        msg.add("UPDATE_SUBSCRIBER_DETAILS"); // Command header
        msg.add(userId); // Target user ID

        // Logic: Send null if a field is empty, otherwise send the trimmed string
        msg.add(username.isEmpty() ? null : username); // Add username or null
        msg.add(phone.isEmpty() ? null : phone); // Add phone or null
        msg.add(email.isEmpty() ? null : email); // Add email or null

        // Transmit the update request to the server
        client.handleMessageFromClientUI(msg); // Calling the client handler
    } // End clickSave method

    /**
     * Processes incoming feedback from the server regarding the update request.
     */
    @Override // Overriding from ChatIF
    public void display(Object message) { // Start display method

        // Ensure all UI updates happen on the JavaFX Application Thread
        Platform.runLater(() -> { // Start of runLater lambda

            // Handling structured responses sent as ArrayList
            if (message instanceof ArrayList<?>) { // Check if message is a list
                
                // Casting to the specific list type
                ArrayList<?> data = (ArrayList<?>) message; // Casting
                
                // Extracting the command identifier from the first index
                String command = data.get(0).toString(); // Get command

                // Refactored: Using switch-case for command identification
                switch (command) { // Start switch on command
                    
                    case "EDIT_DETAILS_RESULT": // Case handling the result of the edit
                        
                        // Extracting the result status from the second index
                        String result = data.get(1).toString(); // Get result status
                        
                        // Nested switch to handle specific success/no-change statuses
                        switch (result) { // Start switch on result
                            
                            case "SUCCESS": // Update was successful in DB
                                showAlert("Profile Updated", "Your personal details were updated successfully.", Alert.AlertType.INFORMATION); 
                                closeWindow(); // Close the edit popup
                                break; // Exit switch
                                
                            case "NO_CHANGES": // Update performed but no data actually changed
                                showAlert("No Changes", "No details were updated.", Alert.AlertType.INFORMATION); 
                                break; // Exit switch
                                
                            default: // For any unhandled status codes
                                break; // Exit switch
                        } // End inner switch
                        break; // Exit outer switch

                    default: // For any other commands received
                        break; // Exit switch
                } // End outer switch
            } // End ArrayList check
            
            // Handling general technical errors sent as simple strings
            else if (message instanceof String && "ERROR_EDITING_DETAILS".equals(message)) { // Check for error string
                // Notify the user about the technical failure
                showAlert("Error", "An error occurred while updating your details.", Alert.AlertType.ERROR); 
            } // End error string check
            
        }); // End of runLater lambda
    } // End display method
    
    // --- Helper Methods ---

    /**
     * Reusable utility for displaying JavaFX Alerts.
     */
    private void showAlert(String title, String content, Alert.AlertType type) { // Start showAlert
        Alert alert = new Alert(type); // Create new alert of provided type
        alert.setTitle(title); // Set title
        alert.setHeaderText(null); // Clear header for simplicity
        alert.setContentText(content); // Set the body content
        alert.showAndWait(); // Display and block
    } // End showAlert
    
    /**
     * Closes the window using the save button's reference to the current scene.
     */
    private void closeWindow() { // Start closeWindow
        // Accessing the window via a UI element and closing it
        Stage stage = (Stage) btnSave.getScene().getWindow(); 
        stage.close(); 
    } // End closeWindow

} // End of EditSubscriberDetailsController class