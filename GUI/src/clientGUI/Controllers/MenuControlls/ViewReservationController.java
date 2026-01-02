package clientGUI.Controllers.MenuControlls; // Defining the package for menu controllers

import java.util.ArrayList; // Importing ArrayList for message construction
import java.util.List; // Importing List interface for data handling
import common.Reservation; // Importing the Reservation entity
import clientGUI.Controllers.ICustomerActions; // Importing the customer actions interface
import javafx.application.Platform; // Importing Platform for UI thread safety
import javafx.collections.FXCollections; // Importing for observable list creation
import javafx.event.ActionEvent; // Importing for UI action event handling
import javafx.fxml.FXML; // Importing FXML annotation for injection
import javafx.scene.control.*; // Importing standard JavaFX controls
import javafx.scene.control.cell.PropertyValueFactory; // Importing for table cell mapping
import javafx.beans.binding.Bindings; // Importing for dynamic UI binding
import javafx.stage.Stage; // Importing Stage for window management

/**
 * Controller for the View Reservation screen.
 * Displays a list of active reservations and provides cancellation options.
 */
public class ViewReservationController extends BaseMenuController implements ICustomerActions { // Start class definition

    // FXML Table components for displaying reservation data
    @FXML private TableView<Reservation> tableReservations; // Reference to the main table
    @FXML private TableColumn<Reservation, Long> colCode; // Column for confirmation codes
    @FXML private TableColumn<Reservation, String> colDate; // Column for reservation dates
    @FXML private TableColumn<Reservation, Integer> colGuests; // Column for guest counts
    @FXML private TableColumn<Reservation, Void> colAction; // Column for the cancel button

    /**
     * Triggered when the client is fully initialized.
     */
    @Override // Overriding method from BaseMenuController
    public void onClientReady() { // Start of onClientReady method
        // Log user context for debugging purposes
        System.out.println("DEBUG: Entering screen. UserID: " + userId); // Printing debug info
        
        // Ensure client is present and userId is valid
        if (client != null && userId != 0) { // Checking client and userId status
            
            // Register this controller to receive server responses
            client.setUI(this); // Setting the UI listener
            
            // Execute UI updates safely on the JavaFX thread
            Platform.runLater(() -> { // Start of Platform.runLater
                
                // Clear existing items from the table
                tableReservations.getItems().clear(); // Clearing the list
                
                // Retrieve the current window stage
                Stage stage = (Stage) tableReservations.getScene().getWindow(); // Getting stage
                
                // Set the window title as requested
                stage.setTitle("Bistro - view & cancel"); // Updating title
                
            }); // End of Platform.runLater

            // Create the protocol message for the server
            ArrayList<Object> message = new ArrayList<>(); // Initializing message list
            
            // Add the specific fetch command
            message.add("GET_ACTIVE_RESERVATIONS"); // Adding command
            
            // Add the current user ID to filter reservations
            message.add(userId); // Adding userId payload
            
            // Send the request to the server
            client.handleMessageFromClientUI(message); // Sending through client
            
        } // End of if check
        
    } // End of onClientReady method

    /**
     * Initializes the table columns and applies visual styling.
     */
    @FXML // FXML linkage
    public void initialize() { // Start of initialize method
        
        // Map the Reservation fields to the corresponding TableColumns
        colCode.setCellValueFactory(new PropertyValueFactory<>("confirmationCode")); // Mapping code
        colDate.setCellValueFactory(new PropertyValueFactory<>("reservationDateTime")); // Mapping date
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests")); // Mapping guests

        // Set a fixed row height to ensure consistent look and prevent clipping
        tableReservations.setFixedCellSize(75.0); // Setting size to 75.0
        
        // Define the visual style for the table component
        tableReservations.setStyle( // Start of style setting
            "-fx-background-radius: 20; " + // Rounding corners
            "-fx-border-radius: 20; " + // Rounding borders
            "-fx-border-color: #444444; " + // Setting border color
            "-fx-border-width: 2;" // Setting border thickness
        ); // End of style setting
        
        // Bind table height to the number of items to hide empty rows
        tableReservations.prefHeightProperty().bind( // Start of binding
            tableReservations.fixedCellSizeProperty().multiply(Bindings.size(tableReservations.getItems()).add(1.1)) // Calculation
        ); // End of binding

        // Define a shared CSS string for column content styling
        String cellStyle = "-fx-alignment: CENTER; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;"; // Styling string
        
        // Apply styling to data columns
        colCode.setStyle(cellStyle); // Styling code column
        colDate.setStyle(cellStyle); // Styling date column
        colGuests.setStyle(cellStyle); // Styling guests column
        
        // Create a custom label to show when the list is empty
        Label placeholderLabel = new Label("NO ACTIVE RESERVATIONS"); // Initializing label

        // Set advanced aesthetic styling for the placeholder
        placeholderLabel.setStyle( // Start of placeholder styling
            "-fx-text-fill: #3498db; " + // Blue color
            "-fx-font-size: 28px; " + // Large font
            "-fx-font-weight: bold; " + // Bold text
            "-fx-font-family: 'Segoe UI'; " + // Font family
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 2);" // Drop shadow effect
        ); // End of placeholder styling

        // Assign the placeholder to the table
        tableReservations.setPlaceholder(placeholderLabel); // Setting the placeholder
        
