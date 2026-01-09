package clientGUI.Controllers.SubscriberControlls; // Define the package for subscriber dashboard controllers

import java.io.IOException; // Import for handling input/output exceptions during FXML loading
import client.ChatClient; // Import the main communication client
import clientGUI.Controllers.ICustomerActions; // Import the interface for customer-related actions
import clientGUI.Controllers.RemoteLoginController; // Import the portal login controller reference
import clientGUI.Controllers.MenuControlls.BaseMenuController; // Import the parent controller for shared logic
import clientGUI.Controllers.MenuControlls.ExitWaitingListHelper; // Import helper for waiting list removal
import javafx.application.Platform; // Import for running tasks on the JavaFX thread
import javafx.event.ActionEvent; // Import for handling button click events
import javafx.fxml.FXML; // Import for FXML field injection
import javafx.fxml.FXMLLoader; // Import for loading FXML layout files
import javafx.scene.Node; // Import for generic UI node elements
import javafx.scene.Parent; // Import for the root of the UI graph
import javafx.scene.Scene; // Import for stage scene management
import javafx.scene.control.Button; // Import for button UI components
import javafx.scene.control.TextArea; // Import for multi-line text display
import javafx.stage.Stage; // Import for managing window stages

/**
 * The SubscriberMenuController acts as the primary dashboard for authenticated subscribers.
 * It manages navigation to all subscriber-specific features while maintaining session state.
 */
public class SubscriberMenuController extends BaseMenuController implements ICustomerActions { // Start class definition

    // FXML injected buttons for the subscriber dashboard
    @FXML private Button btnNewRes; // Button for creating a new reservation
    @FXML private Button btnPayBill; // Button for paying an active bill
    @FXML private Button btnViewRes; // Button for viewing/canceling active reservations
    @FXML private Button btnExitWait; // Button for leaving the waiting list
    @FXML private Button btnHistory; // Button for viewing reservation history
    @FXML private Button btnEditProfile; // Button for updating personal profile details
    @FXML private Button btnLogout; // Button for logging out of the system
    @FXML private Button btnBack;   // Back button only for manager and representative
    
    // FXML injected text area for system feedback logs
    @FXML private TextArea txtLog; // Reference to the log area

    // --- 1. Initialization & UI Display ---

    /**
     * Executes automatically when the controller is ready and session data is injected.
     */
    @Override // Overriding method from BaseMenuController
    public void onClientReady() { // Start of onClientReady method
        // Log a welcome message containing the subscriber's unique ID
        appendLog("Welcome back! Subscriber ID: " + userId); // Adding greeting to log
        
        // Show Back button only if staff entered customer mode
        if (actingAsSubscriber && btnBack != null) 
        {
            btnBack.setVisible(true);
            }
    } // End of onClientReady method

    // --- 2. Reservation & Billing Actions (ICustomerActions) ---

    /**
     * Navigates to the New Reservation screen.
     */
    @FXML // Link method to FXML action
    void clickNewRes(ActionEvent event) { // Start method
        // Utilize the shared navigation logic from the base class
        createNewReservation(client, event, userType, userId); // Calling base method
    } // End method
    
    /**
     * Navigates to the Bill Payment screen.
     */
    @FXML // Link method to FXML action
    void clickPayBill(ActionEvent event) { // Start method
        // Utilize the shared navigation logic from the base class
        payBill(client, event, userType, userId); // Calling base method
    } // End method
    
    /**
     * Navigates to the View Reservations screen.
     */
    @FXML // Link method to FXML action
    void clickViewRes(ActionEvent event) { // Start method
        // Utilize the shared navigation logic from the base class
        viewReservation(client, event, userType, userId); // Calling base method
    } // End method

    // --- 3. Functional Modules ---

    /**
     * Triggers the waiting list removal process.
     */
    @FXML // Link method to FXML action
    void clickExitWait(ActionEvent event) { // Start method
        // Inform the user that the process has started via the log
        appendLog("Exit Waiting List triggered."); // Logging action
        // Call the static helper to transmit the request to the server
        ExitWaitingListHelper.requestLeaveWaitingList(this.client, this.userId); // Executing helper
    } // End method
    
    /**
     * Opens the Reservation History window.
     */
    @FXML // Link method to FXML action
    void clickHistory(ActionEvent event) { // Start method
        // Call the interface implementation to load history
        viewOrderHistory(client, userId); // Executing history logic
    } // End method
    
    /**
     * Opens the Profile Editing window.
     */
    @FXML // Link method to FXML action
    void clickEditProfile(ActionEvent event) { // Start method
        // Call the interface implementation to load profile settings
        editPersonalDetails(client, userId); // Executing edit logic
    } // End method

    // --- 4. Navigation Implementations with Dependency Injection (The Pipe) ---

    /**
     * Loads the Reservation History frame and injects required session data.
     */
    @Override // Implementing ICustomerActions
    public void viewOrderHistory(ChatClient client, int userId) { // Start of viewOrderHistory
        try { // Start of FXML loading try block
            // Initialize the loader for the history frame
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/SubscriberFXML/ReservationsHistoryFrame.fxml")); // Setting path
            Parent root = loader.load(); // Loading the UI graph

            // Extract the controller to inject session dependencies
            Object controller = loader.getController(); // Accessing controller instance
            
            // Branching: Handle injection based on the controller's architecture
            if (controller instanceof BaseMenuController) { // If the controller supports the modern base class
                ((BaseMenuController) controller).setClient(client, userType, userId); // Injecting full session data
            } else if (controller instanceof ReservationHistoryController) { // Fallback for legacy controllers
                ((ReservationHistoryController) controller).setClient(client); // Injecting client only
                ((ReservationHistoryController) controller).loadReservationsForUser(userId); // Triggering data fetch
            } // End of branching logic
            
            // Display the initialized screen in a new window
            showNewWindow(root, "Order History"); // Calling helper to show stage
        } catch (IOException e) { // Catching loading errors
            e.printStackTrace(); // Printing technical trace
        } // End of try-catch block
    } // End of viewOrderHistory method
    
