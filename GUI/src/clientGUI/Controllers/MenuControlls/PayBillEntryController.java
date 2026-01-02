package clientGUI.Controllers.MenuControlls; // Define the package for menu-related controllers

import java.util.ArrayList; // Import for using dynamic list structures
import clientGUI.Controllers.ICustomerActions; // Import the interface for customer-specific actions
import common.Visit; // Import the Visit entity class
import javafx.application.Platform; // Import for executing code on the JavaFX application thread
import javafx.event.ActionEvent; // Import for handling UI action events
import javafx.fxml.FXML; // Import for FXML field injection
import javafx.fxml.FXMLLoader; // Import for loading FXML layout files
import javafx.scene.Parent; // Import for representing the root of the scene graph
import javafx.scene.Scene; // Import for managing the stage scene
import javafx.scene.control.Alert; // Import for showing alert dialogs
import javafx.scene.control.Alert.AlertType; // Import for defining alert categories
import javafx.scene.control.Button; // Import for button components
import javafx.scene.control.TextField; // Import for text input fields
import javafx.stage.Stage; // Import for the primary window container

/**
 * Controller for the initial payment entry screen.
 * This class handles the verification of the confirmation code provided by the customer.
 */
public class PayBillEntryController extends BaseMenuController implements ICustomerActions { // Class definition start

    // FXML injected UI components
    @FXML private TextField txtCode; // Input field for the reservation confirmation code
    @FXML private Button btnBack; // Button to return to the previous screen
    @FXML private Button btnVerify; // Button to initiate the code verification process

    /**
     * Called when the client connection is ready. 
     */
    @Override // Overriding method from BaseMenuController
    public void onClientReady() { // Start of onClientReady method
        // Check if the client instance is not null before setting the UI listener
        if (client != null) { // Null check for client
            client.setUI(this); // Register this controller to receive server messages
        } // End of null check
    } // End of onClientReady method

    /**
     * Handles the "Verify" button click. 
     */
    @FXML // Link method to FXML action
    void onVerifyClicked(ActionEvent event) { // Start of verify click handler
        
        // Extract the code from the text field and remove leading/trailing spaces
        String codeStr = txtCode.getText().trim(); // Get and trim input
        
        // Validation: Check if the input is empty
        if (codeStr.isEmpty()) { // Start of empty check
            // Display a warning message to the user
            showAlert("Error", "Please enter a confirmation code.", AlertType.WARNING); // Show warning
            return; // Exit method
        } // End of empty check

        try { // Start of numeric parsing block
            
            // Convert the string input into a long value
            long code = Long.parseLong(codeStr); // Parse to long
            
            // Prepare a message container to send to the server
            ArrayList<Object> message = new ArrayList<>(); // Initialize message list
            
            // Add the command and the code payload to the list
            message.add("GET_VISIT_BY_CODE"); // Add command header
            message.add(code); // Add the parsed code
            
            // Transmit the message list to the server
            client.handleMessageFromClientUI(message); // Send to server
            
        } catch (NumberFormatException e) { // Catch block for non-numeric input
            // Display an error popup if parsing fails
            showAlert("Error", "Code must be a number.", AlertType.ERROR); // Show error popup
        } // End of catch block
        
    } // End of onVerifyClicked method

