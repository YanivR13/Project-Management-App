package clientGUI.Controllers.OccasionalControlls; // Defining the package for occasional user controllers

import java.util.ArrayList; // Importing ArrayList for data list management
import client.ChatClient; // Importing the ChatClient for communication
import clientGUI.Controllers.MenuControlls.BaseMenuController; // Importing the base controller for inheritance
import clientGUI.Controllers.RemoteLoginController; // Importing the remote login controller reference
import javafx.application.Platform; // Importing Platform for UI thread safety
import javafx.event.ActionEvent; // Importing ActionEvent for UI interaction
import javafx.fxml.FXML; // Importing FXML annotation for UI injection
import javafx.fxml.FXMLLoader; // Importing FXMLLoader to load new scenes
import javafx.scene.Node; // Importing Node for generic UI elements
import javafx.scene.Parent; // Importing Parent for the scene graph root
import javafx.scene.Scene; // Importing Scene for window content
import javafx.scene.control.Button; // Importing Button control
import javafx.scene.control.TextArea; // Importing TextArea control
import javafx.scene.control.TextField; // Importing TextField control
import javafx.scene.layout.VBox; // Importing VBox layout container
import javafx.stage.Stage; // Importing Stage for window management

/**
 * Controller for the Occasional (Guest) Login interface.
 * Manages guest login, registration navigation, and username resets.
 */
public class OccasionalLoginController extends BaseMenuController { // Class definition extending BaseMenuController

    // --- FXML Injected Components ---
    @FXML private TextField txtUsername; // TextField for entering the username
    @FXML private TextField txtContact; // TextField for entering the contact info
    @FXML private TextField txtForgotContact; // TextField for contact info in forgot area
    @FXML private TextField txtNewUsername; // TextField for the new username in forgot area
    @FXML private TextArea txtLog; // TextArea used for displaying system logs
    @FXML private VBox paneLogin; // VBox container for the login form
    @FXML private VBox paneForgot; // VBox container for the forgotten username form
    @FXML private Button btnLogin; // Button used to trigger the login process

    /**
     * Called automatically when the client and session data are ready.
     */
    @Override // Overriding the onClientReady method from BaseMenuController
    public void onClientReady() { // Start of onClientReady method
        // Log a message indicating the portal connection is active
        appendLog("Connected to Portal. Waiting for occasional login..."); // Appending log message
        
        // Log the current system identity for debugging/tracking
        appendLog("System Identity: " + userType + " (ID: " + userId + ")"); // Appending identity info
    } // End of onClientReady method

    /**
     * Handles the login button click event.
     */
    @FXML // Linking the method to FXML action
    void clickLogin(ActionEvent event) { // Start of clickLogin method
        // Extracting input values from text fields
        String username = txtUsername.getText(); // Getting username text
        String contact = txtContact.getText(); // Getting contact text

        // Validation: Ensure that both fields are not empty
        if (username.isEmpty() || contact.isEmpty()) { // Checking for empty fields
            appendLog("Error: Both fields are required."); // Logging validation error
            return; // Terminating method execution
        } // End of if validation block

        // Verify if the inherited client instance is available
        if (client != null) { // Checking if client exists
            // Inform the user that the login attempt has started
            appendLog("Attempting login for Guest: " + username); // Appending log
            
            // Constructing the protocol message for the server
            ArrayList<String> msg = new ArrayList<>(); // Initializing message list
            msg.add("LOGIN_OCCASIONAL"); // Adding the command header
            msg.add(username); // Adding the username payload
            msg.add(contact); // Adding the contact payload
            
            // Sending the message to the server via the client UI handler
            client.handleMessageFromClientUI(msg); // Transmitting data
        } else { // If client is null
            // Log a fatal error indicating connection loss
            appendLog("Fatal Error: No server connection!"); // Appending fatal error log
        } // End of client check else block
    } // End of clickLogin method