    /**
     * Loads the Edit Profile frame and injects required session data.
     */
    @Override // Implementing ICustomerActions
    public void editPersonalDetails(ChatClient client, int userId) { // Start of editPersonalDetails
        try { // Start of FXML loading try block
            // Initialize the loader for the profile editing frame
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/SubscriberFXML/EditSubscriberDetailsFrame.fxml")); // Setting path
            Parent root = loader.load(); // Loading the UI graph

            // Extract the controller to inject session dependencies
            Object controller = loader.getController(); // Accessing controller instance
            
            // Branching: Handle injection based on the controller's architecture
            if (controller instanceof BaseMenuController) { // If it is a modern BaseMenuController
                ((BaseMenuController) controller).setClient(client, userType, userId); // Injecting via uniform pipe
            } else if (controller instanceof EditSubscriberDetailsController) { // Fallback for specific controllers
                ((EditSubscriberDetailsController) controller).setClient(client); // Injecting client
                ((EditSubscriberDetailsController) controller).setUserId(userId); // Injecting userId
            } // End of branching logic

            // Display the initialized screen in a new window
            showNewWindow(root, "Edit Profile"); // Calling helper to show stage
        } catch (IOException e) { // Catching loading errors
            e.printStackTrace(); // Printing technical trace
        } // End of try-catch block
    } // End of editPersonalDetails method

    /**
     * Internal helper to create and display a new Stage.
     */
    private void showNewWindow(Parent root, String title) { // Start of showNewWindow method
        Stage stage = new Stage(); // Instantiate a new stage container
        stage.setTitle(title); // Set the window title
        stage.setScene(new Scene(root)); // Create a new scene with the provided root
        stage.show(); // Display the window to the user
    } // End of showNewWindow method

    // --- 5. Session Termination (Logout) ---

    /**
     * Logs the user out and returns to the primary login portal.
     */
    @FXML // Link method to FXML action
    void clickLogout(ActionEvent event) { // Start of clickLogout method
        try { // Start of navigation try block
            // Prepare the loader for the main login portal frame
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml")); // Setting path
            Parent root = loader.load(); // Loading the UI graph root
            
            // Access the next controller for session hand-off
            Object nextController = loader.getController(); // Accessing controller instance
            
            // Uniform Injection: Reset session data while preserving the server connection
            if (nextController instanceof BaseMenuController) { // Check controller type
                ((BaseMenuController) nextController).setClient(client, null, 0); // Injecting cleared session
            } // End of check
            
            // Identify current stage and apply the login portal scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Getting stage
            stage.setScene(new Scene(root)); // Switching scene
            stage.show(); // Displaying stage
            
        } catch (Exception e) { // Catching generic exceptions
            e.printStackTrace(); // Printing error details
            appendLog("Error during logout: " + e.getMessage()); // Logging failure to user
        } // End of try-catch block
    } // End of clickLogout method
    
    
    /**
     * Returns staff users (Manager/Representative) from the Subscriber menu back to their respective dashboard.
     */
    @FXML
    void clickBack(ActionEvent event) {
        try {
            String fxmlPath;

            // Decide where to return based on original role
            if ("Manager".equalsIgnoreCase(originalUserType)) {
                fxmlPath = "/managmentGUI/ManagerDashboard.fxml";
            } else {
                fxmlPath = "/managmentGUI/RepresentativeDashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BaseMenuController) {
                BaseMenuController base = (BaseMenuController) controller;
                base.setClient(client, originalUserType, userId);
                base.setActingAsSubscriber(false); // exit subscriber mode
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            appendLog("Navigation Error: " + e.getMessage());
            e.printStackTrace();
        }
    }



    // --- 6. Server Communication Handling ---

    /**
     * Processes server responses and manages thread-safe UI updates.
     */
    @Override // Overriding from BaseMenuController/ChatIF
    public void display(Object message) { // Start of display method
        // Guard Clause: Ensure the message is valid before logging
        if (message != null) { // Check for non-null message
            appendLog(message.toString()); // Log raw message content
        } // End of check
        
        // Logical Filter: Handle specific string-based protocol signals
        if (message instanceof String) { // Start of String check
            String response = (String) message; // Casting to string
            
            // Identify if the response relates to waiting list operations
            boolean isWaitResponse = (response.startsWith("CANCEL_WAITING") || response.equals("NOT_ON_WAITING_LIST")); // Validation
            
            if (isWaitResponse) { // If relevant response found
                // Delegate popup feedback to the specialized helper class
                ExitWaitingListHelper.handleServerResponse(response); // Handling server feedback
            } // End of internal if
        } // End of String check
    } // End of display method

    /**
     * Appends text to the GUI log in a thread-safe manner.
     */
    public void appendLog(String message) { // Start of appendLog method
        // Redirect execution to the JavaFX Application Thread
        Platform.runLater(() -> { // Start of lambda block
            // Verify that the log component exists before appending
            if (txtLog != null) { // Null check for log area
                txtLog.appendText("> " + message + "\n"); // Appending formatted string
            } // End of null check
        }); // End of lambda block
    } // End of appendLog method
    
} // End of SubscriberMenuController class