package clientGUI.Controllers.OccasionalControlls; // Define the package for occasional user controllers

import java.util.ArrayList; // Import for dynamic array list management
import client.ChatClient; // Import the main client communication class
import clientGUI.Controllers.ICustomerActions; // Import the interface for customer-specific actions
import clientGUI.Controllers.RemoteLoginController; // Import reference to the login controller
import clientGUI.Controllers.MenuControlls.BaseMenuController; // Import the base controller for inheritance
import clientGUI.Controllers.MenuControlls.ExitWaitingListHelper; // Import helper for waiting list logic
import common.ChatIF; // Import the communication interface
import javafx.application.Platform; // Import for UI thread safety
import javafx.event.ActionEvent; // Import for handling UI action events
import javafx.fxml.FXML; // Import for FXML field injection
import javafx.fxml.FXMLLoader; // Import for loading FXML layout files
import javafx.scene.Node; // Import for generic UI node elements
import javafx.scene.Parent; // Import for root UI elements
import javafx.scene.Scene; // Import for stage scene management
import javafx.scene.control.Alert; // Import for showing alert dialogs
import javafx.scene.control.Alert.AlertType; // Import for alert types
import javafx.scene.control.Button; // Import for button components
import javafx.scene.control.ButtonType; // Import for alert button types
import javafx.scene.control.TextArea; // Import for multi-line text display
import javafx.stage.Stage; // Import for window management

/**
 * The OccasionalMenuController serves as the main dashboard for Guest (Occasional) users.
 * It manages navigation to reservations, payments, and waiting list features.
 */
public class OccasionalMenuController extends BaseMenuController implements ICustomerActions { // Start class definition

    // FXML injected buttons for guest dashboard navigation
    @FXML private Button btnNewReservation; // Button to start a new reservation
    @FXML private Button btnPayBill; // Button to navigate to the payment screen
    @FXML private Button btnViewReservation; // Button to view existing reservations
    @FXML private Button btnLogout; // Button to sign out and return to portal
    @FXML private Button btnExitWaitingList; // Button to request removal from waiting list
    
    // Console-style log area for providing real-time feedback
    @FXML private TextArea txtLog; // Reference to the log text area

    // --- 1. Initialization & UI Setup ---

    /**
     * Executes automatically when the controller is ready and data is injected.
     */
    @Override // Overriding method from BaseMenuController
    public void onClientReady() { // Start of onClientReady method
        // Inform the user that the Guest Portal is now active
        appendLog("Guest Portal Active. Welcome, User ID: " + userId); // Log greeting with user ID
    } // End of onClientReady method

    // --- 2. Reservation & Billing Action Handlers ---

    /**
     * Navigates the guest to the New Reservation screen.
     */
    @FXML // Link method to FXML action
    void clickNewReservation(ActionEvent event) { // Start of clickNewReservation method
        // Call the shared interface method inherited from BaseMenuController
        createNewReservation(client, event, userType, userId); // Trigger navigation
    } // End of method
    
    /**
     * Navigates the guest to the Pay Bill (verification) screen.
     */
    @FXML // Link method to FXML action
    void clickPayBill(ActionEvent event) { // Start of clickPayBill method
        // Call the shared interface method to initiate payment process
        payBill(client, event, userType, userId); // Trigger navigation
    } // End of method
    
    /**
     * Navigates the guest to the View Reservation screen.
     */
    @FXML // Link method to FXML action
    void clickViewReservation(ActionEvent event) { // Start of clickViewReservation method
        // Call the shared interface method to show active bookings
        viewReservation(client, event, userType, userId); // Trigger navigation
    } // End of method

    /**
     * Initiates the process to leave the waiting list.
     */
    @FXML // Link method to FXML action
    void clickExitWaitingList(ActionEvent event) { // Start of clickExitWaitingList method
        // Log the action for user feedback
        appendLog("Exit Waiting List triggered."); // Appending to UI log
        // Use the helper class to send the request to the server
        ExitWaitingListHelper.requestLeaveWaitingList(this.client, this.userId); // Triggering helper logic
    } // End of method
  
    // --- 3. Session Termination (Logout) ---

    /**
     * Handles the logout process and returns the user to the portal.
     */
    @FXML // Link method to FXML action
    void clickLogout(ActionEvent event) { // Start of clickLogout method
        try { // Start of navigation try block
            // Prepare the loader for the main login portal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml")); // Setting FXML path
            
            // Generate the parent root from the FXML file
            Parent root = loader.load(); // Loading the UI graph
            
            // Extract the controller of the next screen for dependency injection
            Object nextController = loader.getController(); // Accessing controller instance
            
            // Check if the target controller supports the shared BaseMenuController architecture
            if (nextController instanceof BaseMenuController) { // Start of type check
                // Reset user session data but preserve the active server connection
                ((BaseMenuController) nextController).setClient(client, null, 0); // Injecting cleared session
            } // End of if block
            
            // Identify the current window stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Getting current stage
            
            // Apply the login portal scene to the stage
            stage.setScene(new Scene(root)); // Switching scenes
            
            // Render the window
            stage.show(); // Displaying stage
            
        } catch (Exception e) { // Catch block for navigation or loading errors
            // Log technical stack trace for debugging
            e.printStackTrace(); // Printing error details
            // Inform the user of the failure via the UI log
            appendLog("Logout Error: Unable to return to portal."); // Logging failure
        } // End of try-catch block
    } // End of clickLogout method

    // --- 4. ICustomerActions Stubs (Role-based exclusions) ---

    /**
     * Implementation stub for viewing order history.
     */
    @Override // Required by ICustomerActions
    public void viewOrderHistory(ChatClient client, int userId) { // Start method
        // Occasional guests do not have long-term history stored in this system version
    } // End method
    
    /**
     * Implementation stub for editing personal profile details.
     */
    @Override // Required by ICustomerActions
    public void editPersonalDetails(ChatClient client, int userId) { // Start method
        // Profile management is not available for one-time guest users
    } // End method

    // --- 5. Server Communication (ChatIF) ---

    /**
     * Processes incoming data or status messages from the server.
     */
    @Override // Overriding display method from BaseMenuController/ChatIF
    public void display(Object message) { // Start of display method
        
        // Basic Guard: Ensure the message is not null before processing
        if (message != null) { // Start check
            // Append any incoming message directly to the UI log for tracking
            appendLog(message.toString()); // Log the raw message
        } // End check
        
        // Logical Branching: Specifically handle string-based protocol responses
        if (message instanceof String) { // Start type check
            
            // Cast the generic object to a String for pattern matching
            String response = (String) message; // Casting
            
            // Define criteria for waiting list related feedback
            boolean isWaitingListResponse = (response.startsWith("CANCEL_WAITING") || response.equals("NOT_ON_WAITING_LIST")); // Validation logic
            
            // If the message relates to the waiting list status
            if (isWaitingListResponse) { // Start block
                // Delegate the visual feedback (popups) to the helper class
                ExitWaitingListHelper.handleServerResponse(response); // Handle server status
            } // End of inner if
            
        } // End of String check
        
    } // End of display method

    /**
     * Updates the UI log area in a thread-safe manner.
     */
    public void appendLog(String message) { // Start of appendLog method
        // Force the execution to the JavaFX Application Thread
        Platform.runLater(() -> { // Start of lambda block
            // Verify that the UI component is properly injected before writing
            if (txtLog != null) { // Null check for txtLog
                // Append the formatted message to the text area
                txtLog.appendText("> " + message + "\n"); // Adding text with prefix
            } // End of null check
        }); // End of lambda block
    } // End of appendLog method
    
} // End of OccasionalMenuController class