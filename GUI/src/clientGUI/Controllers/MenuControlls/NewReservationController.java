package clientGUI.Controllers.MenuControlls; // Define the package for menu controllers

import java.net.URL; // Import for handling URL resources
import java.time.LocalDate; // Import for handling dates without time
import java.time.LocalDateTime; // Import for handling both date and time
import java.time.LocalTime; // Import for handling time components
import java.util.ArrayList; // Import for dynamic array list structures
import java.util.ResourceBundle; // Import for localization resources
import client.ChatClient; // Import the main client communication class
import common.ChatIF; // Import the communication interface
import common.Reservation; // Import the Reservation Data Transfer Object
import common.Restaurant; // Import the Restaurant entity class
import common.ServiceResponse; // Import the generic server response envelope
import javafx.application.Platform; // Import for running tasks on the JavaFX thread
import javafx.collections.FXCollections; // Import for creating observable collections
import javafx.collections.ObservableList; // Import for list types used in UI components
import javafx.event.ActionEvent; // Import for handling UI action events
import javafx.fxml.FXML; // Import for FXML injection annotation
import javafx.fxml.FXMLLoader; // Import for loading FXML layout files
import javafx.fxml.Initializable; // Import for initialization interface
import javafx.scene.Node; // Import for generic UI node elements
import javafx.scene.Parent; // Import for root UI elements
import javafx.scene.Scene; // Import for stage scene management
import javafx.scene.control.Alert; // Import for alert dialog boxes
import javafx.scene.control.Alert.AlertType; // Import for alert types (Info, Error, etc.)
import javafx.scene.control.Button; // Import for button components
import javafx.scene.control.ButtonType; // Import for alert button types
import javafx.scene.control.ComboBox; // Import for dropdown selection components
import javafx.scene.control.DateCell; // Import for customizing individual date cells
import javafx.scene.control.DatePicker; // Import for date selection components
import javafx.scene.control.TextArea; // Import for multi-line text display
import javafx.scene.control.TextField; // Import for single-line text input
import javafx.stage.Stage; // Import for the primary window container

/**
 * Controller class for the New Reservation interface.
 */
public class NewReservationController extends BaseMenuController implements ChatIF, Initializable { // Start class definition

    // Injecting UI components linked to the FXML layout
    @FXML private DatePicker dpDate; // Reference to the date selection field
    @FXML private ComboBox<String> comboTime; // Reference to the time selection dropdown
    @FXML private TextField txtGuests; // Reference to the guest count input field
    @FXML private TextArea txtLog; // Reference to the status logging area
    @FXML private Button btnConfirm; // Reference to the confirmation button
    @FXML private Button btnBack; // Reference to the navigation back button
    
    // Internal cache to store restaurant operational data
    private Restaurant currentRestaurant; 
    
    /**
     * Executes when the client instance is ready for network communication.
     */
    @Override // Overriding method from BaseMenuController
    public void onClientReady() { // Start method
        // Prepare a message list to request work times from the server
        ArrayList<Object> msg = new ArrayList<>(); // Initialize the list
        msg.add("GET_RESTAURANT_WORKTIMES"); // Add the specific request command
        
        // Transmit the request to the server via the OCSF client
        this.client.handleMessageFromClientUI(msg); // Send the list
        
        // Update the GUI log to inform the user of the process
        appendLog("Fetching restaurant information..."); // Log action
    } // End method
   
    /**
     * Initializes the controller and sets up UI constraints.
     */
    @Override // Overriding Initializable interface method
    public void initialize(URL location, ResourceBundle resources) { // Start method
        // Lock manual editing of the date picker to ensure format integrity
        dpDate.setEditable(false); // Disable text editing

        // Define a custom factory to control date availability in the calendar
        dpDate.setDayCellFactory(picker -> new DateCell() { // Start cell factory lambda
            @Override // Overriding updateItem for custom rendering
            public void updateItem(LocalDate date, boolean empty) { // Start updateItem
                super.updateItem(date, empty); // Call parent implementation
                
                // Logic: Check if the date being rendered is in the past
                if (date.isBefore(LocalDate.now())) { // Check past date condition
                    setDisable(true); // Disable interaction with past dates
                    setStyle("-fx-background-color: #ffc0cb;"); // Color past dates pink
                } // End if
            } // End updateItem
        }); // End cell factory

        // Generate a list of time slots in 30-minute intervals
        ObservableList<String> hours = FXCollections.observableArrayList(); // Create observable list
        LocalTime time = LocalTime.MIDNIGHT; // Start time at 00:00
        
        do { // Start of loop to populate time slots
            hours.add(time.toString()); // Convert current time to string and add to list
            time = time.plusMinutes(30); // Increment time by 30 minutes
        } while (!time.equals(LocalTime.MIDNIGHT)); // Continue until a full cycle is complete

        // Assign the generated list to the time ComboBox
        comboTime.setItems(hours); // Set items in the UI component
        
        // Default the selected date to today for convenience
        dpDate.setValue(LocalDate.now()); // Set current date
        
        // Inform the user that the system is ready
        appendLog("Ready to take your reservation."); // Log readiness
    } // End method