    /**
     * Callback method to handle messages received from the server.
     */
    @Override // Overriding ChatIF method
    public void display(Object message) { // Start of display method
        
        // Case 1: Handle a string response indicating the visit was not found
        if (message instanceof String && message.equals("VISIT_NOT_FOUND")) { // Check for specific error string
            
            // Ensure UI updates occur on the main JavaFX thread
            Platform.runLater(() -> { // Start of Platform.runLater
                // Inform the user that the visit record does not exist
                showAlert("Not Found", "No active visit found. Returning to menu.", AlertType.INFORMATION); // Show info
                returnToMainMenu(); // Redirect user back to the menu
            }); // End of runLater
            
        } // End of Case 1
        
        // Case 2: Handle a successful retrieval of a Visit object
        else if (message instanceof Visit) { // Check if message is a Visit object
            
            // Ensure UI transition happens on the main JavaFX thread
            Platform.runLater(() -> { // Start of Platform.runLater
                
                // Cast the generic message object to a Visit type
                Visit visit = (Visit) message; // Cast object
                
                try { // Start of FXML loading block
                    
                    // Initialize the loader for the payment summary frame
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/MenuFXML/PayBillFrame.fxml")); // Set FXML path
                    
                    // Load the visual root from the FXML file
                    Parent root = loader.load(); // Load root node

                    // Access the controller of the loaded payment screen
                    PaymentUIController paymentController = loader.getController(); // Get controller instance

                    // Inject dependencies and session data into the next screen's controller
                    paymentController.setupPayment( // Call setup method
                        client, // Pass client reference
                        userType, // Pass user role
                        userId, // Pass user ID
                        visit, // Pass the visit data retrieved from server
                        "Subscriber".equalsIgnoreCase(userType) // Pass subscription status boolean
                    ); // End of setupPayment call

                    // Identify the current window stage
                    Stage stage = (Stage) btnVerify.getScene().getWindow(); // Get current stage
                    
                    // Apply the new scene to the stage
                    stage.setScene(new Scene(root)); // Switch scene
                    
                    // Update the window title
                    stage.setTitle("Bistro - Final Payment Summary"); // Set new title
                    
                    // Render the updated stage to the user
                    stage.show(); // Display stage
                    
                } catch (Exception e) { // Catch block for navigation errors
                    e.printStackTrace(); // Log technical stack trace
                    showAlert("Error", "Failed to load the payment screen.", AlertType.ERROR); // Show error popup
                } // End of try-catch
                
            }); // End of runLater
            
        } // End of Case 2
        
    } // End of display method

    /**
     * Logic to determine the correct main menu path based on the user's role.
     */
    private void returnToMainMenu() { // Start of returnToMainMenu method
        
        // Variable to store the resulting FXML file path
        String path = ""; // Initialize empty path
        
        // Refactored: Using switch-case for role-based navigation logic
        if (userType != null) { // Guard check for null userType
            
            switch (userType) { // Start of switch block
                
                case "Subscriber": // Handle registered subscribers
                    path = "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml"; // Set subscriber path
                    break; // Exit switch
                    
                default: // Handle Occasional users or any other types
                    path = "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml"; // Set occasional path
                    break; // Exit switch
                    
            } // End of switch block
            
        } // End of null guard

        // Execute navigation using the base class helper method
        navigateTo(client, new ActionEvent(btnBack, null), userType, userId, path, "Bistro - Main Menu"); // Trigger navigation
        
    } // End of returnToMainMenu method

    /**
     * Navigates back when the "Back" button is clicked.
     */
    @FXML // Link to FXML action
    void onBackClicked(ActionEvent event) { // Start of back click handler
        returnToMainMenu(); // Call the shared navigation logic
    } // End of onBackClicked method

    /**
     * Helper method to display standardized JavaFX Alert dialogs.
     */
    private void showAlert(String title, String content, AlertType type) { // Start of showAlert method
        Alert alert = new Alert(type); // Instantiate a new alert with specified type
        alert.setTitle(title); // Set window title
        alert.setHeaderText(null); // Remove header for clean look
        alert.setContentText(content); // Set body text
        alert.showAndWait(); // Display and block execution until closed
    } // End of showAlert method

    // Interface stubs for ICustomerActions (No logic changes permitted)
    @Override public void viewOrderHistory(client.ChatClient client, int userId) {} // Empty implementation stub
    @Override public void editPersonalDetails(client.ChatClient client, int userId) {} // Empty implementation stub
    
} // End of PayBillEntryController class