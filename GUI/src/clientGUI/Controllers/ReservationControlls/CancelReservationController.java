package clientGUI.Controllers.ReservationControlls;

import java.util.ArrayList;
import client.ChatClient;
import common.ChatIF;
import common.Reservation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Controller for the shared Cancellation screen.
 * Displays active reservations in a table and handles cancellation requests.
 */
public class CancelReservationController implements ChatIF {

    private ChatClient client;
    private String userType; // Distinguishes between Subscriber and Occasional guest

    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, Long> colID;
    @FXML private TableColumn<Reservation, String> colDate, colTime;
    @FXML private TableColumn<Reservation, Integer> colGuests;
    @FXML private TextArea txtLog;

    /**
     * Initializes the controller with necessary dependencies.
     */
    public void setClient(ChatClient client, String userType) {
        this.client = client;
        this.userType = userType;
        setupTableColumns();
        fetchActiveReservations(); 
    }

    /**
     * Maps TableView columns to Reservation property names/getters.
     */
    private void setupTableColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateString"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("timeString"));
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
    }

    /**
     * Requests active reservations from the server to populate the table.
     */
    private void fetchActiveReservations() {
        appendLog("System: Fetching your active reservations...");
        ArrayList<String> message = new ArrayList<>();
        message.add("GET_ACTIVE_RESERVATIONS");
        client.handleMessageFromClientUI(message);
    }

    /**
     * Handles the cancellation of the selected reservation in the table.
     */
    @FXML
    void clickCancelSelected(ActionEvent event) {
        Reservation selected = tableReservations.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            appendLog("Error: No reservation selected. Please pick an order from the table.");
            return;
        }

        ArrayList<String> message = new ArrayList<>();
        message.add("CANCEL_RESERVATION");
        message.add(String.valueOf(selected.getConfirmationCode()));

        appendLog("Requesting cancellation for Order ID: " + selected.getConfirmationCode());
        client.handleMessageFromClientUI(message);
    }

    /**
     * Navigates back to the appropriate menu based on the user type.
     */
    @FXML
    void clickBack(ActionEvent event) {
        String fxmlPath = userType.equals("Subscriber") ? 
            "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml" : 
            "/clientGUI/fxmlFiles/OccasinalFXML/OccasionalMenuFrame.fxml";
        navigateToMenu(event, fxmlPath);
    }

    /**
     * Helper method to load the previous menu and re-inject the client.
     */
    private void navigateToMenu(ActionEvent event, String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Object controller = loader.getController();
            
            if (controller instanceof clientGUI.Controllers.SubscriberControlls.SubscriberMenuController) {
                ((clientGUI.Controllers.SubscriberControlls.SubscriberMenuController) controller).setClient(client);
            } else {
                ((clientGUI.Controllers.OccasionalControlls.OccasionalMenuController) controller).setClient(client);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            // 1. מדפיס את כל ה-Stack Trace ל-Console (טקסט אדום מפורט)
            e.printStackTrace(); 
            
            // 2. מציג הודעה קצרה למשתמש על גבי ה-UI (ב-TextArea)
            appendLog("Error: " + e.getMessage());
        }
    }

    /**
     * Implements ChatIF to display server messages.
     */
    @Override
    public void display(Object message) {
        if (message != null) {
            appendLog(message.toString());
        }
    }

    /**
     * Appends text to the GUI log area in a thread-safe manner.
     */
    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}