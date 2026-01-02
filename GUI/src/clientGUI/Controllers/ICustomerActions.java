package clientGUI.Controllers; // Define the package location for the controller interfaces

import client.ChatClient; // Import the main communication client class
import clientGUI.Controllers.MenuControlls.BaseMenuController; // Import the base controller for dependency injection
import javafx.event.ActionEvent; // Import ActionEvent to handle UI button clicks
import javafx.fxml.FXMLLoader; // Import FXMLLoader to load FXML layout files
import javafx.scene.Node; // Import Node to access UI elements within the scene graph
import javafx.scene.Parent; // Import Parent as the root for the scene
import javafx.scene.Scene; // Import Scene for window content management
import javafx.stage.Stage; // Import Stage for primary window management

/**
 * The ICustomerActions interface defines the behavioral contract for customer-related activities.
 * It provides a centralized navigation engine via Java 8 default methods.
 */
public interface ICustomerActions { // Start of ICustomerActions interface definition

    // =========================================================================
    // SECTION 1: Navigation Shortcuts (Default Methods)
    // =========================================================================

    /**
     * Navigates the user to the New Reservation screen.
     */
    default void createNewReservation(ChatClient client, ActionEvent event, String userType, int userId) { // Method start
        // Call the central navigation engine with the specific FXML path and title
        navigateTo(client, event, userType, userId, "/clientGUI/fxmlFiles/MenuFXML/NewReservationFrame.fxml", "Bistro - New Reservation"); // Execution
    } // End of createNewReservation method

    /**
     * Navigates the user to the Payment Code entry screen.
     */
    default void payBill(ChatClient client, ActionEvent event, String userType, int userId) { // Method start
        // Call the central navigation engine for the payment entry logic
        navigateTo(client, event, userType, userId, "/clientGUI/fxmlFiles/MenuFXML/PayBillEntryFrame.fxml", "Bistro - Enter Payment Code"); // Execution
    } // End of payBill method

    /**
     * Navigates the user to the screen where they can view existing reservations.
     */
    default void viewReservation(ChatClient client, ActionEvent event, String userType, int userId) { // Method start
        // Call the central navigation engine for the viewing/cancellation screen
        navigateTo(client, event, userType, userId, "/clientGUI/fxmlFiles/MenuFXML/ViewReservationFrame.fxml", "Bistro - View & Pay"); // Execution
    } // End of viewReservation method
    
    // =========================================================================
    // SECTION 2: Abstract Actions (To be implemented by Subclasses)
    // =========================================================================

    /**
     * Abstract method for retrieving reservation history.
     */
    void viewOrderHistory(ChatClient client, int userId); // Method signature (No implementation)

    /**
     * Abstract method for editing personal profile details.
     */
    void editPersonalDetails(ChatClient client, int userId); // Method signature (No implementation)
    
    /**
     * Sends a command to exit the digital waiting list.
     */
    default void exitWaitingList(ChatClient client, String confirmationCode) { // Method start
        // Logic left empty as per the original source provided
    } // End of exitWaitingList method

    // =========================================================================
    // SECTION 3: The Central Navigation Engine
    // =========================================================================

    /**
     * The Central Navigation Engine: Handles FXML loading and Controller injection.
     */
    default void navigateTo(ChatClient client, ActionEvent event, String userType, int userId, String path, String title) { // Method start
        
        try { // Start of try block for safe FXML loading
            
            // Step 1: Initialize the FXML Loader with the target resource path provided in parameters
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path)); // Creating loader instance
            
            // Step 2: Load the parent root element from the FXML file (The visual layout)
            Parent root = loader.load(); // Loading the UI graph
            
            // Step 3: Access the controller associated with the newly loaded FXML scene
            Object controller = loader.getController(); // Retrieving the controller instance
            
            /**
             * Dependency Injection:
             * If the controller implements BaseMenuController, we push the session data through "The Pipe".
             */
            if (controller instanceof BaseMenuController) { // Check if the controller is of type BaseMenuController
                // Inject the client reference, user role, and user ID into the target controller
                ((BaseMenuController) controller).setClient(client, userType, userId); // Performing injection
            } // End of dependency injection check

            // Step 4: Identify the current Stage (Window) using the ActionEvent's source node
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Accessing the stage
            
            // Step 5: Create a new Scene container with the loaded visual root
            Scene scene = new Scene(root); // Initializing the scene object
            
            // Step 6: Apply the global CSS stylesheet to ensure consistent styling across all screens
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm()); // Style injection
            
            // Step 7: Update window properties and switch the active scene
            stage.setTitle(title); // Updating the title bar text
            stage.setScene(scene); // Assigning the new scene to the stage
            stage.show(); // Rendering the updated stage to the user
            
        } catch (Exception e) { // Start of catch block for unexpected errors
            
            // Print the technical stack trace to the console for debugging (FXML missing, CSS errors, etc.)
            e.printStackTrace(); // Logging technical failure
            
        } // End of try-catch block
        
    } // End of navigateTo method
    
} // End of ICustomerActions interface