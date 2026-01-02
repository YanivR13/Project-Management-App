package clientGUI.Controllers.SubscriberControlls; // Defining the package for subscriber-related controllers

import java.util.ArrayList; // Importing ArrayList for dynamic message lists
import client.ChatClient; // Importing the main client communication class
import clientGUI.Controllers.MenuControlls.BaseMenuController; // Importing the parent controller for inheritance
import clientGUI.Controllers.RemoteLoginController; // Importing reference to the portal login controller
import javafx.event.ActionEvent; // Importing ActionEvent for UI interaction handling
import javafx.fxml.FXML; // Importing FXML annotation for UI element injection
import javafx.fxml.FXMLLoader; // Importing FXMLLoader for loading layout files
import javafx.scene.Parent; // Importing Parent for the scene graph root
import javafx.scene.Scene; // Importing Scene for window content management
import javafx.scene.Node; // Importing Node for identifying UI elements in events
import javafx.scene.control.Button; // Importing Button component
import javafx.scene.control.TextArea; // Importing TextArea component
import javafx.scene.control.TextField; // Importing TextField component
import javafx.stage.Stage; // Importing Stage for window management
import javafx.application.Platform; // Importing Platform for thread-safe UI updates

/**
 * Controller class for the Subscriber Login interface.
 * Inherits from BaseMenuController to utilize shared session and client logic.
 */
public class SubscriberLoginController extends BaseMenuController { // Start class definition extending BaseMenuController

    // --- FXML UI Components ---
    @FXML private TextField txtSubscriberID; // TextField for entering the unique Subscriber ID
    @FXML private Button btnLogin; // Button to trigger the login verification process
    @FXML private TextArea txtLog; // Multi-line text area for displaying status logs

    /**
     * Triggered automatically when the client and session data are ready.
     */
    @Override // Overriding method from BaseMenuController
    public void onClientReady() { // Start of onClientReady method
        // Inform the user that the portal connection is established
        appendLog("Connected to Portal. Waiting for login..."); // Appending status log
        
        // Log the current session identity for tracking purposes
        appendLog("System Identity: " + userType + " (ID: " + userId + ")"); // Appending identity info
    } // End of onClientReady method

    /**
     * Handles the login button click event.
     */
    @FXML // Linking the method to FXML action
    void clickLogin(ActionEvent event) { // Start of clickLogin method
        // Extracting the entered ID from the text field
        String subID = txtSubscriberID.getText(); // Getting input string
        
        // Validation: Ensure the ID field is not empty before sending to server
        if (subID.isEmpty()) { // Start of empty check
            appendLog("Error: Please enter a Subscriber ID."); // Log validation error
            return; // Terminate method execution
        } // End of empty check check

        // Verify that the inherited client instance is correctly initialized
        if (client != null) { // Start of client null check
            // Notify the user about the login attempt
            appendLog("Attempting login for ID: " + subID); // Appending attempt log
            
            // Constructing the protocol message list for the server
            ArrayList<String> msg = new ArrayList<>(); // Initializing message list
            msg.add("LOGIN_SUBSCRIBER"); // Adding the command header
            msg.add(subID); // Adding the Subscriber ID as payload
            
            // Transmitting the message to the server via the client UI handler
            client.handleMessageFromClientUI(msg); // Calling client transmission
        } else { // If client is null
            // Log a fatal error indicating a loss of server connection
            appendLog("Fatal Error: No server connection!"); // Appending fatal error log
        } // End of client check else block
        
    } // End of clickLogin method