    /**
     * Processes messages received from the server.
     */
    @Override // Overriding the display method from ChatIF (via BaseMenuController)
    @SuppressWarnings("unchecked") // Suppressing warnings for generic list casting
    public void display(Object message) { // Start of display method
        // Transition to the JavaFX Application thread for UI updates
        Platform.runLater(() -> { // Start of runLater lambda
            
            // Check if the received message is a list of results
            if (message instanceof ArrayList) { // Start of ArrayList check
                // Casting the message to an ArrayList of objects
                ArrayList<Object> res = (ArrayList<Object>) message; // Casting object
                // Extracting the status string from the first element
                String status = res.get(0).toString(); // Getting status

                // Using switch-case to handle different login statuses
                switch (status) { // Start of switch block
                    case "LOGIN_OCCASIONAL_SUCCESS": // If login succeeded
                        appendLog("Welcome! Navigating to Guest Menu..."); // Logging success
                        int userIdFromDB = (int) res.get(1); // Extracting DB user ID
                        navigateToMenu(userIdFromDB); // Triggering navigation
                        break; // Exiting switch

                    default: // For any other status (e.g., error messages)
                        appendLog("Server Response: " + status); // Logging server response
                        break; // Exiting switch
                } // End of switch block
            } // End of ArrayList check
            
            // Handle cases where the message is a simple string response
            else if (message != null) { // If message is not null
                // Convert the message to a string for comparison
                String response = message.toString(); // Extracting string
                
                // Using switch-case for string-based server responses
                switch (response) { // Start of response switch
                    case "RESET_USERNAME_SUCCESS": // If username reset worked
                        appendLog("SUCCESS: Your username has been updated!"); // Logging reset success
                        txtForgotContact.clear(); // Clearing the contact field
                        txtNewUsername.clear(); // Clearing the new username field
                        break; // Exiting switch
                        
                    default: // For any other generic server message
                        appendLog("Server Message: " + response); // Logging message
                        break; // Exiting switch
                } // End of response switch
            } // End of string message check
            
        }); // End of runLater lambda
    } // End of display method

