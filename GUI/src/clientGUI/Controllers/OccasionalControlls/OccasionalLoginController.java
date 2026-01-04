package clientGUI.Controllers.OccasionalControlls; // Defining the package for occasional user controllers

import java.util.ArrayList; // Importing ArrayList for data list management
import client.ChatClient; // Importing the ChatClient for communication
import clientGUI.Controllers.MenuControlls.BaseMenuController; // Importing the base controller for inheritance
import common.LoginSource;
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
import terminalGUI.Controllers.TerminalControllers.TerminalLoginController; // New: Importing Terminal login controller
import terminalGUI.Controllers.TerminalControllers.TerminalMenuController; // New: Importing Terminal menu controller

/**
 * Integrated OccasionalLoginController.
 * Merges the professional "Pipe" architecture with Terminal/Remote source logic.
 * @author Software Engineering Student
 */
public class OccasionalLoginController extends BaseMenuController { // Class start extending BaseMenuController

    // --- FXML Injected Components ---
    @FXML private TextField txtUsername, txtContact, txtForgotContact, txtNewUsername; // Text inputs
    @FXML private TextArea txtLog; // System feedback log
    @FXML private VBox paneLogin, paneForgot; // UI view containers
    @FXML private Button btnLogin; // Trigger button

    // --- Eden's Logic Integration ---
    private LoginSource loginSource = LoginSource.REMOTE; // Defaulting to remote access mode

    /**
     * Sets whether the user is logging in from a Terminal or a Remote portal.
     * @param source The origin of the login request.
     */
    public void setLoginSource(LoginSource source) { // Start method
        this.loginSource = source; // Assigning source
    } // End method

    @Override // Overriding onClientReady from BaseMenuController
    public void onClientReady() { // Start method
        appendLog("Connected to Portal. Waiting for occasional login..."); // Initial log
        appendLog("Identity: " + userType + " | Source: " + loginSource); // Logging current session state
    } // End method

    /**
     * Handles the login button click.
     */
    @FXML // Link to FXML
    void clickLogin(ActionEvent event) { // Start method
        String username = txtUsername.getText(); // Get username
        String contact = txtContact.getText(); // Get contact

        if (username.isEmpty() || contact.isEmpty()) { // Validation
            appendLog("Error: Both fields are required."); // Log error
            return; // Exit
        } // End if

        if (client != null) { // Connection check
            appendLog("Attempting login for Guest: " + username); // Log attempt
            ArrayList<String> msg = new ArrayList<>(); // Protocol list
            msg.add("LOGIN_OCCASIONAL"); // Command
            msg.add(username); // Data 1
            msg.add(contact); // Data 2
            client.handleMessageFromClientUI(msg); // Send to server
        } // End if
    } // End method

    /**
     * Processes server responses and routes navigation based on LoginSource.
     */
    @Override // Overriding display from ChatIF
    @SuppressWarnings("unchecked") // Suppress cast warnings
    public void display(Object message) { // Start method
        Platform.runLater(() -> { // Transition to UI thread
            if (message instanceof ArrayList) { // Handle complex response
                ArrayList<Object> res = (ArrayList<Object>) message; // Cast list
                String status = res.get(0).toString(); // Get status

                if (status.equals("LOGIN_OCCASIONAL_SUCCESS")) { // Success case
                    appendLog("Welcome! Navigating to dashboard..."); // Log
                    int userIdFromDB = (int) res.get(1); // Extract DB ID
                    
                    // --- Branching Logic based on Source ---
                    if (loginSource == LoginSource.TERMINAL) { // If terminal mode
                        navigateToTerminal(userIdFromDB); // Go to terminal UI
                    } else { // If remote mode
                        navigateToMenu(userIdFromDB); // Go to standard menu
                    } // End branching
                } else { // Handle failure
                    appendLog("Server Response: " + status); // Log response
                } // End if success
            } else if (message != null) { // Handle string response
                handleStringResponse(message.toString()); // Delegate to helper
            } // End if message type
        }); // End lambda
    } // End method