    /**
     * Navigates back to the main portal selection screen.
     */
    @FXML // Linking to FXML action
    void clickBack(ActionEvent event) { // Start of clickBack method
        try { // Start of navigation try block
            // Loading the main remote login portal FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml")); // Set path
            Parent root = loader.load(); // Loading the UI graph root
            
            // Extract the controller of the portal screen for dependency injection
            Object controller = loader.getController(); // Accessing controller instance
            
            // Check if the portal controller follows the BaseMenuController architecture
            if (controller instanceof BaseMenuController) { // If it is a BaseMenuController
                // Inject the current client and session state into the previous screen
                ((BaseMenuController) controller).setClient(client, userType, userId); // Passing session data
            } // End of injection check
            
            // Identify the current stage and update its scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Getting current stage
            stage.setScene(new Scene(root)); // Setting the new portal scene
            stage.show(); // Displaying the stage
            
        } catch (Exception e) { // Catch block for loading errors
            // Print technical details for debugging
            e.printStackTrace(); // Printing stack trace
        } // End of try-catch block
        
    } // End of clickBack method

    /**
     * Processes incoming server messages regarding the login process.
     */
    @Override // Overriding display method from ChatIF (via BaseMenuController)
    @SuppressWarnings("unchecked") // Suppressing warnings for generic list casting
    public void display(Object message) { // Start of display method
        
        // Handling structured protocol responses sent as ArrayList
        if (message instanceof ArrayList) { // Start of ArrayList check
            // Casting the message to the protocol list format
            ArrayList<Object> res = (ArrayList<Object>) message; // Casting object
            // Extracting the status identifier from the first index
            String status = res.get(0).toString(); // Getting status string

            // Refactored: Using switch-case for identifying server status
            switch (status) { // Start switch on status
                
                case "LOGIN_SUCCESS": // Case where credentials matched in DB
                    appendLog("Login confirmed! Loading dashboard..."); // Logging success
                    // Navigate to the main menu using the real Database ID retrieved
                    Platform.runLater(() -> navigateToMenu((int)res.get(1))); // Triggering UI transition
                    break; // Exit switch
                    
                default: // For any other responses (e.g., error messages)
                    appendLog("Server Response: " + res.toString()); // Logging raw server feedback
                    break; // Exit switch
                    
            } // End of switch block
            
        } // End of ArrayList check
        
        // Handling cases where the message is a simple string feedback
        else if (message != null) { // If message is not null
            // Directly log the string content to the UI log
            appendLog(message.toString()); // Logging message
        } // End of string message check
        
    } // End of display method

    /**
     * Navigates the subscriber to their main dashboard screen.
     */
    private void navigateToMenu(int userIdFromDB) { // Start of navigateToMenu method
        try { // Start of FXML loading try block
            // Initializing the loader for the subscriber menu frame
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml")); // Set path
            Parent root = loader.load(); // Loading UI root
            
            // Access the next controller for session data injection
            Object nextController = loader.getController(); // Getting controller instance
            
            if (nextController instanceof BaseMenuController) { // Checking if it is a BaseMenuController
                // Update session: role is now "Subscriber" and ID is the official DB identifier
                ((BaseMenuController) nextController).setClient(client, "Subscriber", userIdFromDB); // Injecting session
            } // End of injection check
            
            // Switching the scene on the current stage
            Stage stage = (Stage) btnLogin.getScene().getWindow(); // Getting primary stage
            stage.setScene(new Scene(root)); // Assigning new menu scene
            stage.show(); // Displaying window
            
        } catch (Exception e) { // Catching navigation or loading exceptions
            // Log technical details for debugging
            e.printStackTrace(); // Printing trace
            // Inform the user about the UI loading failure
            appendLog("UI Error: Could not load Menu Frame."); // Logging error
        } // End of try-catch block
        
    } // End of navigateToMenu method

    /**
     * Appends a message to the UI log area in a thread-safe manner.
     */
    public void appendLog(String message) { // Start of appendLog method
        // Force the execution to the JavaFX Application Thread
        Platform.runLater(() -> { // Start of lambda block
            // Ensure the text area component is not null before writing
            if (txtLog != null) { // Null check for txtLog
                // Append formatted message to the end of the log
                txtLog.appendText("> " + message + "\n"); // Adding log entry
            } // End of null check
        }); // End of lambda block
    } // End of appendLog method
    
} // End of SubscriberLoginController class