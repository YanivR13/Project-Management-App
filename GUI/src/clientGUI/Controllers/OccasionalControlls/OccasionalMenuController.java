package clientGUI.Controllers.OccasionalControlls;

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
 * The OccasionalMenuController serves as the main dashboard for Guest (Occasional) users.
 * It provides access to core restaurant services like making, viewing, or canceling 
 * reservations without requiring a full subscription.
 * * <p>Architectural Strategy:
 * 1. Extends {@link BaseMenuController} to inherit shared session state (client, userId, userType).
 * 2. Implements {@link ICustomerActions} to leverage centralized navigation logic for reservation flows.
 * 3. Implements {@link ChatIF} to allow the server to push status updates directly to the guest log.
 * </p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public class OccasionalMenuController extends BaseMenuController implements ChatIF, ICustomerActions {

    /** Navigation buttons for Guest-accessible features. */
    @FXML private Button btnNewReservation, btnCancelReservation, btnViewReservation, btnLogout, btnExitWaitingList;
    
    /** Console-style log area for providing real-time feedback to the guest user. */
    @FXML private TextArea txtLog;

    // --- Core Navigation Methods ---
    // These methods call default implementations in ICustomerActions to reduce code duplication.

    /** Triggers the New Reservation scene transition. */
    @FXML void clickNewReservation(ActionEvent event) { 
        createNewReservation(client, event, userType, userId); 
    }
    
    /** Triggers the Reservation Cancellation scene transition. */
    @FXML void clickCancelReservation(ActionEvent event) { 
        cancelReservation(client, event, userType, userId); 
    }
    
    /** Triggers the Reservation Viewing/Payment scene transition. */
    @FXML void clickViewReservation(ActionEvent event) { 
        viewReservation(client, event, userType, userId); 
    }

    /** Placeholder for future Waiting List management features. */
    @FXML
    void clickExitWaitingList(ActionEvent event) {
        appendLog("Navigating to Waiting List management...");
    }

    /**
     * Handles the logout process. Returns the user to the initial portal selection screen
     * and ensures the persistent ChatClient is passed back correctly to maintain connection.
     * * @param event The ActionEvent from the 'Logout' button.
     */
    @FXML
    void clickLogout(ActionEvent event) {
        try {
            // Load the main remote access landing page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml"));
            Parent root = loader.load();
            
            // Re-inject the client into the next controller to maintain the OCSF socket
            ((RemoteLoginController)loader.getController()).setClient(client);
            
            // Update the Primary Stage with the new Scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { 
            e.printStackTrace(); 
            appendLog("Logout Error: Unable to return to portal.");
        }
    }

    /**
     * Implementation of ICustomerActions. 
     * Note: Occasional guests may have restricted access to history compared to Subscribers.
     */
    @Override public void viewOrderHistory(ChatClient client, int userId) { }
    
    /**
     * Implementation of ICustomerActions.
     * Profile editing for guests is typically handled during the 'Update Username' flow.
     */
    @Override public void editPersonalDetails(ChatClient client, int userId) { }

    /**
     * HOOK METHOD (ChatIF): Receives and processes data objects pushed by the Server.
     * @param message The object sent by the server via OCSF.
     */
    @Override
    public void display(Object message) {
        if (message != null) {
            appendLog(message.toString());
        }
    }

    /**
     * Updates the UI log area in a thread-safe manner.
     * Crucial because OCSF handleMessageFromServer calls occur on a background thread,
     * while JavaFX UI updates must happen on the Application Thread.
     * * @param message The text to display in the UI log.
     */
    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}