    /**
     * Logic for processing the 'Confirm Reservation' action.
     */
    @FXML // Link to FXML action
    void clickConfirm(ActionEvent event) { // Start method
        
        // Guard Clause: Check for any empty or null inputs in mandatory fields
        boolean isMissingInput = (dpDate.getValue() == null || comboTime.getValue() == null || txtGuests.getText().isEmpty()); // Validation logic
        
        if (isMissingInput) { // If inputs are missing
            appendLog("Error: Missing input fields."); // Notify user via log
            return; // Terminate method execution
        } // End if

        try { // Start error handling block for parsing and validation
            
            // Clean the guest input string from extra whitespace
            String guestsInput = txtGuests.getText().trim(); // Trim input
            
            // Regex validation: Ensure the string contains only numeric digits
            if (!guestsInput.matches("\\d+")) { // Check for non-numeric characters
                appendLog("Error: Guests field must contain only numbers (no letters or symbols)."); // Log error
                return; // Terminate method execution
            } // End if
            
            // Parse the string into an integer
            int guestCount = Integer.parseInt(guestsInput); // Convert to int
            
            // Logic validation: Reservations must have a positive number of guests
            if (guestCount <= 0) { // Check for zero or negative values
                throw new NumberFormatException(); // Force catch block entry
            } // End if
            
            // Construct a LocalDateTime object for range checking
            LocalDateTime requestedDT = LocalDateTime.of(dpDate.getValue(), LocalTime.parse(comboTime.getValue())); // Combine date and time
            LocalDateTime now = LocalDateTime.now(); // Get current timestamp
            
            // Logic validation: Reservations must be at least 1 hour in the future
            if (requestedDT.isBefore(now.plusHours(1))) { // Check 1-hour lead time
                appendLog("Error: Reservations must be made at least 1 hour in advance."); // Log error
                return; // Terminate execution
            } // End if

            // Logic validation: Limit reservations to one month ahead
            if (requestedDT.isAfter(now.plusMonths(1))) { // Check 1-month limit
                appendLog("Error: Reservations can only be made up to one month in advance."); // Log error
                return; // Terminate execution
            } // End if

            // Format the final date-time string for SQL (YYYY-MM-DD HH:mm:ss)
            String sqlDateTime = dpDate.getValue().toString() + " " + comboTime.getValue() + ":00"; // Build string
            
            // Create the Reservation DTO with session data
            Reservation res = new Reservation(userId, sqlDateTime, guestCount); // Initialize DTO
            
            // Encapsulate the command and DTO in a message for the server
            ArrayList<Object> msg = new ArrayList<>(); // Initialize message list
            msg.add("CREATE_RESERVATION"); // Add command header
            msg.add(res); // Add the reservation object

            // Transmission logic: Ensure client is active and register UI for response
            if (client != null) { // Check client existence
                client.setUI(this); // Set this controller as the message listener
                appendLog("Sending request to server..."); // Log transmission
                client.handleMessageFromClientUI(msg); // Send to server
            } // End if
            
        } catch (NumberFormatException e) { // Catch invalid numeric inputs
            appendLog("Error: Please enter a valid number of guests."); // Log generic error
        } // End catch
        
    } // End method

