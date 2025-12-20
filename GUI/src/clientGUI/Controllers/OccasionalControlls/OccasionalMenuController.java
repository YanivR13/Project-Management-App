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
 * Provides access only to basic restaurant features using shared navigation logic.
 */
public class OccasionalMenuController implements ChatIF, ICustomerActions {

    private ChatClient client;

    @FXML private Button btnNewReservation;
    @FXML private Button btnCancelReservation;
    @FXML private Button btnViewReservation;
    @FXML private Button btnExitWaitingList;
    @FXML private Button btnLogout;
    @FXML private TextArea txtLog;

    /**
     * Injects the client instance and ensures connectivity.
     */
    public void setClient(ChatClient client) {
        this.client = client;
    }

    // --- Actions using shared logic from ICustomerActions ---

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

    // --- Subscriber-only methods (Empty implementation for Guest) ---

    @Override public void viewOrderHistory(ChatClient client) { /* Not available for Guest */ }
    @Override public void editPersonalDetails(ChatClient client) { /* Not available for Guest */ }

    // --- Navigation & Log Logic ---

    @FXML
    void clickLogout(ActionEvent event) {
        appendLog("Logging out...");
        navigateToPortal(event);
    }

    /**
     * Implementation of ChatIF. Receives updates from the server.
     * Updated to handle Object type as required by the new interface.
     */
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
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/style.css").toExternalForm());
            stage.setTitle("Bistro - Remote Access Portal");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            // 1. מדפיס את כל ה-Stack Trace ל-Console (טקסט אדום מפורט)
            e.printStackTrace(); 
            
            // 2. מציג הודעה קצרה למשתמש על גבי ה-UI (ב-TextArea)
            appendLog("Error: " + e.getMessage());
        }
    }
}