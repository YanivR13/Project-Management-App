package clientGUI.Controllers.OccasionalControlls; // Define the package for occasional user controllers

import java.util.ArrayList; // Import ArrayList for message packaging
import client.ChatClient; // Import the ChatClient for communication
import clientGUI.Controllers.MenuControlls.BaseMenuController; // Import the base controller for inheritance
import javafx.application.Platform; // Import for UI thread safety
import javafx.event.ActionEvent; // Import for handling button actions
import javafx.fxml.FXML; // Import for FXML injection
import javafx.fxml.FXMLLoader; // Import for loading new FXML scenes
import javafx.scene.Node; // Import for generic UI node elements
import javafx.scene.Parent; // Import for the root of the scene graph
import javafx.scene.Scene; // Import for managing the stage scene
import javafx.scene.control.TextArea; // Import for multi-line text display
import javafx.scene.control.TextField; // Import for text input fields
import javafx.stage.Stage; // Import for window management

/**
 * Controller class for the Occasional Customer Registration interface.
 * Handles input validation and registration requests for guest users.
 */
public class OccasionalRegistrationController extends BaseMenuController { // Class start extending BaseMenuController

    // FXML injected UI components
    @FXML private TextField txtNewUser; // TextField for the desired username
    @FXML private TextField txtNewContact; // TextField for phone number or email
    @FXML private TextArea txtLog; // Logger area for user feedback

    /**
     * Executes automatically once the client and session data are ready.
     */
    @Override // Overriding method from BaseMenuController
    public void onClientReady() { // Start of onClientReady
        // Log that the controller is initialized and ready for registration
        appendLog("Ready for new guest registration."); // Logging status
        // Log the identity of the current operator/user
        appendLog("Operator Identity: " + userType + " (ID: " + userId + ")"); // Logging session info
    } // End of onClientReady

    /**
     * Processes the registration form submission.
     */
    @FXML // Link to FXML action
    void clickSubmitRegistration(ActionEvent event) { // Start of clickSubmitRegistration
        // Retrieve and trim input values from the text fields
        String user = txtNewUser.getText().trim(); // Get username
        String contact = txtNewContact.getText().trim(); // Get contact info

        // Logic Check 1: Ensure no fields are empty
        if (user.isEmpty() || contact.isEmpty()) { // Start if empty
            appendLog("Error: All fields are required."); // Log error
            return; // Terminate method
        } // End if empty

        // Logic Check 2: Max username length validation
        if (user.length() > 10) { // Start if length > 10
            appendLog("Error: Username must be 10 characters or less."); // Log error
            return; // Terminate method
        } // End if length check

        // Identify the first character to determine validation type (Phone vs Email)
        char firstChar = contact.charAt(0); // Extract first character
        
        // Logic Check 3: Digital contact validation (Phone)
        if (Character.isDigit(firstChar)) { // Start if first char is digit
            // Verify that the phone number is exactly 10 digits
            if (contact.length() != 10 || !contact.matches("\\d+")) { // Internal check
                appendLog("Error: You started with a number. Phone must be exactly 10 digits."); // Log error
                return; // Terminate method
            } // End internal check
        } // End if digital
        
        // Logic Check 4: String contact validation (Email)
        else { // Start if first char is a letter
            // Verify that the email contains the '@' symbol
            if (!contact.contains("@")) { // Internal check
                appendLog("Error: You started with a letter. Email must contain '@'."); // Log error
                return; // Terminate method
            } // End internal check
        } // End if letter

        // Final Step: Transmit registration data to server if client is active
        if (client != null) { // Start if client exists
            appendLog("Sending registration request for: " + user); // Log transmission
            ArrayList<String> message = new ArrayList<>(); // Initialize message list
            message.add("REGISTER_OCCASIONAL"); // Add command header
            message.add(user); // Add username
            message.add(contact); // Add contact info
            client.handleMessageFromClientUI(message); // Transmit through client
        } else { // If client is null
            appendLog("Fatal Error: No server connection!"); // Log fatal error
        } // End if client check
    } // End of clickSubmitRegistration

    /**
     * Processes messages received from the server.
     */
    @Override // Overriding display method from ChatIF
    public void display(Object message) { // Start of display method
        // Ensure UI updates happen on the main JavaFX thread
        Platform.runLater(() -> { // Start of runLater lambda
            if (message != null) { // Start if message exists
                // Convert server response to String for processing
                String response = message.toString(); // Casting to string
                appendLog("Server Response: " + response); // Log raw response

                // Refactored: Using switch-case for response handling
                switch (response) { // Start of switch
                    case "REGISTRATION_SUCCESS": // Success scenario
                        appendLog("SUCCESS: Account created! You can now go back and login."); // Notify user
                        txtNewUser.clear(); // Clear input field
                        txtNewContact.clear(); // Clear input field
                        break; // Exit switch

                    default: // All other status messages
                        // No additional action needed for general messages
                        break; // Exit switch
                } // End of switch
            } // End if message exists
        }); // End of runLater lambda
    } // End of display method

    /**
     * Navigates back to the Occasional login screen.
     */
    @FXML // Link to FXML action
    void clickBack(ActionEvent event) { // Start of clickBack
        try { // Start of try block
            // Initialize loader for the login frame
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/OccasionalFXML/OccasionalLoginFrame.fxml")); // Set path
            Parent root = loader.load(); // Load root graph
            
            // Dependency Injection: Pass session data back to the login controller
            Object nextController = loader.getController(); // Get controller instance
            if (nextController instanceof BaseMenuController) { // Check if controller is valid
                ((BaseMenuController) nextController).setClient(client, userType, userId); // Inject data
            } // End of injection check

            // Identify current stage and transition scenes
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Get current stage
            Scene scene = new Scene(root); // Create new scene
            // Apply global CSS stylesheet
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm()); // Add styling
            
            stage.setScene(scene); // Assign scene to stage
            stage.show(); // Display the window
        } catch (Exception e) { // Catch navigation exceptions
            e.printStackTrace(); // Print technical trace
            appendLog("Navigation Error: " + e.getMessage()); // Log user-friendly error
        } // End of try-catch block
    } // End of clickBack

    /**
     * Thread-safe method to update the GUI log area.
     */
    public void appendLog(String message) { // Start of appendLog
        // Transition logic to the Application Thread
        Platform.runLater(() -> { // Start of runLater lambda
            if (txtLog != null) { // Check if log component is injected
                txtLog.appendText("> " + message + "\n"); // Append formatted text
            } // End if
        }); // End of runLater lambda
    } // End of appendLog
} // End of OccasionalRegistrationController class