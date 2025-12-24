package clientGUI.Controllers;

import client.ChatClient;
import clientGUI.Controllers.OccasionalControlls.OccasionalLoginController;
import clientGUI.Controllers.SubscriberControlls.SubscriberLoginController;
import javafx.event.ActionEvent;
import common.ChatIF;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.Node;

/**
 * The RemoteLoginController handles the primary navigation logic for the landing screen.
 * It serves as a gateway, allowing users to choose between Subscriber and Occasional login paths.
 * * <p>Key Responsibilities:
 * 1. Handling initial UI interactions and scene transitions.
 * 2. Managing the lifecycle of the {@link ChatClient} and passing it to subsequent controllers.
 * 3. Logging system-level connection messages to the UI.
 * </p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public class RemoteLoginController implements ChatIF {
    
    /** The network client instance used for server communication throughout the session. */
    private ChatClient client;
    
    /** Logger area for displaying connection status and error messages to the user. */
    @FXML private TextArea txtLog;

    /**
     * Injects the persistent ChatClient instance into this controller.
     * @param client The active network client.
     */
    public void setClient(ChatClient client) { 
        this.client = client; 
    }

    /**
     * Event handler for the 'Occasional Guest' button.
     * Navigates the user to the guest login/registration portal.
     * * @param event The action event triggered by the button click.
     */
    @FXML
    void clickOccasional(ActionEvent event) {
        loadScreen(event, "OccasionalFXML/OccasionalLoginFrame.fxml", "Occasional Login");
    }

    /**
     * Event handler for the 'Subscriber Login' button.
     * Navigates the user to the registered subscriber portal.
     * * @param event The action event triggered by the button click.
     */
    @FXML
    void clickSubscriber(ActionEvent event) {
        loadScreen(event, "SubscriberFXML/SubscriberLoginFrame.fxml", "Subscriber Login");
    }

    /**
     * Core Navigation Engine: Dynamically loads and displays FXML-defined scenes.
     * This method handles the complexity of FXML loading, CSS application, 
     * and Controller Dependency Injection.
     * * @param event    The event source used to identify the current window (Stage).
     * @param fxmlFile The relative path to the FXML layout file.
     * @param title    The title to be displayed on the new window stage.
     */
    private void loadScreen(ActionEvent event, String fxmlFile, String title) {
        try {
            // Initialize the FXML Loader for the requested screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/" + fxmlFile));
            Parent root = loader.load();
            
            // Retrieve the controller instance associated with the loaded FXML
            Object controller = loader.getController();
            
            /**
             * Dependency Injection Phase:
             * We determine the controller type at runtime and inject the shared ChatClient.
             * This ensures the network connection remains persistent across different frames.
             */
            if (controller instanceof SubscriberLoginController) {
                ((SubscriberLoginController) controller).setClient(client);
            } else if (controller instanceof OccasionalLoginController) {
                ((OccasionalLoginController) controller).setClient(client);
            }

            // Stage and Scene Configuration
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            
            // Apply global CSS styling for visual consistency
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            appendLog("Error loading screen: " + e.getMessage());
        }
    }

    /**
     * Interface Implementation: Receives and logs messages from the server.
     * @param message The incoming object from the server.
     */
    @Override 
    public void display(Object message) { 
        if (message != null) appendLog(message.toString()); 
    }

    /**
     * Thread-safe logging method.
     * Since network messages arrive on a background thread, updates to the UI
     * components (TextArea) must be delegated to the JavaFX Application Thread.
     * * @param message The string message to append to the log.
     */
    public void appendLog(String message) { 
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n")); 
    }
}