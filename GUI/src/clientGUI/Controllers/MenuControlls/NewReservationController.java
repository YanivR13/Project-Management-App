package clientGUI.Controllers.MenuControlls;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ResourceBundle;
import client.ChatClient;
import common.ChatIF;
import common.Reservation;
import common.Restaurant;
import common.ServiceResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller class for the New Reservation interface.
 * This class manages the user input for booking a table, performs client-side 
 * validation, and handles the asynchronous communication with the server.
 * * <p>It extends {@link BaseMenuController} to maintain session state and 
 * implements {@link ChatIF} for server feedback.</p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public class NewReservationController extends BaseMenuController implements ChatIF, Initializable {

    /** UI components injected from FXML */
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> comboTime;
    @FXML private TextField txtGuests;
    @FXML private TextArea txtLog;
    @FXML private Button btnConfirm;
    @FXML private Button btnBack;
    
    /** Cached restaurant data received from the server for validation and display. */
    private Restaurant currentRestaurant;
    
    /**
     * This runs automatically because it's called by the BaseMenuController
     * immediately after the client is injected.
     */
    @Override
    public void onClientReady() {
        ArrayList<Object> msg = new ArrayList<>();
        msg.add("GET_RESTAURANT_WORKTIMES");
        
        this.client.handleMessageFromClientUI(msg);
        appendLog("Fetching restaurant information...");
    }
   
    /**
     * Initializes the controller, setting up the UI constraints and populating time slots.
     * Called automatically by the FXMLLoader.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Prevents manual typing in the DatePicker to ensure valid date formats
        dpDate.setEditable(false);

        // Customizes the DatePicker to disable and highlight past dates
        dpDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true); // Disable past dates
                    setStyle("-fx-background-color: #ffc0cb;"); // Visual indicator (Pink)
                }
            }
        });

        // Populates the time ComboBox with 30-minute intervals from 09:00 to 22:00
        ObservableList<String> hours = FXCollections.observableArrayList();
        LocalTime time = LocalTime.MIDNIGHT; // 00:00
        do {
            hours.add(time.toString());
            time = time.plusMinutes(30);
        } while (!time.equals(LocalTime.MIDNIGHT)); // 23:30

        comboTime.setItems(hours);
        
        // Sets today as the default selected date
        dpDate.setValue(LocalDate.now());
        appendLog("Ready to take your reservation.");
  
    }

    /**
     * Handles the 'Confirm Reservation' button click.
     * Validates input, constructs a {@link Reservation} DTO, and sends it to the server.
     * * @param event The ActionEvent from the UI.
     */
    @FXML
    void clickConfirm(ActionEvent event) {
        // Step 1: Basic validation for null or empty inputs
        if (dpDate.getValue() == null || comboTime.getValue() == null || txtGuests.getText().isEmpty()) {
            appendLog("Error: Missing input fields.");
            return;
        }

        try {
            // Step 2: Parse and validate guest count
        	String guestsInput = txtGuests.getText().trim();
        	if (!guestsInput.matches("\\d+")) {
                appendLog("Error: Guests field must contain only numbers (no letters or symbols).");
                return;
            }
        	
        	int g = Integer.parseInt(guestsInput);
            if (g <= 0) throw new NumberFormatException();
        	
        	// Step 3: Check that the order is at least an hour from now to a month from now
        	LocalDateTime requestedDT = LocalDateTime.of(dpDate.getValue(), LocalTime.parse(comboTime.getValue()));
        	LocalDateTime now = LocalDateTime.now();
        	
        	if (requestedDT.isBefore(now.plusHours(1))) {
        	    appendLog("Error: Reservations must be made at least 1 hour in advance.");
        	    return;
        	}

        	
        	if (requestedDT.isAfter(now.plusMonths(1))) {
        	    appendLog("Error: Reservations can only be made up to one month in advance.");
        	    return;
        	}

            // Step 4: Format the date and time for SQL compatibility (YYYY-MM-DD HH:mm:ss)
            String dt = dpDate.getValue().toString() + " " + comboTime.getValue() + ":00";
            
            // Step 5: Create the DTO and encapsulate it in a protocol message
            Reservation res = new Reservation(userId, dt, g);
            ArrayList<Object> msg = new ArrayList<>();
            msg.add("CREATE_RESERVATION");
            msg.add(res);

            // Step 6: Transmit to server via OCSF
            if (client != null) {
                client.setUI(this); // Register this screen to receive the server's response
                appendLog("Sending request to server...");
                client.handleMessageFromClientUI(msg);
            }
        } catch (NumberFormatException e) {
            appendLog("Error: Please enter a valid number of guests.");
        }
    }

    /**
     * Navigates back to the relevant menu based on the current user type (Subscriber/Occasional).
     * * @param event The trigger event used to identify the current window.
     */
    @FXML
    void clickBack(ActionEvent event) {
        String fxmlPath;
        
        // Dynamic navigation logic based on session data
        if ("Subscriber".equals(userType)) {
            fxmlPath = "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml";
        } else if ("Occasional".equals(userType)) {
            fxmlPath = "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml";
        } else {
            appendLog("Error: Unknown user type for navigation.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Dependency Injection: Passing session data back to the menu controller
            BaseMenuController controller = loader.getController();
            controller.setClient(client, userType, userId);

            // Scene transition
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            appendLog("Navigation Error: " + e.getMessage());
        }
    }

    /**
     * Processes incoming server messages, specifically {@link ServiceResponse} objects.
     * Uses Platform.runLater to ensure UI updates are thread-safe.
     * * @param message The server's response payload.
     */
    @Override
    public void display(Object message) {
    	
    	if (message instanceof Restaurant) {
            this.currentRestaurant = (Restaurant) message;

            Platform.runLater(() -> {
                appendLog("--- Restaurant Information Loaded ---");
                
                appendLog(currentRestaurant.getFormattedOpeningHours());
            });
            return;
        }
        if (message instanceof ServiceResponse) {
            ServiceResponse sr = (ServiceResponse) message;
            Platform.runLater(() -> {
                // Handling different business scenarios based on server status
                switch (sr.getStatus()) {
                    case RESERVATION_SUCCESS:
                        showPopup(AlertType.INFORMATION, "Success", "Reservation confirmed! Code: " + sr.getData());
                        appendLog("Reservation secured with code: " + sr.getData());
                        break;
                    case RESERVATION_SUGGESTION:
                        // Triggered if the requested slot is full but an alternative is found
                        handleSuggestion(sr.getData().toString());
                        break;
                    case RESERVATION_FULL:
                        showPopup(AlertType.WARNING, "Fully Booked", "Sorry, no tables available.");
                        appendLog("Server: No tables available.");
                        break;
                    case INTERNAL_ERROR:
                        showPopup(AlertType.ERROR, "Server Error", sr.getData().toString());
                        appendLog("Server Error: " + sr.getData());
                        break;
                        
                    case RESERVATION_OUT_OF_HOURS:
                        showPopup(AlertType.WARNING, "Restaurant Closed", sr.getData().toString());
                        appendLog("Server: " + sr.getData());
                        break;
                }
            });
        }
    }

    /**
     * Logic for handling alternative time slot suggestions.
     * Prompts the user to accept or decline the suggested time.
     * * @param suggested The alternative time slot (String) provided by the server.
     */
    private void handleSuggestion(String suggested) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, 
            "Requested time is full. Would you like to reserve for " + suggested + " instead?", 
            ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("Alternative Slot Found");
        a.setHeaderText("No availability for requested time.");
        
        // If the user accepts the suggestion, send a new reservation request immediately
        if (a.showAndWait().get() == ButtonType.OK) {
            try {
                int guests = Integer.parseInt(txtGuests.getText());
                Reservation res = new Reservation(userId, suggested + ":00", guests);
                ArrayList<Object> msg = new ArrayList<>();
                msg.add("CREATE_RESERVATION");
                msg.add(res);
                client.handleMessageFromClientUI(msg);
                appendLog("Attempting to book suggested slot: " + suggested);
            } catch (NumberFormatException e) {
                appendLog("Error reading guest number for suggestion.");
            }
        }
    }

    /**
     * Utility method for displaying JavaFX Alerts.
     */
    private void showPopup(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Thread-safe method to append text to the GUI logger.
     * * @param message The text to display.
     */
    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }
}