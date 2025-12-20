package clientGUI.Controllers.ReservationControlls;

import java.time.LocalDate;
import java.util.ArrayList;
import client.ChatClient;
import common.ChatIF;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for the New Reservation screen.
 * Handles availability checks and initial reservation data entry.
 */
public class NewReservationController implements ChatIF {

    private ChatClient client;
    private String userType;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboTime;
    @FXML private TextField txtGuests;
    @FXML private TextArea txtLog;

    /**
     * Sets up the client and initializes UI components.
     */
    public void setClient(ChatClient client, String userType) {
        this.client = client;
        this.userType = userType;
        initTimeSlots();
    }

    /**
     * Populates the time slot dropdown.
     */
    private void initTimeSlots() {
        comboTime.setItems(FXCollections.observableArrayList("12:00", "13:00", "14:00", "19:00", "20:00", "21:00"));
    }

    /**
     * Validates input and sends availability check to the server.
     */
    @FXML
    void clickCheckAvailability(ActionEvent event) {
        LocalDate date = datePicker.getValue();
        String time = comboTime.getValue();
        String guests = txtGuests.getText();

        if (date == null || time == null || guests.isEmpty()) {
            appendLog("Error: All fields are mandatory.");
            return;
        }

        ArrayList<String> message = new ArrayList<>();
        message.add("CHECK_AVAILABILITY");
        message.add(date.toString());
        message.add(time);
        message.add(guests);

        appendLog("Checking availability for " + guests + " diners on " + date);
        client.handleMessageFromClientUI(message);
    }

    /**
     * Navigates back to the main menu.
     */
    @FXML
    void clickBack(ActionEvent event) {
        String fxmlPath = userType.equals("Subscriber") ? 
            "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml" : 
            "/clientGUI/fxmlFiles/OccasinalFXML/OccasionalMenuFrame.fxml";
        navigateTo(event, fxmlPath);
    }

    /**
     * Scene switching logic for returning to menus.
     */
    private void navigateTo(ActionEvent event, String path) {
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
            e.printStackTrace();
        }
    }

    /**
     * Implements ChatIF to display server responses.
     */
    @Override
    public void display(Object message) {
        if (message != null) {
            appendLog(message.toString());
        }
    }

    /**
     * Safe UI update for log messages.
     */
    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}