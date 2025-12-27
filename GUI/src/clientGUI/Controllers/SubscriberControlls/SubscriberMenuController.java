package clientGUI.Controllers.SubscriberControlls;

import java.io.IOException;

import client.ChatClient;
import clientGUI.Controllers.ICustomerActions;
import clientGUI.Controllers.RemoteLoginController;
import clientGUI.Controllers.MenuControlls.BaseMenuController;
import common.ChatIF;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * The SubscriberMenuController acts as the primary dashboard for authenticated restaurant subscribers.
 * It provides a centralized hub for all subscriber-specific actions, such as managing reservations, 
 * viewing history, and profile editing.
 * * <p>Architectural Highlights:
 * 1. Extends {@link BaseMenuController} to inherit session state (client, userType, userId).
 * 2. Implements {@link ICustomerActions} to utilize shared navigation logic for the reservation flow.
 * 3. Implements {@link ChatIF} to handle asynchronous server notifications directly on the dashboard.
 * </p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public class SubscriberMenuController extends BaseMenuController implements ChatIF, ICustomerActions {

    /** Navigation buttons for various system modules. */
    @FXML private Button btnNewRes, btnCancelRes, btnViewRes, btnExitWait, btnHistory, btnEditProfile, btnLogout;
    
    /** Logger area for displaying session updates and background operation feedback. */
    @FXML private TextArea txtLog;

    // --- Reservation Action Handlers ---
    // These methods delegate the navigation logic to the default implementations in ICustomerActions.

    @FXML void clickNewRes(ActionEvent event) { 
        createNewReservation(client, event, userType, userId); 
    }
    
    @FXML void clickCancelRes(ActionEvent event) { 
        cancelReservation(client, event, userType, userId); 
    }
    
    @FXML void clickViewRes(ActionEvent event) { 
        viewReservation(client, event, userType, userId); 
    }
    
    // --- Functional Module Placeholders ---

    @FXML void clickExitWait(ActionEvent event) { 
        appendLog("Exit Waiting List triggered."); 
    }
    
    @FXML void clickHistory(ActionEvent event) { 
        viewOrderHistory(client, userId); 
    }
    
    @FXML void clickEditProfile(ActionEvent event) { 
        editPersonalDetails(client, userId); 
    }

    /**
     * Implementation of the order history logic.
     * @param client The active OCSF network client.
     * @param userId The specific subscriber's database ID.
     */
    @Override public void viewOrderHistory(ChatClient client, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientGUI/fxmlFiles/SubscriberFXML/ReservationsHistoryFrame.fxml")
            );
            Parent root = loader.load();

            ReservationHistoryController controller = loader.getController();
            controller.setClient(client);
            controller.loadReservationsForUser(userId);
            
            Stage stage = new Stage();
            stage.setTitle("Order History");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
        appendLog("History logic for user: " + userId); 
    }
    
    /**
     * Implementation of the profile management logic.
     * @param client The active OCSF network client.
     * @param userId The specific subscriber's database ID.
     */
    @Override public void editPersonalDetails(ChatClient client, int userId) { 
        appendLog("Settings for user: " + userId); 
    }

    /**
     * Handles the logout process by terminating the current session and 
     * returning the user to the Remote Login screen.
     * * @param event The ActionEvent used to identify the current Stage for scene transition.
     */
    @FXML
    void clickLogout(ActionEvent event) {
        try {
            // Load the main login portal FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml"));
            Parent root = loader.load();
            
            // Re-inject the client into the login controller to allow for new login attempts
            ((RemoteLoginController)loader.getController()).setClient(client);
            
            // Perform the Stage transition
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { 
            e.printStackTrace(); 
            appendLog("Error during logout: " + e.getMessage());
        }
    }

    /**
     * Interface Implementation: Processes incoming OCSF server messages.
     * Updates the UI Log asynchronously.
     * * @param message The data object received from the server.
     */
    @Override
    public void display(Object message) {
        if (message != null) {
            appendLog(message.toString());
        }
    }

    /**
     * Appends a timestamped or formatted message to the UI Logger.
     * Uses Platform.runLater to ensure thread safety when updating the JavaFX TextArea
     * from a background network thread.
     * * @param message The text to be displayed in the dashboard log.
     */
    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}