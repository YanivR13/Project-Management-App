package clientGUI.Controllers; // Defining the package where the main controllers reside

import client.ChatClient; // Importing the main client class for network communication
import clientGUI.Controllers.MenuControlls.BaseMenuController; // Importing the parent class for session logic
import javafx.event.ActionEvent; // Importing ActionEvent to handle user clicks on buttons
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Importing the loader to instantiate FXML layout files
import javafx.scene.Node; // Importing Node to access elements within the UI hierarchy
import javafx.scene.Parent; // Importing Parent as the root node for scenes
import javafx.scene.Scene; // Importing Scene to manage the content of the window
import javafx.scene.control.TextArea; // Importing TextArea to display the system log
import javafx.stage.Stage; // Importing Stage to manage the primary window
import javafx.application.Platform; // Importing Platform for thread-safe UI updates

/**
 * The RemoteLoginController handles the primary navigation logic for the landing screen.
 * It serves as the entry point for both Occasional and Subscriber login paths.
 */
public class RemoteLoginController extends BaseMenuController { // Class start inheriting from BaseMenuController

    // FXML injected UI component for logging connection status and messages
    @FXML private TextArea txtLog; 

    /**
     * Event handler for the Occasional (Guest) login button.
     * Triggers the navigation to the guest login interface.
     */
    @FXML // Link method to FXML button action
    void clickOccasional(ActionEvent event) { // Start of clickOccasional method
        // Invoke the navigation engine with the specific path for guests
        loadScreen(event, "OccasionalFXML/OccasionalLoginFrame.fxml", "Occasional Login"); 
    } // End of clickOccasional method

    /**
     * Event handler for the Subscriber login button.
     * Triggers the navigation to the member login interface.
     */
    @FXML // Link method to FXML button action
    void clickSubscriber(ActionEvent event) { // Start of clickSubscriber method
        // Invoke the navigation engine with the specific path for subscribers
        loadScreen(event, "SubscriberFXML/SubscriberLoginFrame.fxml", "Subscriber Login"); 
    } // End of clickSubscriber method

    /**
     * Core Navigation Engine: Loads FXML files and propagates session data.
     * This method ensures the 'client', 'userType', and 'userId' are passed forward.
     */
    private void loadScreen(ActionEvent event, String fxmlFile, String title) { // Start of loadScreen method
        
        try { // Start of try block to handle potential FXML loading errors
            
            // Step 1: Initialize the loader with the relative path to the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/" + fxmlFile)); // Creating loader
            
            // Step 2: Load the UI graph from the resource file
            Parent root = loader.load(); // Loading the root node
            
            // Step 3: Access the controller instance created by the FXMLLoader
            Object controller = loader.getController(); // Retrieving the target controller
            
            /**
             * Dependency Injection:
             * If the target controller inherits from BaseMenuController, push the session data.
             */
            if (controller instanceof BaseMenuController) { // Check if controller is part of the session hierarchy
                // Transmit the active client and current session context to the new screen
                ((BaseMenuController) controller).setClient(client, userType, userId); // Performing injection
            } // End of controller type check

            // Step 4: Identify the current Stage using the event source node
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Getting the window
            
            // Step 5: Create a new Scene with the loaded visual root
            Scene scene = new Scene(root); // Initializing scene
            
            // Step 6: Apply the global CSS styles if the resource file is found
            if (getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css") != null) { // Check for CSS existence
                scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm()); // Add CSS
            } // End of CSS null check
            
            // Step 7: Update window properties and render the new scene
            stage.setTitle(title); // Updating window title
            stage.setScene(scene); // Setting the scene to the stage
            stage.show(); // Displaying the window
            
        } catch (Exception e) { // Start of catch block for unexpected loading failures
            
            // Print the technical details to the system console for debugging
            e.printStackTrace(); // Logging technical stack trace
            
            // Notify the user about the failure through the visible GUI log
            appendLog("Error loading screen: " + e.getMessage()); // Appending error to log
            
        } // End of try-catch block
        
    } // End of loadScreen method

    /**
     * Implementation of the ChatIF interface method to handle server messages.
     */
    @Override // Overriding display from ChatIF (inherited via BaseMenuController)
    public void display(Object message) { // Start of display method
        // If a message is received from the server, append it to the text log
        if (message != null) { // Check if the message is not null
            appendLog(message.toString()); // Convert object to string and log it
        } // End of null check
    } // End of display method

    /**
     * Appends a message to the UI log area in a thread-safe manner.
     */
    public void appendLog(String message) { // Start of appendLog method
        // Use Platform.runLater to ensure the UI update occurs on the JavaFX Application Thread
        Platform.runLater(() -> { // Start of lambda block
            // Ensure the log component is actually injected before writing to it
            if (txtLog != null) { // Checking if txtLog is initialized
                txtLog.appendText("> " + message + "\n"); // Appending formatted text
            } // End of component null check
        }); // End of lambda block
    } // End of appendLog method
    
} // End of RemoteLoginController class