    /**
     * Navigates to the guest menu after a successful login.
     */
    private void navigateToMenu(int userIdFromDB) { // Start of navigateToMenu method
        try { // Start of try block for FXML loading
            // Initializing the loader for the guest menu frame
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml")); // Setting FXML path
            Parent root = loader.load(); // Loading the root element
            
            // Extract the controller from the loader for dependency injection
            Object nextController = loader.getController(); // Getting controller instance
            
            // Check if the next controller follows the BaseMenuController architecture
            if (nextController instanceof BaseMenuController) { // If it is a BaseMenuController
                // Inject the client, role, and the new DB ID into the next screen
                ((BaseMenuController) nextController).setClient(client, "Occasional", userIdFromDB); // Setting session data
            } // End of injection check
            
            // Identify the current stage and set the new scene
            Stage stage = (Stage) btnLogin.getScene().getWindow(); // Getting current stage
            Scene scene = new Scene(root); // Creating new scene
            
            // Apply global CSS styling if the file exists
            if (getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css") != null) { // Checking for CSS file
                scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm()); // Adding stylesheet
            } // End of CSS check
            
            // Configure and display the stage
            stage.setTitle("Guest Dashboard"); // Setting window title
            stage.setScene(scene); // Assigning scene to stage
            stage.show(); // Displaying the window
            
        } catch (Exception e) { // Catching any navigation exceptions
            e.printStackTrace(); // Printing technical stack trace
            appendLog("Navigation Error: " + e.getMessage()); // Logging user-friendly error
        } // End of try-catch block
    } // End of navigateToMenu method

    /**
     * Navigates back to the main remote login portal.
     */
    @FXML // Linking to FXML action
    void clickBack(ActionEvent event) { // Start of clickBack method
        try { // Start of try block
            // Initializing loader for the portal screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml")); // Path to portal
            Parent root = loader.load(); // Loading root
            
            // Pass the current session and client back to the previous screen
            Object nextController = loader.getController(); // Getting controller
            if (nextController instanceof BaseMenuController) { // Checking type
                ((BaseMenuController) nextController).setClient(client, userType, userId); // Injecting session
            } // End if
            
            // Switching the scene on the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Getting stage
            stage.setScene(new Scene(root)); // Setting portal scene
            stage.show(); // Displaying stage
        } catch (Exception e) { // Handling errors
            e.printStackTrace(); // Printing trace
        } // End try-catch
    } // End of clickBack method

    /**
     * Navigates to the registration screen for guests.
     */
    @FXML // Linking to FXML action
    void clickRegister(ActionEvent event) { // Start of clickRegister method
        try { // Start of try block
            // Loading the registration frame FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/OccasionalFXML/OccasionalRegistrationFrame.fxml")); // Setting path
            Parent root = loader.load(); // Loading root
            
            // Handle dependency injection for the registration screen
            Object nextController = loader.getController(); // Getting controller
            
            // Prefer BaseMenuController injection if updated
            if (nextController instanceof BaseMenuController) { // Start of Base check
                ((BaseMenuController) nextController).setClient(client, userType, userId); // Injecting full session
            } else { // Fallback for controllers not yet migrated to Base class
                try { // Start of reflection attempt
                    // Try calling setClient manually via reflection
                    nextController.getClass().getMethod("setClient", ChatClient.class).invoke(nextController, client); // Invoking method
                } catch (Exception ignored) { // Ignoring if method doesn't exist
                } // End reflection try-catch
            } // End check else

            // Identifying the stage and showing the registration scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Getting stage
            stage.setScene(new Scene(root)); // Setting scene
            stage.show(); // Displaying stage
        } catch (Exception e) { // Handling errors
            e.printStackTrace(); // Printing trace
        } // End try-catch
    } // End of clickRegister method

    /**
     * Submits a request to reset a guest's username.
     */
    @FXML // Linking to FXML action
    void clickSubmitForgot(ActionEvent event) { // Start of clickSubmitForgot method
        // Extracting input for the reset process
        String contact = txtForgotContact.getText(); // Getting contact info
        String newUsername = txtNewUsername.getText(); // Getting desired username
        
        // Basic input validation for reset
        if (contact.isEmpty() || newUsername.isEmpty()) { // Checking for empty fields
            appendLog("Error: Fields required."); // Logging error
            return; // Terminating
        } // End if empty

        // Constraint check: Maximum username length
        if (newUsername.length() > 10) { // If longer than 10 characters
            appendLog("Error: Max 10 chars."); // Logging constraint error
            return; // Terminating
        } // End if length

        // Building message for the username reset protocol
        ArrayList<String> msg = new ArrayList<>(); // Initializing list
        msg.add("RESET_OCCASIONAL_USERNAME"); // Adding command
        msg.add(contact); // Adding contact payload
        msg.add(newUsername); // Adding new name payload
        
        // Sending message if client is active
        if (client != null) { // Null check
            client.handleMessageFromClientUI(msg); // Transmitting reset request
        } // End client check
    } // End of clickSubmitForgot method

    /**
     * Shows the "Forgot Username" UI section.
     */
    @FXML // Linking to FXML action
    void showForgotArea(ActionEvent event) { // Start of showForgotArea method
        paneLogin.setVisible(false); // Hiding the login pane
        paneLogin.setManaged(false); // Removing login pane from layout
        paneForgot.setVisible(true); // Showing the forgot pane
        paneForgot.setManaged(true); // Including forgot pane in layout
    } // End of showForgotArea method

    /**
     * Hides the "Forgot Username" UI section and returns to login.
     */
    @FXML // Linking to FXML action
    void hideForgotArea(ActionEvent event) { // Start of hideForgotArea method
        paneForgot.setVisible(false); // Hiding the forgot pane
        paneForgot.setManaged(false); // Removing forgot pane from layout
        paneLogin.setVisible(true); // Showing the login pane
        paneLogin.setManaged(true); // Including login pane in layout
    } // End of hideForgotArea method

    /**
     * Appends a message to the UI log in a thread-safe manner.
     */
    public void appendLog(String message) { // Start of appendLog method
        // Execute the update on the UI thread
        Platform.runLater(() -> { // Start of runLater lambda
            // Verify that the log component exists before writing
            if (txtLog != null) { // Null check for log
                txtLog.appendText("> " + message + "\n"); // Appending formatted message
            } // End null check
        }); // End runLater lambda
    } // End of appendLog method

} // End of OccasionalLoginController class