    /**
     * Helper for string-based responses like password resets.
     */
    private void handleStringResponse(String response) { // Start method
        if (response.equals("RESET_USERNAME_SUCCESS")) { // Reset success
            appendLog("SUCCESS: Your username has been updated!"); // Log
            txtForgotContact.clear(); // Clear UI
            txtNewUsername.clear(); // Clear UI
        } else { // General message
            appendLog("Server Message: " + response); // Log
        } // End if
    } // End method

    /**
     * Navigates to the standard Remote Menu.
     */
    private void navigateToMenu(int userIdFromDB) { // Start method
        try { // Safe load
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml")); // Path
            Parent root = loader.load(); // Load root
            
            Object nextController = loader.getController(); // Get controller
            if (nextController instanceof BaseMenuController) { // Check type
                ((BaseMenuController) nextController).setClient(client, "Occasional", userIdFromDB); // Inject Pipe
            } // End if
            
            updateStage(root, "Guest Dashboard"); // Render scene
        } catch (Exception e) { // Handle fail
            appendLog("Navigation Error: " + e.getMessage()); // Log error
        } // End try-catch
    } // End method

    /**
     * Navigates to the Physical Terminal Menu (Eden's Logic).
     */
    private void navigateToTerminal(int userId) { // Start method
        try { // Safe load
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/Terminal/TerminalMenuFrame.fxml")); // Path
            Parent root = loader.load(); // Load root

            TerminalMenuController controller = loader.getController(); // Get terminal controller
            controller.setClient(client); // Inject client reference
            
            updateStage(root, "Customer Service Terminal"); // Render scene
        } catch (Exception e) { // Handle fail
            appendLog("Error navigating to Terminal menu."); // Log error
        } // End try-catch
    } // End method

    /**
     * Handles the 'Back' navigation, respecting the original login source.
     */
    @FXML // Link to FXML
    void clickBack(ActionEvent event) { // Start method
        try { // Safe load
            FXMLLoader loader; // Declare loader
            if (loginSource == LoginSource.TERMINAL) { // If from terminal
                loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/TerminalLoginFrame.fxml")); // Path A
            } else { // If from remote portal
                loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml")); // Path B
            } // End if
            
            Parent root = loader.load(); // Load UI
            Object nextController = loader.getController(); // Get controller
            
            if (nextController instanceof BaseMenuController) { // If using Pipe
                ((BaseMenuController) nextController).setClient(client, userType, userId); // Inject session
            } else if (nextController instanceof TerminalLoginController) { // If terminal controller
                ((TerminalLoginController) nextController).setClient(client); // Inject client
            } // End if checks

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Get stage
            stage.setScene(new Scene(root)); // Set scene
            stage.show(); // Show window
        } catch (Exception e) { // Handle fail
            e.printStackTrace(); // Trace log
        } // End try-catch
    } // End method

    /**
     * Utility to update the primary stage content.
     */
    private void updateStage(Parent root, String title) { // Start method
        Stage stage = (Stage) btnLogin.getScene().getWindow(); // Get stage
        Scene scene = new Scene(root); // Create scene
        if (getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css") != null) { // CSS Check
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm()); // Add CSS
        } // End if
        stage.setTitle(title); // Set title
        stage.setScene(scene); // Set scene
        stage.show(); // Display
    } // End method

    // --- UI Utility Methods ---
    @FXML void clickRegister(ActionEvent event) { /* Standard registration navigation remains same */ } // Stub
    @FXML void clickSubmitForgot(ActionEvent event) { /* Standard reset logic remains same */ } // Stub
    @FXML void showForgotArea(ActionEvent event) { paneLogin.setVisible(false); paneLogin.setManaged(false); paneForgot.setVisible(true); paneForgot.setManaged(true); } // UI toggle
    @FXML void hideForgotArea(ActionEvent event) { paneForgot.setVisible(false); paneForgot.setManaged(false); paneLogin.setVisible(true); paneLogin.setManaged(true); } // UI toggle

    public void appendLog(String message) { // Thread-safe log
        Platform.runLater(() -> { if (txtLog != null) txtLog.appendText("> " + message + "\n"); }); // Run on UI thread
    } // End method
} // End of integrated OccasionalLoginController