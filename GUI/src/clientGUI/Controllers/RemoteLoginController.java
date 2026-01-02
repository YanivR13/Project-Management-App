package clientGUI.Controllers; // Defining the package where the main controllers reside

import client.ChatClient; // Importing the main client class for network communication
import clientGUI.Controllers.MenuControlls.BaseMenuController; // Importing the parent class for session logic
import clientGUI.Controllers.OccasionalControlls.OccasionalLoginController; // New: Importing guest controller for type check
import clientGUI.Controllers.SubscriberControlls.SubscriberLoginController; // New: Importing subscriber controller for type check
import commonLogin.LoginSource; // New: Importing the LoginSource enum for navigation logic
import javafx.event.ActionEvent; // Importing ActionEvent to handle user clicks
import javafx.fxml.FXML; // Importing FXML annotation for injection
import javafx.fxml.FXMLLoader; // Importing the loader to instantiate FXML files
import javafx.scene.Node; // Importing Node to access UI elements
import javafx.scene.Parent; // Importing Parent as the root node for scenes
import javafx.scene.Scene; // Importing Scene to manage window content
import javafx.scene.control.TextArea; // Importing TextArea for logging
import javafx.stage.Stage; // Importing Stage for window management
import javafx.application.Platform; // Importing Platform for thread-safe UI updates

/**
 * Integrated RemoteLoginController.
 * This version maintains the "Pipe" architecture while preserving Eden's LoginSource logic.
 */
public class RemoteLoginController extends BaseMenuController { // Class start inheriting from BaseMenuController

    // FXML injected UI component for logging connection status and messages
    @FXML private TextArea txtLog; // Declaring the log text area

    /**
     * Event handler for the Occasional (Guest) login button.
     */
    @FXML // Link method to FXML button action
    void clickOccasional(ActionEvent event) { // Start method
        // Navigate to the guest login screen
        loadScreen(event, "OccasionalFXML/OccasionalLoginFrame.fxml", "Occasional Login"); // Triggering loader
    } // End method

    /**
     * Event handler for the Subscriber login button.
     */
    @FXML // Link method to FXML button action
    void clickSubscriber(ActionEvent event) { // Start method
        // Navigate to the subscriber login screen
        loadScreen(event, "SubscriberFXML/SubscriberLoginFrame.fxml", "Subscriber Login"); // Triggering loader
    } // End method

    /**
     * Core Navigation Engine: Loads FXML and propagates session data + navigation source.
     */
    private void loadScreen(ActionEvent event, String fxmlFile, String title) { // Start method
        
        try { // Start of try block to handle FXML loading
            
            // Step 1: Initialize the FXML loader with the correct resource path
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/" + fxmlFile)); // Creating loader
            
            // Step 2: Load the UI root from the FXML file
            Parent root = loader.load(); // Loading root node
            
            // Step 3: Access the controller instance created for the new scene
            Object controller = loader.getController(); // Retrieving the controller
            
            // --- STEP 4: INTEGRATED DEPENDENCY INJECTION ---
            
            // Phase A: Inject the standard session "Pipe" (Client, UserType, UserId)
            if (controller instanceof BaseMenuController) { // Checking for base controller hierarchy
                ((BaseMenuController) controller).setClient(client, userType, userId); // Injecting session data
            } // End Pipe injection

            // Phase B: Inject Eden's LoginSource (Critical for 'Back' button logic)
            if (controller instanceof SubscriberLoginController) { // If navigating to Subscriber login
                ((SubscriberLoginController) controller).setLoginSource(LoginSource.REMOTE); // Setting source to REMOTE
            } // End Subscriber source check
            else if (controller instanceof OccasionalLoginController) { // If navigating to Occasional login
                ((OccasionalLoginController) controller).setLoginSource(LoginSource.REMOTE); // Setting source to REMOTE
            } // End Occasional source check

            // Step 5: Configure the window (Stage) and the scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Getting current stage
            Scene scene = new Scene(root); // Creating new scene
            
            // Step 6: Apply global CSS styles if available
            if (getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css") != null) { // Checking for CSS
                scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm()); // Adding CSS
            } // End CSS check
            
            // Step 7: Finalize stage properties and display
            stage.setTitle(title); // Updating window title
            stage.setScene(scene); // Assigning scene to stage
            stage.show(); // Rendering window
            
        } catch (Exception e) { // Handling unexpected navigation errors
            e.printStackTrace(); // Logging technical trace
            appendLog("Error loading screen: " + e.getMessage()); // Feedback to user
        } // End try-catch
    } // End loadScreen method

    /**
     * Implementation of the ChatIF display method.
     */
    @Override // Overriding from inherited interface
    public void display(Object message) { // Start method
        if (message != null) { // Null check
            appendLog(message.toString()); // Log server message
        } // End if
    } // End method

    /**
     * Appends a message to the UI log area in a thread-safe manner.
     */
    public void appendLog(String message) { // Start method
        Platform.runLater(() -> { // Ensuring UI thread execution
            if (txtLog != null) { // Component check
                txtLog.appendText("> " + message + "\n"); // Appending text
            } // End check
        }); // End lambda
    } // End appendLog method
    
} // End of integrated RemoteLoginController class