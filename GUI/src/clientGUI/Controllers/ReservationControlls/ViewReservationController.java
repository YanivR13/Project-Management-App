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
 * Controller for the View & Pay screen.
 * Allows users to view active unpaid reservations and process payments.
 */
public class ViewReservationController implements ChatIF {

    private ChatClient client;
    private String userType;

    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, Long> colCode;
    @FXML private TableColumn<Reservation, String> colDate, colTime;
    @FXML private TableColumn<Reservation, Integer> colGuests;
    @FXML private TextArea txtLog;

    /**
     * Initializes the UI and requests data from the server.
     */
    public void setClient(ChatClient client, String userType) {
        this.client = client;
        this.userType = userType;
        setupTable();
        requestUnpaidReservations();
    }

    /**
     * Maps table columns to Reservation entity getters.
     */
    private void setupTable() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateString")); 
        colTime.setCellValueFactory(new PropertyValueFactory<>("timeString"));
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
    }

    /**
     * Asks the server for all active reservations with unpaid status.
     */
    private void requestUnpaidReservations() {
        appendLog("System: Requesting your unpaid active orders...");
        ArrayList<String> message = new ArrayList<>();
        message.add("GET_UNPAID_RESERVATIONS");
        client.handleMessageFromClientUI(message);
    }

    /**
     * Sends a payment command for the selected reservation.
     */
    @FXML
    void clickPayNow(ActionEvent event) {
        Reservation selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            appendLog("Error: Please select an order from the table first.");
            return;
        }

        ArrayList<String> message = new ArrayList<>();
        message.add("PAY_RESERVATION");
        message.add(String.valueOf(selected.getConfirmationCode()));

        appendLog("System: Processing payment for code: " + selected.getConfirmationCode());
        client.handleMessageFromClientUI(message);
    }

    /**
     * Returns the user to the dashboard.
     */
    @FXML
    void clickBack(ActionEvent event) {
        String fxmlPath = userType.equals("Subscriber") ? 
            "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml" : 
            "/clientGUI/fxmlFiles/OccasinalFXML/OccasionalMenuFrame.fxml";
        navigateToMenu(event, fxmlPath);
    }

    /**
     * Navigation helper with client re-injection.
     */
    private void navigateToMenu(ActionEvent event, String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            
            if (ctrl instanceof clientGUI.Controllers.SubscriberControlls.SubscriberMenuController) {
                ((clientGUI.Controllers.SubscriberControlls.SubscriberMenuController) ctrl).setClient(client);
            } else {
                ((clientGUI.Controllers.OccasionalControlls.OccasionalMenuController) ctrl).setClient(client);
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
     * Implements ChatIF for server feedback.
     */
    @Override
    public void display(Object message) {
        if (message != null) {
            appendLog(message.toString());
        }
    }

    /**
     * Safe UI logger.
     */
    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}