    /**
     * Handles navigation back to the main menu.
     */
    @FXML // Link to FXML action
    void clickBack(ActionEvent event) { // Start method
        
        String fxmlPath = ""; // Variable to store the destination path
        
        // Refactored: Use switch for better readability in navigation logic
        if (userType == null) { // Guard against null userType
            appendLog("Error: Unknown user type for navigation."); // Log error
            return; // Terminate
        } // End if

        switch (userType) { // Evaluate user type for path selection
            
            case "Subscriber": // For registered subscribers
                fxmlPath = "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml"; // Assign path
                break; // Exit switch
                
            case "Occasional": // For one-time users
                fxmlPath = "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml"; // Assign path
                break; // Exit switch
                
            default: // For any unexpected role
                appendLog("Error: Invalid role detected."); // Log error
                return; // Terminate method
                
        } // End switch

        try { // Start transition handling
            
            // Load the FXML file for the target menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath)); // Initialize loader
            Parent root = loader.load(); // Generate the UI graph
            
            // Inject shared session dependencies into the target controller
            BaseMenuController controller = loader.getController(); // Get controller instance
            controller.setClient(client, userType, userId); // Pass client and session data

            // Execute the scene transition on the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Identify window
            Scene scene = new Scene(root); // Create new scene
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm()); // Apply styling
            stage.setScene(scene); // Set scene to stage
            stage.show(); // Display stage
            
        } catch (Exception e) { // Catch any loading errors
            e.printStackTrace(); // Print technical details
            appendLog("Navigation Error: " + e.getMessage()); // Log user-friendly error
        } // End catch
        
    } // End method

    /**
     * Processes incoming server messages.
     */
    @Override // Overriding ChatIF method
    public void display(Object message) { // Start method
        
        // Handle incoming Restaurant data (Operating hours setup)
        if (message instanceof Restaurant) { // Check if message is a Restaurant object
            this.currentRestaurant = (Restaurant) message; // Cast and store data

            Platform.runLater(() -> { // Execute UI update on main thread
                appendLog("--- Restaurant Information Loaded ---"); // Log status
                appendLog(currentRestaurant.getFormattedOpeningHours()); // Display hours in the log
            }); // End thread block
            return; // Exit method
        } // End if

        // Handle logical server responses (Success, Failures, Suggestions)
        if (message instanceof ServiceResponse) { // Check if message is a ServiceResponse
            
            ServiceResponse sr = (ServiceResponse) message; // Cast to specific response type
            
            Platform.runLater(() -> { // Ensure thread safety for UI modifications
                
                // Evaluating server status code using switch for clean branching
                switch (sr.getStatus()) { // Start switch block
                    
                    case RESERVATION_SUCCESS: // Case: DB successfully updated
                        showPopup(AlertType.INFORMATION, "Success", "Reservation confirmed! Code: " + sr.getData()); // Alert user
                        appendLog("Reservation secured with code: " + sr.getData()); // Log confirmation
                        break; // Exit switch
                        
                    case RESERVATION_SUGGESTION: // Case: Slot busy, alternative found
                        handleSuggestion(sr.getData().toString()); // Trigger suggestion flow
                        break; // Exit switch
                        
                    case RESERVATION_FULL: // Case: No capacity at all
                        showPopup(AlertType.WARNING, "Fully Booked", "Sorry, no tables available."); // Alert user
                        appendLog("Server: No tables available."); // Log status
                        break; // Exit switch
                        
                    case INTERNAL_ERROR: // Case: SQL or network error
                        showPopup(AlertType.ERROR, "Server Error", sr.getData().toString()); // Alert user
                        appendLog("Server Error: " + sr.getData()); // Log technical error
                        break; // Exit switch
                        
                    case RESERVATION_OUT_OF_HOURS: // Case: Time not within operating range
                        showPopup(AlertType.WARNING, "Restaurant Closed", sr.getData().toString()); // Alert user
                        appendLog("Server: " + sr.getData()); // Log closure message
                        break; // Exit switch
                        
                    default: // Default case for unhandled statuses
                        break; // Exit switch
                        
                } // End switch block
                
            }); // End runLater
            
        } // End if for ServiceResponse
        
    } // End method

    /**
     * Logic for handling alternative time slot suggestions.
     */
    private void handleSuggestion(String suggested) { // Start method
        
        // Prompt the user to accept or decline the server's recommendation
        Alert suggestionAlert = new Alert(Alert.AlertType.CONFIRMATION, 
            "Requested time is full. Would you like to reserve for " + suggested + " instead?", 
            ButtonType.OK, ButtonType.CANCEL); // Initialize dialog
            
        suggestionAlert.setTitle("Alternative Slot Found"); // Set title
        suggestionAlert.setHeaderText("No availability for requested time."); // Set header
        
        // Execute the user's choice
        if (suggestionAlert.showAndWait().get() == ButtonType.OK) { // If user accepts (OK clicked)
            
            try { // Attempt to process the alternative booking
                
                int guests = Integer.parseInt(txtGuests.getText()); // Read current guest input
                Reservation res = new Reservation(userId, suggested + ":00", guests); // Create DTO for new slot
                
                ArrayList<Object> msg = new ArrayList<>(); // Initialize new message
                msg.add("CREATE_RESERVATION"); // Add same command
                msg.add(res); // Add new reservation details
                
                client.handleMessageFromClientUI(msg); // Transmit back to server
                appendLog("Attempting to book suggested slot: " + suggested); // Log action
                
            } catch (NumberFormatException e) { // Catch parsing issues
                appendLog("Error reading guest number for suggestion."); // Log failure
            } // End catch
            
        } // End if user accepts
        
    } // End method

    /**
     * Utility method for displaying standardized JavaFX Alerts.
     */
    private void showPopup(AlertType type, String title, String content) { // Start method
        Alert alert = new Alert(type); // Create new alert of provided type
        alert.setTitle(title); // Set title
        alert.setHeaderText(null); // Remove header for simplicity
        alert.setContentText(content); // Set body text
        alert.showAndWait(); // Display and block
    } // End method

    /**
     * Thread-safe method to append text to the GUI logger.
     */
    public void appendLog(String message) { // Start method
        Platform.runLater(() -> { // Transition logic to the Application Thread
            txtLog.appendText("> " + message + "\n"); // Append text with a prefix
        }); // End thread block
    } // End method
    
} // End of NewReservationController class