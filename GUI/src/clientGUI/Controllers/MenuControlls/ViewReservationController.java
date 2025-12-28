package clientGUI.Controllers.MenuControlls;

import java.util.ArrayList;
import java.util.List;
import common.Reservation;
import clientGUI.Controllers.ICustomerActions;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.binding.Bindings;
import javafx.stage.Stage;

/**
 * Controller for the View Reservation screen.
 * Displays a list of active reservations for the logged-in customer and provides cancellation options.
 */
public class ViewReservationController extends BaseMenuController implements ICustomerActions {

    // FXML Table components for displaying reservation data
    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, Long> colCode;
    @FXML private TableColumn<Reservation, String> colDate;
    @FXML private TableColumn<Reservation, Integer> colGuests;
    @FXML private TableColumn<Reservation, Void> colAction;

    /**
     * Triggered when the client is fully initialized.
     * Sets the UI reference for the client and requests reservation data from the server.
     */
    @Override
    public void onClientReady() {
        // Log user context for debugging purposes
        System.out.println("DEBUG: Entering screen. UserID: " + userId);
        
        if (client != null && userId != 0) {
            // Ensure the client knows to send responses back to this controller
            client.setUI(this); 
            
            // Clear current items from the table before loading new ones (Thread-safe)
            Platform.runLater(() -> {
                tableReservations.getItems().clear();
                // Change the window title (top-left) to the requested text
                Stage stage = (Stage) tableReservations.getScene().getWindow();
                stage.setTitle("Bistro - view & cancel");
            });

            // Construct and send a request message to the server
            ArrayList<Object> message = new ArrayList<>();
            message.add("GET_ACTIVE_RESERVATIONS");
            message.add(userId);
            client.handleMessageFromClientUI(message);
        }
    }

    /**
     * Initializes the table columns and applies visual styling.
     * Runs automatically when the FXML is loaded.
     */
    @FXML
    public void initialize() {
        // Map Reservation object properties to table columns
        colCode.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("reservationDateTime"));
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));

        // Set fixed row height for consistent visual layout and prevent text clipping
        tableReservations.setFixedCellSize(75.0);
        
        // Aesthetic styling: Rounded corners and light background for visibility
        tableReservations.setStyle(
            "-fx-background-radius: 20; " + 
            "-fx-border-radius: 20; " + 
            "-fx-border-color: #444444; " +
            "-fx-border-width: 2;"
        );
        
        // Dynamic height binding: Hides empty rows by adjusting table height based on item count
        tableReservations.prefHeightProperty().bind(
            tableReservations.fixedCellSizeProperty().multiply(Bindings.size(tableReservations.getItems()).add(1.1))
        );

        // Apply dark-themed centered styling to columns via code
        // CHANGED: -fx-text-fill is now black for better contrast on light cells
        String cellStyle = "-fx-alignment: CENTER; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;";
        colCode.setStyle(cellStyle);
        colDate.setStyle(cellStyle);
        colGuests.setStyle(cellStyle);
        
        Label placeholderLabel = new Label("NO ACTIVE RESERVATIONS");

     placeholderLabel.setStyle(
         "-fx-text-fill: #3498db; " +             
         "-fx-font-size: 28px; " +                
         "-fx-font-weight: bold; " +              
         "-fx-font-family: 'Segoe UI'; " +       
         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 2);" 
     );

     tableReservations.setPlaceholder(placeholderLabel);
        setupCancelButton();
    }

    /**
     * Creates a custom cell factory for the Action column to include a red "Cancel" button.
     * Replaced Pay Bill with a styled Cancel button.
     */
    private void setupCancelButton() {
        colAction.setStyle("-fx-alignment: CENTER;");
        colAction.setCellFactory(param -> new TableCell<>() {
            // Styled red button with rounded corners
            private final Button btnCancel = new Button("Cancel");
            {
                btnCancel.setStyle(
                    "-fx-background-color: #e74c3c; " + // Red color
                    "-fx-text-fill: white; " + 
                    "-fx-background-radius: 15; " + 
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;"
                );
                btnCancel.setPrefHeight(35);
                btnCancel.setPrefWidth(100);

                btnCancel.setOnAction(event -> {
                    Reservation res = getTableView().getItems().get(getIndex());
                    handleCancelAction(res);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // Only show button if the row contains data
                setGraphic(empty ? null : btnCancel);
            }
        });
    }

    /**
     * Handles the cancellation logic when the red button is clicked.
     */
    private void handleCancelAction(Reservation res) {
        // 1. הקפצת הודעת אישור (Confirmation)
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Reservation");
        confirmAlert.setHeaderText("Are you sure you want to cancel?");

        // 2. בדיקה אם המשתמש לחץ על OK
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // שליחת בקשת ביטול לשרת
                ArrayList<Object> message = new ArrayList<>();
                message.add("CANCEL_RESERVATION");
                message.add(res.getConfirmationCode()); // שליחת קוד ההזמנה לזיהוי ב-DB
                client.handleMessageFromClientUI(message);
            }
        });
    }

    /**
     * Handles responses from the server.
     */
    @Override
    public void display(Object message) {
        if (message instanceof List) {
            // עדכון הטבלה הראשוני
            Platform.runLater(() -> {
                tableReservations.setItems(FXCollections.observableArrayList((List<Reservation>) message));
                tableReservations.refresh();
            });
        } else if (message instanceof String && ((String) message).equals("CANCEL_SUCCESS")) {
            // 3. טיפול בהודעת הצלחה מהשרת
            Platform.runLater(() -> {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("The reservation has been successfully canceled.");
                successAlert.showAndWait();

                // רענון הטבלה: שליחת בקשה מחודשת לקבלת ההזמנות המעודכנות
                onClientReady(); 
            });
        }
    }

    /**
     * Navigates back to the appropriate main menu based on user type (Subscriber/Occasional).
     * @param event The action event from the back button.
     */
    @FXML
    void clickBack(ActionEvent event) {
        // Determine the correct FXML path dynamically
        String path = "Subscriber".equalsIgnoreCase(userType) ? 
            "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml" : 
            "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml";
            
        // Use the BaseMenuController's navigateTo utility
        navigateTo(client, event, userType, userId, path, "Bistro - Main Menu");
    }

    // Unimplemented interface methods 
    @Override public void viewOrderHistory(client.ChatClient client, int userId) {}
    @Override public void editPersonalDetails(client.ChatClient client, int userId) {}
}