        // Initialize the custom cancel button logic
        setupCancelButton(); // Calling helper method
        
    } // End of initialize method

    /**
     * Creates a custom cell factory for the Action column.
     */
    private void setupCancelButton() { // Start of setupCancelButton method
        
        // Center the content in the action column
        colAction.setStyle("-fx-alignment: CENTER;"); // Setting alignment
        
        // Define how each cell in the action column should be rendered
        colAction.setCellFactory(param -> new TableCell<>() { // Start of cell factory
            
            // Initialize a stylized JavaFX button
            private final Button btnCancel = new Button("Cancel"); // Creating button
            
            { // Start of button configuration block
                
                // Apply red theme and interactive styling to the button
                btnCancel.setStyle( // Start of button styling
                    "-fx-background-color: #e74c3c; " + // Red background
                    "-fx-text-fill: white; " + // White text
                    "-fx-background-radius: 15; " + // Rounded corners
                    "-fx-font-weight: bold; " + // Bold text
                    "-fx-cursor: hand;" // Pointer cursor
                ); // End of button styling
                
                // Set fixed dimensions for the button
                btnCancel.setPrefHeight(35); // Height setting
                btnCancel.setPrefWidth(100); // Width setting

                // Define the action when the button is clicked
                btnCancel.setOnAction(event -> { // Start of button action
                    // Retrieve the specific reservation object for this row
                    Reservation res = getTableView().getItems().get(getIndex()); // Getting item
                    // Trigger the cancellation confirmation flow
                    handleCancelAction(res); // Calling handler
                }); // End of button action
                
            } // End of configuration block

            @Override // Overriding updateItem for custom rendering
            protected void updateItem(Void item, boolean empty) { // Start updateItem
                super.updateItem(item, empty); // Calling parent
                // Display the button only if the row is not empty
                setGraphic(empty ? null : btnCancel); // Conditional rendering
            } // End updateItem
            
        }); // End of cell factory
        
    } // End of setupCancelButton method

    /**
     * Handles the cancellation logic with a confirmation dialog.
     */
    private void handleCancelAction(Reservation res) { // Start handleCancelAction method
        
        // Initialize a confirmation alert dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION); // Creating alert
        
        // Set window and header text for clarity
        confirmAlert.setTitle("Cancel Reservation"); // Setting title
        confirmAlert.setHeaderText("Are you sure you want to cancel?"); // Setting header

        // Display the dialog and wait for user response
        confirmAlert.showAndWait().ifPresent(response -> { // Start response handling
            
            // Check if the user confirmed the action
            if (response == ButtonType.OK) { // User clicked OK
                
                // Prepare the message for the server
                ArrayList<Object> message = new ArrayList<>(); // Initializing list
                
                // Add the cancellation command
                message.add("CANCEL_RESERVATION"); // Adding command
                
                // Provide the unique confirmation code for DB identification
                message.add(res.getConfirmationCode()); // Adding code payload
                
                // Send request through client UI handler
                client.handleMessageFromClientUI(message); // Transmitting
                
            } // End of OK check
            
        }); // End of response handling
        
    } // End of handleCancelAction method

    /**
     * Handles responses from the server.
     */
    @Override // Overriding ChatIF display method
    public void display(Object message) { // Start of display method
        
        // Check if the server sent a list (Initial load or refresh)
        if (message instanceof List) { // Check for list instance
            
            // Update the table items on the UI thread
            Platform.runLater(() -> { // Start runLater
                // Convert to observable list and populate table
                tableReservations.setItems(FXCollections.observableArrayList((List<Reservation>) message)); // Setting items
                // Refresh table visuals
                tableReservations.refresh(); // Refreshing
            }); // End runLater
            
        } // End of List check
        
        // Check if the server sent a success notification for cancellation
        else if (message instanceof String && ((String) message).equals("CANCEL_SUCCESS")) { // Check for success string
            
            // Show success message and refresh data on UI thread
            Platform.runLater(() -> { // Start runLater
                
                // Create an information alert
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION); // Initializing alert
                successAlert.setTitle("Success"); // Setting title
                successAlert.setHeaderText(null); // No header
                successAlert.setContentText("The reservation has been successfully canceled."); // Setting message
                successAlert.showAndWait(); // Display and block

                // Refresh the table by re-triggering the data fetch logic
                onClientReady(); // Calling refresh method
                
            }); // End runLater
            
        } // End of success string check
        
    } // End of display method

    /**
     * Navigates back to the relevant main menu.
     */
    @FXML // FXML linkage
    void clickBack(ActionEvent event) { // Start clickBack method
        
        // Variable to store the destination path
        String fxmlPath = ""; // Initialize path
        
        // Refactored: Using switch-case for role-based navigation logic
        if (userType != null) { // Guard against null type
            
            switch (userType) { // Evaluate userType
                
                case "Subscriber": // Registered member case
                    fxmlPath = "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml"; // Set subscriber path
                    break; // Exit switch
                    
                default: // Occasional or other user case
                    fxmlPath = "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml"; // Set occasional path
                    break; // Exit switch
                    
            } // End of switch
            
        } // End of null guard
            
        // Use the BaseMenuController's navigateTo utility for scene transition
        navigateTo(client, event, userType, userId, fxmlPath, "Bistro - Main Menu"); // Executing navigation
        
    } // End of clickBack method

    // Interface requirement implementations (Stubs - logic preserved)
    @Override public void viewOrderHistory(client.ChatClient client, int userId) {} // Empty implementation stub
    @Override public void editPersonalDetails(client.ChatClient client, int userId) {} // Empty implementation stub
    
} // End of ViewReservationController class