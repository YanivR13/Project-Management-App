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
 * Implements shared logic and handles exclusive member-only requests.
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

    public void setClient(ChatClient client) {
        this.client = client;
    }

    @FXML
    void clickNewRes(ActionEvent event) {
        appendLog("Launching Reservation Wizard...");
        createNewReservation(client);
    }

    @FXML
    void clickCancelRes(ActionEvent event) {
        appendLog("Accessing cancellation service...");
        cancelReservation(client, "SUB_ACTIVE_ID");
    }

    @FXML
    void clickViewRes(ActionEvent event) {
        appendLog("Loading reservation details...");
        viewReservation(client, "SUB_ACTIVE_ID");
    }

    @FXML
    void clickExitWait(ActionEvent event) {
        appendLog("Exiting waiting list...");
        exitWaitingList(client, "SUB_ACTIVE_ID");
    }

    @Override
    @FXML
    public void viewOrderHistory(ChatClient client) {
        appendLog("Fetching subscriber history...");
        ArrayList<String> message = new ArrayList<>();
        message.add("GET_SUBSCRIBER_HISTORY");
        client.handleMessageFromClientUI(message);
    }

    @Override
    @FXML
    public void editPersonalDetails(ChatClient client) {
        appendLog("Navigating to Profile Settings...");
    }

    @FXML
    void clickHistory(ActionEvent event) {
        viewOrderHistory(client);
    }

    @FXML
    void clickEditProfile(ActionEvent event) {
        editPersonalDetails(client);
    }

    @FXML
    void clickLogout(ActionEvent event) {
        navigateToPortal(event);
    }

    @Override
    public void display(String message) {
        appendLog(message);
    }

    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }

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
            appendLog("Error during logout: " + e.getMessage());
        }
    }
}