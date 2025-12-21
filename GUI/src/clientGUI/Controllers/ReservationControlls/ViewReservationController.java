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

public class ViewReservationController implements ChatIF {

    private ChatClient client;
    private String userType;

    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, Long> colCode;
    @FXML private TableColumn<Reservation, String> colDate, colTime;
    @FXML private TableColumn<Reservation, Integer> colGuests;
    @FXML private TextArea txtLog;

    public void setClient(ChatClient client, String userType) {
        this.client = client;
        this.userType = userType;
        setupTable();
        requestUnpaidReservations();
    }

    private void setupTable() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateString")); 
        colTime.setCellValueFactory(new PropertyValueFactory<>("timeString"));
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
    }

    private void requestUnpaidReservations() {
        appendLog("System: Requesting your unpaid orders...");
        ArrayList<String> message = new ArrayList<>();
        message.add("GET_UNPAID_RESERVATIONS");
        client.handleMessageFromClientUI(message);
    }

    @FXML
    void clickPayNow(ActionEvent event) {
        Reservation selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            appendLog("Error: Select an order first.");
            return;
        }

        ArrayList<String> message = new ArrayList<>();
        message.add("PAY_RESERVATION");
        message.add(String.valueOf(selected.getConfirmationCode()));

        appendLog("System: Processing payment...");
        client.handleMessageFromClientUI(message);
    }

    @FXML
    void clickBack(ActionEvent event) {
        String fxmlPath = userType.equals("Subscriber") ? 
            "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml" : 
            "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml";
        navigateToMenu(event, fxmlPath);
    }

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
            // תיקון נתיב CSS
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); 
            appendLog("Error: " + e.getMessage());
        }
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
}