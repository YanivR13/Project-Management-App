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
        appendLog("Navigating to New Reservation...");
        createNewReservation(client, event, "Occasional");
    }

    @FXML
    void clickCancelReservation(ActionEvent event) {
        appendLog("Opening Cancellation Module...");
        cancelReservation(client, event, "Occasional");
    }

    @FXML
    void clickViewReservation(ActionEvent event) {
        appendLog("Opening View & Pay Module...");
        viewReservation(client, event, "Occasional");
    }

    @FXML
    void clickExitWaitingList(ActionEvent event) {
        appendLog("Sending request to exit waiting list...");
        exitWaitingList(client, "GUEST_TEMP_ID");
    }

    @Override public void viewOrderHistory(ChatClient client) { }
    @Override public void editPersonalDetails(ChatClient client) { }

    @FXML
    void clickLogout(ActionEvent event) {
        appendLog("Logging out...");
        navigateToPortal(event);
    }

    @Override
    public void display(Object message) {
        if (message != null) {
            appendLog(message.toString());
        }
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
            // תיקון נתיב CSS
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            stage.setTitle("Bistro - Remote Access Portal");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); 
            appendLog("Error: " + e.getMessage());
        }
    }
}