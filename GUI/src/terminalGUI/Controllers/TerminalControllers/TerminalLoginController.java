package terminalGUI.Controllers.TerminalControllers; // Defining the package for terminal-side controllers

import client.ChatClient; // Importing the main client class for communication
import clientGUI.Controllers.OccasionalControlls.OccasionalLoginController; // Importing the guest controller for casting
import clientGUI.Controllers.SubscriberControlls.SubscriberLoginController; // Importing the member controller for casting
import common.ChatIF; // Importing the interface for server-side responses
import common.LoginSource;
import javafx.event.ActionEvent; // Importing ActionEvent for UI interaction handling
import javafx.fxml.FXML; // Importing FXML annotation for UI component injection
import javafx.fxml.FXMLLoader; // Importing FXMLLoader for loading login frames
import javafx.scene.Parent; // Importing Parent as the root node for scenes
import javafx.scene.Scene; // Importing Scene to manage the window content
import javafx.stage.Stage; // Importing Stage for window management
import javafx.scene.Node; // Importing Node to identify the current window

/**
 * Integrated TerminalLoginController.
 * This class handles the transition from a physical terminal to specific login portals
 * while maintaining the "Pipe" architecture and Eden's terminal logic.
 */
public class TerminalLoginController implements ChatIF { // Start of TerminalLoginController class definition

    /** Shared network client instance used for server communication. */
    private ChatClient client; // Declaring the active network client

    /**
     * Injects the persistent ChatClient instance into this controller.
     * @param client The active network client.
     */
    public void setClient(ChatClient client) { // Start of setClient method
        this.client = client; // Assigning the client reference
    } // End of setClient method

    /**
     * Event handler for the 'Subscriber Login' button on the terminal.
     */
    @FXML // Link method to FXML button action
    void clickSubscriber(ActionEvent event) { // Start of clickSubscriber method
        // Navigate to the subscriber login portal with terminal-specific context
        loadScreen(event, "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberLoginFrame.fxml", true, "Subscriber Login"); // Calling loadScreen
    } // End of clickSubscriber method

    /**
     * Event handler for the 'Occasional Login' button on the terminal.
     */
    @FXML // Link method to FXML button action
    void clickOccasional(ActionEvent event) { // Start of clickOccasional method
        // Navigate to the guest login portal with terminal-specific context
        loadScreen(event, "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalLoginFrame.fxml", false, "Occasional Login"); // Calling loadScreen
    } // End of clickOccasional method

    /**
     * Core Navigation Engine: Loads the target FXML and injects session data + terminal flag.
     * @param fxmlPath     The path to the login FXML.
     * @param isSubscriber Flag to distinguish between subscriber and guest controllers.
     * @param title        The title for the new stage.
     */
    private void loadScreen(ActionEvent event, String fxmlPath, boolean isSubscriber, String title) { // Start loadScreen
        
        try { // Start of navigation try-block
            
            // Step 1: Initialize the FXML loader for the requested login portal
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath)); // Creating loader
            
            // Step 2: Load the UI graph root from the FXML resource
            Parent root = loader.load(); // Loading root node
            
            // Step 3: Extract the controller instance from the loader
            Object controller = loader.getController(); // Retrieving controller

            // --- THE 300% FIX: Aligning with the new 3-parameter 'Pipe' architecture ---
            
            if (isSubscriber) { // If navigating to the Subscriber login screen
                // Casting to the refactored SubscriberLoginController
                SubscriberLoginController subController = (SubscriberLoginController) controller; // Performing cast
                
                // FIXED: Providing (client, userType, userId). Using -1 as ID is not yet known.
                subController.setClient(client, "Subscriber", -1); // Injecting session into the Pipe
                
                // Preserving Eden's logic: Marking the origin as a physical Terminal
                subController.setLoginSource(LoginSource.TERMINAL); // Setting terminal source
                
            } else { // If navigating to the Occasional (Guest) login screen
                // Casting to the refactored OccasionalLoginController
                OccasionalLoginController occController = (OccasionalLoginController) controller; // Performing cast
                
                // FIXED: Providing (client, userType, userId). Using -1 as ID is not yet known.
                occController.setClient(client, "Occasional", -1); // Injecting session into the Pipe
                
                // Preserving Eden's logic: Marking the origin as a physical Terminal
                occController.setLoginSource(LoginSource.TERMINAL); // Setting terminal source
            } // End of injection branching

            // Step 4: Configure the window (Stage) and assign the new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Identifying current stage
            Scene scene = new Scene(root); // Creating scene with loaded root
            
            // Step 5: Apply global CSS styling for terminal UI consistency
            if (getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css") != null) { // Checking for CSS
                scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm()); // Adding styles
            } // End CSS check

            // Step 6: Update stage properties and display
            stage.setTitle(title); // Updating window title
            stage.setScene(scene); // Setting scene to stage
            stage.show(); // Displaying window

        } catch (Exception e) { // Handling potential FXML loading or injection errors
            // Print technical details for debugging terminal-specific navigation issues
            e.printStackTrace(); // Printing technical stack trace
        } // End of try-catch block
        
    } // End of the loadScreen method

    /**
     * Implementation of the ChatIF display method for server communication.
     */
    @Override // Overriding from the ChatIF interface
    public void display(Object message) { // Start of display method
        // This method can be implemented for terminal-wide broadcast messages
    } // End of display method
    
} // End of integrated TerminalLoginController class