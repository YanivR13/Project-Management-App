package clientGUI.Controllers.OccasionalControlls;

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
 * Boundary class for Occasional (Guest) customers.
 * Provides access only to basic restaurant features.
 */
public class OccasionalMenuController implements ChatIF, ICustomerActions {

    private ChatClient client;

    @FXML private Button btnNewReservation;
    @FXML private Button btnCancelReservation;
    @FXML private Button btnViewReservation;
    @FXML private Button btnExitWaitingList;
    @FXML private Button btnLogout;
    @FXML private TextArea txtLog;

    public void setClient(ChatClient client) {
        this.client = client;
    }

    @FXML
    void clickNewReservation(ActionEvent event) {
        createNewReservation(client, event, "Occasional");
    }

    @FXML
    void clickCancelReservation(ActionEvent event) {
        appendLog("Requesting cancellation module...");
        cancelReservation(client, "GUEST_TEMP_ID");
    }

    @FXML
    void clickViewReservation(ActionEvent event) {
        appendLog("Fetching reservation data...");
        viewReservation(client, "GUEST_TEMP_ID");
    }

    @FXML
    void clickExitWaitingList(ActionEvent event) {
        appendLog("Processing removal from waiting list...");
        exitWaitingList(client, "GUEST_TEMP_ID");
    }

    @FXML
    void clickLogout(ActionEvent event) {
        navigateToPortal(event);
    }

    @Override public void viewOrderHistory(ChatClient client) {}
    @Override public void editPersonalDetails(ChatClient client) {}

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
            appendLog("Navigation Error: " + e.getMessage());
        }
    }
}