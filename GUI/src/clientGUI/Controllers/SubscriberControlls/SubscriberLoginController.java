package clientGUI.Controllers.SubscriberControlls; // Defining the package for subscriber-related controllers

import java.util.ArrayList; // Importing ArrayList for dynamic message lists
import client.ChatClient; // Importing the main client communication class
import clientGUI.Controllers.MenuControlls.BaseMenuController; // Importing the parent controller for inheritance
import common.LoginSource;
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
import terminalGUI.Controllers.TerminalControllers.TerminalLoginController; // New: Importing Terminal login controller
import terminalGUI.Controllers.TerminalControllers.TerminalMenuController; // New: Importing Terminal menu controller
import javafx.application.Platform; // Importing Platform for thread-safe UI updates

/**
 * Integrated SubscriberLoginController.
 * Merges the professional "Pipe" architecture with Eden's Terminal/Remote source logic.
 */
public class SubscriberLoginController extends BaseMenuController { // Start class definition extending BaseMenuController

    // --- FXML UI Components ---
    @FXML private TextField txtSubscriberID; // TextField for entering the unique Subscriber ID
    @FXML private Button btnLogin; // Button to trigger the login verification process
    @FXML private TextArea txtLog; // Multi-line text area for displaying status logs

    // --- Eden's Logic Integration ---
    private LoginSource loginSource = LoginSource.REMOTE; // Defaulting to remote login source

    /**
     * Sets the origin of the login request (Terminal vs. Remote).
     * @param source The source of the login.
     */
    public void setLoginSource(LoginSource source) { // Start method
        this.loginSource = source; // Assigning the source value
    } // End method

    /**
     * Triggered automatically when the client and session data are ready.
     */
    @Override // Overriding method from BaseMenuController
    public void onClientReady() { // Start of onClientReady method
        // Inform the user that the portal connection is established
        appendLog("Connected to Portal. Waiting for login..."); // Appending status log
        
        // Log the current session identity and the login source for tracking
        appendLog("Identity: " + userType + " | Source: " + loginSource); // Appending session info
    } // End of onClientReady method

    /**
     * Handles the login button click event.
     */
    @FXML // Linking the method to FXML action
    void clickLogin(ActionEvent event) { // Start of clickLogin method
        String subID = txtSubscriberID.getText(); // Extracting input string
        
        if (subID.isEmpty()) { // Validation check
            appendLog("Error: Please enter a Subscriber ID."); // Log validation error
            return; // Terminate execution
        } // End of check

        if (client != null) { // Start of client null check
            appendLog("Attempting login for ID: " + subID); // Appending attempt log
            ArrayList<String> msg = new ArrayList<>(); // Initializing protocol list
            msg.add("LOGIN_SUBSCRIBER"); // Adding command
            msg.add(subID); // Adding payload
            client.handleMessageFromClientUI(msg); // Transmitting to server
        } else { // If client is null
            appendLog("Fatal Error: No server connection!"); // Logging error
        } // End of else
    } // End of clickLogin method

    /**
     * Navigates back to the appropriate previous screen based on the login source.
     */
    @FXML // Linking to FXML action
    void clickBack(ActionEvent event) { // Start of clickBack method
        try { // Start of navigation try block
            FXMLLoader loader; // Declaring loader
            Parent root; // Declaring root node

            // Branching logic based on Eden's loginSource
            if (loginSource == LoginSource.TERMINAL) { // If originating from terminal
                loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/TerminalLoginFrame.fxml")); // Set terminal path
                root = loader.load(); // Loading terminal UI
                
                Object controller = loader.getController(); // Accessing controller
                if (controller instanceof TerminalLoginController) { // If it's a Terminal controller
                    ((TerminalLoginController) controller).setClient(client); // Injecting client
                } // End terminal check
            } else { // If originating from remote portal
                loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml")); // Set remote path
                root = loader.load(); // Loading remote UI
                
                Object controller = loader.getController(); // Accessing controller
                if (controller instanceof BaseMenuController) { // If it uses the Base architecture
                    ((BaseMenuController) controller).setClient(client, userType, userId); // Injecting session
                } // End remote check
            } // End of source branching

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Getting stage
            stage.setScene(new Scene(root)); // Updating scene
            stage.show(); // Displaying window
            
        } catch (Exception e) { // Handling errors
            e.printStackTrace(); // Printing trace
        } // End of try-catch
    } // End of clickBack method

