package clientGUI.Controllers.SubscriberControlls;

import java.util.ArrayList;
import client.ChatClient;
import clientGUI.Controllers.ICustomerActions;
import clientGUI.Controllers.RemoteLoginController;
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
 * Boundary class for Registered Subscribers.
 * Implements shared logic for reservations and handles exclusive member-only requests.
 */
public class SubscriberMenuController implements ChatIF, ICustomerActions {

    private ChatClient client;

    @FXML private Button btnNewRes;
    @FXML private Button btnCancelRes;
    @FXML private Button btnViewRes;
    @FXML private Button btnExitWait;
    @FXML private Button btnHistory;
    @FXML private Button btnEditProfile;
    @FXML private Button btnLogout;
    @FXML private TextArea txtLog;

    /**
     * Injects the client instance into the controller.
     * @param client The active ChatClient for server communication.
     */
    public void setClient(ChatClient client) {
        this.client = client;
    }

    /**
     * Navigates to the shared New Reservation screen.
     */
    @FXML
    void clickNewRes(ActionEvent event) {
        appendLog("Navigating to New Reservation...");
        createNewReservation(client, event, "Subscriber");
    }

    /**
     * Navigates to the shared Cancellation screen with TableView.
     */
    @FXML
    void clickCancelRes(ActionEvent event) {
        appendLog("Accessing cancellation module...");
        // Updated to use shared navigation from ICustomerActions
        cancelReservation(client, event, "Subscriber");
    }

    /**
     * Navigates to the shared View & Pay screen.
     */
    @FXML
    void clickViewRes(ActionEvent event) {
        appendLog("Loading reservation details and payment options...");
        // Updated to use shared navigation from ICustomerActions
        viewReservation(client, event, "Subscriber");
    }

    /**
     * Sends a request to remove the subscriber from a waiting list.
     */
    @FXML
    void clickExitWait(ActionEvent event) {
        appendLog("Processing request to exit waiting list...");
        exitWaitingList(client, "SUB_ACTIVE_ID");
    }

    /**
     * Implementation of subscriber-only action: viewing past visits.
     */
    @Override
    @FXML
    public void viewOrderHistory(ChatClient client) {
        appendLog("Fetching subscriber order history from database...");
        ArrayList<String> message = new ArrayList<>();
        message.add("GET_SUBSCRIBER_HISTORY");
        client.handleMessageFromClientUI(message);
    }

    /**
     * Implementation of subscriber-only action: editing profile details.
     */
    @Override
    @FXML
    public void editPersonalDetails(ChatClient client) {
        appendLog("Navigating to Profile Settings...");
        // Future implementation for profile editing screen
    }

    /**
     * Trigger for the Order History button.
     */
    @FXML
    void clickHistory(ActionEvent event) {
        viewOrderHistory(client);
    }

    /**
     * Trigger for the Edit Profile button.
     */
    @FXML
    void clickEditProfile(ActionEvent event) {
        editPersonalDetails(client);
    }

    /**
     * Handles the logout process and returns to the portal.
     */
    @FXML
    void clickLogout(ActionEvent event) {
        appendLog("Logging out and returning to portal...");
        navigateToPortal(event);
    }

    /**
     * Implementation of ChatIF display. 
     * Handles Object input to support varied server responses.
     */
    @Override
    public void display(Object message) {
        if (message != null) {
            appendLog(message.toString());
        }
    }

    /**
     * Thread-safe method to append messages to the UI log.
     */
    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }

    /**
     * Navigation helper to return to the Remote Access Portal.
     */
    private void navigateToPortal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml"));
            Parent root = loader.load();
            
            RemoteLoginController controller = loader.getController();
            controller.setClient(client);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/style.css").toExternalForm());
            stage.setTitle("Bistro - Remote Access Portal");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            appendLog("Error during navigation: " + e.getMessage());
        }
    }
}