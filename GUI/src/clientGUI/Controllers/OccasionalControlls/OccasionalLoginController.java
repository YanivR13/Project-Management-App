package clientGUI.Controllers.OccasionalControlls;

import java.util.ArrayList;
import client.ChatClient;
import clientGUI.Controllers.RemoteLoginController;
import common.ChatIF;
import commonLogin.LoginSource;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import terminalGUI.Controllers.TerminalControllers.TerminalLoginController;
import terminalGUI.Controllers.TerminalControllers.TerminalMenuController;

/**
 * Controller for the Occasional (Guest) Login interface.
 * This class handles guest authentication, username recovery, and navigation
 * between the login, registration, and main guest menu screens.
 * * <p>It implements {@link ChatIF} to serve as a listener for server-side messages
 * within the OCSF framework.</p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public class OccasionalLoginController implements ChatIF {
    
    /** The network client responsible for server communication. */
    private ChatClient client;
    
    private LoginSource loginSource = LoginSource.REMOTE;


    /** FXML injected components for user input and visual feedback. */
    @FXML private TextField txtUsername, txtContact, txtForgotContact, txtNewUsername;
    @FXML private TextArea txtLog;
    
    /** Layout containers used for toggling between the Login and Forgot Username views. */
    @FXML private VBox paneLogin, paneForgot;
    
    /** Trigger button for the login process. */
    @FXML private Button btnLogin;

    /**
     * Injects the ChatClient instance and establishes this controller as the active UI listener.
     * @param client The active OCSF network client.
     */
    public void setClient(ChatClient client) {
        this.client = client;
        if (client != null) {
            // Register this controller to receive incoming messages from the server
            client.setUI(this);
            appendLog("Connected to Portal. Waiting for occasional login...");
        }
    }
    
    public void setLoginSource(LoginSource source) {
        this.loginSource = source;
    }

    /**
     * Initiates the login process for an occasional customer.
     * Validates input fields and sends a "LOGIN_OCCASIONAL" request to the server.
     * @param event The ActionEvent from the 'Login' button.
     */
    @FXML
    void clickLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String contact = txtContact.getText();

        // Layer 1: Validate that required input is present
        if (username.isEmpty() || contact.isEmpty()) {
            appendLog("Error: Both fields are required.");
            return;
        }

        // Layer 2: Construct the protocol message (Command + Data)
        ArrayList<String> msg = new ArrayList<>();
        msg.add("LOGIN_OCCASIONAL");
        msg.add(username);
        msg.add(contact);

        // Layer 3: Dispatch message to server via the client logic
        if (client != null) {
            appendLog("Attempting login for Guest: " + username);
            client.handleMessageFromClientUI(msg);
        }
    }

    /**
     * HOOK METHOD: Processes data received from the Server.
     * Wraps execution in Platform.runLater to ensure UI changes occur on the JavaFX Application Thread.
     * @param message The data object sent by the server.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void display(Object message) {
        // Asynchronous server responses must be handled on the UI thread
        Platform.runLater(() -> {
            // Case A: Response is an ArrayList (Complex result containing data like UserID)
            if (message instanceof ArrayList) {
                ArrayList<Object> res = (ArrayList<Object>) message;
                String status = res.get(0).toString();

                // Successful login leads to scene navigation
                if (status.equals("LOGIN_OCCASIONAL_SUCCESS")) {
                    appendLog("Welcome! Navigating to Guest Menu...");
                    // The internal database ID is expected at index 1
                    int userId = (int) res.get(1);
                    if (loginSource == LoginSource.TERMINAL) {
                        navigateToTerminal(userId);
                    } else {
                        navigateToMenu(userId);
                    }
                } else {
                    // Log protocol errors or specialized login failures
                    appendLog("Server Response: " + status);
                }
            } 
            // Case B: Response is a simple Object (typically a String message)
            else if (message != null) {
                String response = message.toString();

                // Handle the outcome of a username recovery attempt
                if (response.equals("RESET_USERNAME_SUCCESS")) {
                    appendLog("SUCCESS: Your username has been updated successfully!");
                    appendLog("Instruction: Please click 'Cancel' to return to the login screen and use your new username.");
                    
                    // Clear fields to reset the state for the next user interaction
                    txtForgotContact.clear();
                    txtNewUsername.clear();
                } 
                else {
                    // Log general server-side errors or informational messages
                    appendLog("Server Message: " + response);
                }
            }
        });
    }

    /**
     * Processes requests to recover or update a guest username.
     * Validates contact info and username length constraints before server submission.
     */
    @FXML 
    void clickSubmitForgot(ActionEvent event) {
        String contact = txtForgotContact.getText();
        String newUsername = txtNewUsername.getText();

        // Client-side validation for missing fields
        if (contact.isEmpty() || newUsername.isEmpty()) {
            appendLog("Error: Contact info and New Username are required.");
            return;
        }

        // Database constraint validation: Username must fit within the VARCHAR limit (10)
        if (newUsername.length() > 10) {
            appendLog("Error: New username must be 10 characters or less.");
            return;
        }

        // Prepare the protocol message for recovery
        ArrayList<String> msg = new ArrayList<>();
        msg.add("RESET_OCCASIONAL_USERNAME");
        msg.add(contact);
        msg.add(newUsername);

        if (client != null) {
            appendLog("Requesting username recovery for contact: " + contact);
            client.handleMessageFromClientUI(msg);
        }
    }

    /**
     * Orchestrates the transition to the Occasional Menu dashboard.
     * Injects the persistent session data (client, type, userId) into the next controller.
     * @param userId The database-generated ID for the current guest session.
     */
    private void navigateToMenu(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml"));
            Parent root = loader.load();
            
            // Dependency Injection: Setup the menu controller with session state
            ((OccasionalMenuController)loader.getController()).setClient(client, "Occasional", userId);
            
            // Standard JavaFX Stage update sequence
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            stage.setTitle("Guest Dashboard");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) { 
            e.printStackTrace();
            appendLog("Navigation Error: " + e.getMessage());
        }
    }
    
    private void navigateToTerminal(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(
            		getClass().getResource("/clientGUI/fxmlFiles/Terminal/TerminalMenuFrame.fxml")
            );
            Parent root = loader.load();

            // Dependency Injection – טרמינל
            TerminalMenuController controller = loader.getController();
            controller.setClient(client);
            // אם בעתיד תצטרך:
            // controller.setUserId(userId);

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm()
            );

            stage.setTitle("Customer Service Terminal");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            appendLog("Error navigating to Terminal menu.");
        }
    }


    /**
     * UI Logic: Toggles the visible area to show the 'Forgot Username' form.
     * Uses setManaged(false) to ensure the hidden pane doesn't occupy layout space.
     */
    @FXML void showForgotArea(ActionEvent event) {
        paneLogin.setVisible(false); paneLogin.setManaged(false);
        paneForgot.setVisible(true); paneForgot.setManaged(true);
    }

    /**
     * UI Logic: Toggles the visible area back to the 'Login' form.
     */
    @FXML void hideForgotArea(ActionEvent event) {
        paneForgot.setVisible(false); paneForgot.setManaged(false);
        paneLogin.setVisible(true); paneLogin.setManaged(true);
    }

    /**
     * Navigates the user to the Registration screen to create a new guest account.
     */
    @FXML void clickRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/OccasionalFXML/OccasionalRegistrationFrame.fxml"));
            Parent root = loader.load();
            ((OccasionalRegistrationController)loader.getController()).setClient(client);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Returns the user to the main Bistro Portal selection screen.
     */
    @FXML void clickBack(ActionEvent event) {
        try {
            FXMLLoader loader;
            Parent root;

            if (loginSource == LoginSource.TERMINAL) {
                loader = new FXMLLoader(
                    getClass().getResource("/clientGUI/fxmlFiles/TerminalLoginFrame.fxml")
                );
                root = loader.load();
                TerminalLoginController controller = loader.getController();
                controller.setClient(client);

            } else {
                loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml"));
                root = loader.load();

                ((RemoteLoginController) loader.getController())
                        .setClient(client);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Thread-safe logging utility for the UI console.
     * Ensures updates happen on the Application thread even if called from network threads.
     * @param message The text to display in the log.
     */
    public void appendLog(String message) { 
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n")); 
    }
}