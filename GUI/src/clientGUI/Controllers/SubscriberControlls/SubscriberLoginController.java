package clientGUI.Controllers.SubscriberControlls;

import java.util.ArrayList;
import client.ChatClient;
import clientGUI.Controllers.RemoteLoginController;
import common.ChatIF;
import commonLogin.LoginSource;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import terminalGUI.Controllers.TerminalControllers.TerminalMenuController;
import javafx.application.Platform;

/**
 * Controller class for the Subscriber Login interface.
 * This class manages the user interactions for registered subscribers attempting
 * to access their personal dashboard. It handles input validation, communication 
 * with the OCSF ChatClient, and navigation upon successful authentication.
 * * <p>Implements {@link ChatIF} to allow asynchronous server responses to be 
 * processed and displayed in the UI.</p>
 */
public class SubscriberLoginController implements ChatIF {
    
    /** The persistent client connection used to send and receive server messages. */
    private ChatClient client;
    
    private LoginSource loginSource = LoginSource.REMOTE;
    
    /** Input field for the unique Subscriber Identification number. */
    @FXML private TextField txtSubscriberID;
    
    /** Button triggered to initiate the login process. */
    @FXML private Button btnLogin;
    
    /** Log area for displaying status updates, errors, and server feedback. */
    @FXML private TextArea txtLog;

    /**
     * Injects the ChatClient instance and sets this controller as the active UI listener.
     * * @param client The active OCSF network client.
     */
    public void setClient(ChatClient client) {
        this.client = client;
        if (client != null) {
            // Establishes the link between the network layer and this specific UI controller
            client.setUI(this);
            
            // Provides initial visual feedback to the user that the portal is ready
            appendLog("Connected to Portal. Waiting for login...");
        }
    }
    
    public void setLoginSource(LoginSource source) {
        this.loginSource = source;
    }

    /**
     * Handles the 'Login' button click event.
     * Performs client-side validation before encapsulating the subscriber ID into 
     * a protocol message for the server.
     * * @param event The action event triggered by the user.
     */
    @FXML
    void clickLogin(ActionEvent event) {
        String subID = txtSubscriberID.getText();
        
        // Layer 1: Basic validation to ensure the field is not empty
        if (subID.isEmpty()) {
            appendLog("Error: Please enter a Subscriber ID.");
            return;
        }

        // Layer 2: Constructing the OCSF protocol message (ArrayList format)
        ArrayList<String> msg = new ArrayList<>();
        msg.add("LOGIN_SUBSCRIBER"); // Command header
        msg.add(subID);              // Payload (ID)

        // Layer 3: Physical transmission via the OCSF AbstractClient logic
        if (client != null) {
            appendLog("Attempting login for ID: " + subID);
            client.handleMessageFromClientUI(msg);
        } else {
            // Defensive check in case the connection was dropped before the attempt
            appendLog("Fatal Error: No server connection!");
        }
    }

    /**
     * Handles navigation back to the main Remote Login portal.
     * * @param event The action event used to identify the current Stage.
     */
    @FXML
    void clickBack(ActionEvent event) {
        try {
            // Loading the FXML for the landing screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml"));
            Parent root = loader.load();
            
            // Passing the client reference back to the previous controller to maintain session
            ((RemoteLoginController)loader.getController()).setClient(client);
            
            // Scene switching logic
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    /**
     * HOOK METHOD: Processes incoming messages from the Server.
     * This method is triggered by the ChatClient whenever the server sends data.
     * * @param message The object received from the server (expected to be an ArrayList).
     */
    @Override
    @SuppressWarnings("unchecked") // Handles the yellow warning for unchecked casting of ArrayList
    public void display(Object message) {
        // Validation: Ensure the message follows the expected protocol structure
        if (message instanceof ArrayList) {
            ArrayList<Object> res = (ArrayList<Object>) message;
            String status = res.get(0).toString(); // The first index always contains the status/command

            // Successful authentication logic
            if (status.equals("LOGIN_SUCCESS")) {
                appendLog("Login confirmed! Loading dashboard...");
                
                int userId = (int) res.get(1);
                // Navigation must happen on the JavaFX thread using Platform.runLater
                Platform.runLater(() -> {
                    if (loginSource == LoginSource.TERMINAL) {
                        navigateToTerminal(userId);
                    } else {
                        navigateToMenu(userId);
                    }
                });
    
            }else {
                // Display specific server-side error messages (e.g., "ID not found")
                appendLog("Server Response: " + res.toString());
            }
        } else if (message != null) {
            // Fallback for simple string-based server messages
            appendLog(message.toString());
        }
    }

    /**
     * Transitions the UI to the Subscriber Menu upon successful login.
     * * @param userId The unique internal user ID retrieved from the Database.
     */
    private void navigateToMenu(int userId) {
        try {
            // Initialize the menu screen loader
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml"));
            Parent root = loader.load();
            
            // Dependency Injection: Passing session data to the next controller
            ((SubscriberMenuController)loader.getController()).setClient(client, "Subscriber", userId);
            
            // Finalize stage update
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { 
            e.printStackTrace();
            appendLog("UI Error: Could not load Menu Frame.");
        }
    }
    
    private void navigateToTerminal(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(
            		getClass().getResource("/clientGUI/fxmlFiles/Terminal/TerminalMenuFrame.fxml")
            );
            Parent root = loader.load();

            // Dependency Injection
            TerminalMenuController controller = loader.getController();
            controller.setClient(client);

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
            appendLog("Terminal navigation error.");
        }
    }


    /**
     * Appends text to the GUI's log area in a thread-safe manner.
     * Since network events occur on a background thread, this method uses 
     * Platform.runLater to ensure JavaFX UI updates are performed correctly.
     * * @param message The text string to append to the log.
     */
    public void appendLog(String message) { 
        Platform.runLater(() -> {
            if (txtLog != null) {
                // Standard UI logging
                txtLog.appendText("> " + message + "\n");
            } else {
                // Console fallback if the UI component is not yet initialized
                System.out.println("CONSOLE LOG (txtLog is null): " + message);
            }
        });
    }
}