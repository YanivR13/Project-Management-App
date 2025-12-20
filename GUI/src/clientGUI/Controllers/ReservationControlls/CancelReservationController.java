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
    private String userType; // Remembers if the user is a Subscriber or Occasional guest

    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, String> colID, colDate, colTime, colGuests;
    @FXML private TextArea txtLog;

    /**
     * Injects dependencies and initializes the table.
     */
    public void setClient(ChatClient client, String userType) {
        this.client = client;
        this.userType = userType;
        setupTableColumns();
        fetchActiveReservations(); 
    }

    /**
     * Links TableView columns to the Reservation object properties.
     */
    private void setupTableColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colGuests.setCellValueFactory(new PropertyValueFactory<>("guests"));
    }

    /**
     * Sends a request to the server to get all reservations with status 'ACTIVE'.
     */
    private void fetchActiveReservations() {
        appendLog("System: Fetching your active reservations...");
        ArrayList<String> message = new ArrayList<>();
        message.add("GET_ACTIVE_RESERVATIONS");
        // In the future, logic to filter by specific User/Phone will go here
        client.handleMessageFromClientUI(message);
    }

    /**
     * Triggered when the user clicks 'Cancel Selected'.
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
        message.add(selected.getId());

        appendLog("Requesting cancellation for Order ID: " + selected.getId());
        client.handleMessageFromClientUI(message);
    }

    /**
     * Navigates back to the correct menu based on userType.
     */
    @FXML
    void clickBack(ActionEvent event) {
        String fxmlPath = userType.equals("Subscriber") ? 
            "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml" : 
            "/clientGUI/fxmlFiles/OccasinalFXML/OccasionalMenuFrame.fxml";
        navigateToMenu(event, fxmlPath);
    }

    private void navigateToMenu(ActionEvent event, String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Object controller = loader.getController();
            
            // Re-inject the client into the returning menu controller
            if (controller instanceof clientGUI.Controllers.SubscriberControlls.SubscriberMenuController) {
                ((clientGUI.Controllers.SubscriberControlls.SubscriberMenuController) controller).setClient(client);
            } else {
                ((clientGUI.Controllers.OccasionalControlls.OccasionalMenuController) controller).setClient(client);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void display(String message) {
        appendLog(message);
    }

    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}