    /**
     * Processes incoming server messages and routes to the correct Menu based on source.
     */
    @Override // Overriding display method from ChatIF
    @SuppressWarnings("unchecked") // Suppressing cast warnings
    public void display(Object message) { // Start of display method
        if (message instanceof ArrayList) { // Start of ArrayList check
            ArrayList<Object> res = (ArrayList<Object>) message; // Casting object
            String status = res.get(0).toString(); // Getting status string

            if (status.equals("LOGIN_SUCCESS")) { // Successful authentication
                appendLog("Login confirmed! Loading dashboard..."); // Logging success
                int userIdFromDB = (int) res.get(1); // Extracting official ID
                String userStatusFromDB = (String)res.get(2);
                
                // Transition to the JavaFX Application thread for navigation
                Platform.runLater(() -> { // Start of UI thread execution
                    // Branching navigation based on Eden's loginSource logic
                    if (loginSource == LoginSource.TERMINAL) { // If terminal source
                        navigateToTerminal(userIdFromDB); // Navigate to Terminal Menu
                    } else { 
                    	// Navigation based on subscriber status
                    	if ("subscriber".equalsIgnoreCase(userStatusFromDB)) {
                            navigateToMenu(userIdFromDB);
                        } else if ("manager".equalsIgnoreCase(userStatusFromDB)) {
                                navigateToManagerMenu(userIdFromDB);
                        } else if ("representative".equalsIgnoreCase(userStatusFromDB)) {
                                navigateToRepresentativeMenu(userIdFromDB);
                        }

                    } // End of branching
                }); // End of runLater lambda
            } else { // For any other server response
                appendLog("Server Response: " + res.toString()); // Logging raw feedback
            } // End status check
        } else if (message != null) { // Simple string message check
            appendLog(message.toString()); // Logging message content
        } // End of message processing
    } // End of display method

    /**
     * Navigates the subscriber to the standard Subscriber Menu.
     */
    private void navigateToMenu(int userIdFromDB) { // Start method
        try { // Start try
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml")); // Set path
            Parent root = loader.load(); // Load root
            
            if (loader.getController() instanceof BaseMenuController) { // Check type
                ((BaseMenuController) loader.getController()).setClient(client, "Subscriber", userIdFromDB); // Inject session
            } // End check
            
            updateStage(root, "Subscriber Dashboard"); // Update display
        } catch (Exception e) { // Handle error
            e.printStackTrace(); // Log trace
            appendLog("UI Error: Could not load Menu."); // Inform user
        } // End try-catch
    } // End method

    /**
     * Navigates the subscriber to the Physical Terminal Menu (Eden's logic).
     */
    private void navigateToTerminal(int userId) { // Start of navigateToTerminal method
        try { // Start of try block
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/Terminal/TerminalMenuFrame.fxml")); // Set path
            Parent root = loader.load(); // Loading UI

            TerminalMenuController controller = loader.getController(); // Accessing controller
            controller.setClient(client); // Injecting client connection

            updateStage(root, "Customer Service Terminal"); // Update display
        } catch (Exception e) { // Handling errors
            e.printStackTrace(); // Printing trace
            appendLog("Terminal navigation error."); // Logging failure
        } // End of try-catch
    } // End of method
    
    
    /**
     * Navigates the manager to the Manager Menu.
     */
    private void navigateToManagerMenu(int userIdFromDB) {
        try {
            FXMLLoader loader = new FXMLLoader(
            		getClass().getResource("/managmentGUI/ManagerDashboard.fxml"));
            Parent root = loader.load();

            if (loader.getController() instanceof BaseMenuController) {
                ((BaseMenuController) loader.getController())
                    .setClient(client, "Manager", userIdFromDB);
            }

            updateStage(root, "Manager Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            appendLog("UI Error: Could not load Manager Menu.");
        }
    }
    
    
    /**
     * Navigates the representative to the Representative Menu.
     */
    private void navigateToRepresentativeMenu(int userIdFromDB) {
        try {
            FXMLLoader loader = new FXMLLoader(
            		getClass().getResource("/managmentGUI/RepresentativeDashboard.fxml"));
            Parent root = loader.load();

            if (loader.getController() instanceof BaseMenuController) {
                ((BaseMenuController) loader.getController())
                    .setClient(client, "Representative", userIdFromDB);
            }

            updateStage(root, "Representative Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            appendLog("UI Error: Could not load Representative Menu.");
        }
    }


    /**
     * Shared utility for updating the stage and applying CSS.
     */
    private void updateStage(Parent root, String title) {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        Scene scene = new Scene(root);

        // Apply global CSS only for non-management screens
        if (!title.contains("Manager") && !title.contains("Representative")) {
            if (getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css") != null) {
                scene.getStylesheets().add(
                    getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm()
                );
            }
        }

        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }


    /**
     * Appends a message to the UI log area in a thread-safe manner.
     */
    public void appendLog(String message) { // Start method
        Platform.runLater(() -> { // Ensuring UI thread execution
            if (txtLog != null) { // Component check
                txtLog.appendText("> " + message + "\n"); // Appending text
            } // End check
        }); // End lambda
    } // End method
    
} // End of integrated SubscriberLoginController class