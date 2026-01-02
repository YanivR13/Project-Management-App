package managmentGUI; // Defining the package for management-related controllers

import java.io.IOException; // Importing for handling input/output exceptions during FXML loading
import java.time.LocalDate; // Importing for date handling
import java.util.ArrayList; // Importing for dynamic list structures
import java.util.HashMap; // Importing for key-value pair storage
import java.util.Map; // Importing for map interface

import clientGUI.Controllers.MenuControlls.BaseMenuController; // Importing the base menu controller for session data
import common.TimeRange; // Importing the TimeRange domain entity
import common.ServiceResponse; // Importing the server response envelope
import javafx.application.Platform; // Importing for UI thread management
import javafx.collections.FXCollections; // Importing for observable collection utilities
import javafx.collections.ObservableList; // Importing for dynamic list types used in UI components
import javafx.event.ActionEvent; // Importing for handling UI button clicks
import javafx.fxml.FXML; // Importing FXML annotation for field injection
import javafx.fxml.FXMLLoader; // Importing for loading FXML layout files
import javafx.scene.Node; // Importing for generic UI node elements
import javafx.scene.Parent; // Importing for root scene graph elements
import javafx.scene.Scene; // Importing for scene management
import javafx.scene.control.*; // Importing standard JavaFX controls
import javafx.scene.control.Alert.AlertType; // Importing alert categories
import javafx.scene.control.cell.PropertyValueFactory; // Importing for table column mapping
import javafx.scene.layout.AnchorPane; // Importing for layout container management
import javafx.stage.Stage; // Importing for window stage management

/**
 * Controller class for the Representative Dashboard.
 * Manages the dynamic loading of sub-screens within a central pane and handles operational updates.
 */
public class RepresentativeDashboardController extends BaseMenuController { // Class definition start

    // --- 1. Primary FXML Fields ---
    @FXML private TextArea txtLog; // Console area for system feedback
    @FXML private AnchorPane contentPane; // The central container where sub-screens are injected

    // --- Sub-screen FXML Fields (Injected only when specific FXMLs are loaded) ---
    @FXML private TableView<DayScheduleRow> tableHours; // Table for displaying regular hours
    @FXML private TableColumn<DayScheduleRow, String> colDay; // Column for the day of the week
    @FXML private TableColumn<DayScheduleRow, String> colOpen; // Column for opening time
    @FXML private TableColumn<DayScheduleRow, String> colClose; // Column for closing time
    
    @FXML private DatePicker dpSpecialDate; // Date selector for special hours overrides
    @FXML private ComboBox<String> comboSpecialOpen; // Dropdown for special opening time
    @FXML private ComboBox<String> comboSpecialClose; // Dropdown for special closing time

    // Observable list to maintain live data for the hours table
    private ObservableList<DayScheduleRow> scheduleData = FXCollections.observableArrayList(); // List initialization

    // --- 2. Initialization and Infrastructure ---

    @Override // Overriding method from BaseMenuController
    public void onClientReady() { // Start of onClientReady method
        // Check if the inherited client instance is initialized
        if (client != null) { // Start if block
            // Register this dashboard instance as the UI for the client
            client.setUI(this); // Setting UI listener
            
            // Log successful loading and display the session user ID
            appendLog("Representative Dashboard Loaded. System ID: " + userId); // Appending log
            
            // Automatically load the default sub-screen (Regular Hours) upon startup
            showRegularHoursScreen(null); // Initial screen load
        } // End if block
    } // End of onClientReady method

    /**
     * Utility method to load FXML sub-screens into the central contentPane.
     * Crucial: Sets the current class instance ('this') as the controller for all sub-screens.
     */
    private void loadSubScreen(String fxmlPath) { // Start of loadSubScreen method
        try { // Start of try block for FXML loading
            // Clear all current children from the central container
            contentPane.getChildren().clear(); // Clearing pane
            
            // Initialize the FXMLLoader with the provided file path
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath)); // Loader initialization
            
            // Injection logic: Force the loader to use the current dashboard instance as the controller
            loader.setController(this); // Setting 'this' as sub-screen controller
            
            // Load the FXML file and retrieve the resulting UI node
            Node node = loader.load(); // Loading node
            
            // Ensure the loaded content expands to fill the entire AnchorPane
            AnchorPane.setTopAnchor(node, 0.0); // Top constraint
            AnchorPane.setBottomAnchor(node, 0.0); // Bottom constraint
            AnchorPane.setLeftAnchor(node, 0.0); // Left constraint
            AnchorPane.setRightAnchor(node, 0.0); // Right constraint
            
            // Add the processed node into the contentPane's visual hierarchy
            contentPane.getChildren().add(node); // Updating UI
            
        } catch (IOException e) { // Start of catch block for loading errors
            // Log failure to find or load the specified FXML file
            appendLog("Error loading screen: " + fxmlPath + " -> " + e.getMessage()); // Logging error
            // Print the full technical stack trace for debugging
            e.printStackTrace(); // Printing trace
        } // End of try-catch block
    } // End of loadSubScreen method

    // --- 3. Screen Navigation Handlers ---

    @FXML // Link to FXML menu button
    void showRegularHoursScreen(ActionEvent event) { // Start method
        // Load the FXML layout for standard operational hours
        loadSubScreen("/managmentGUI/ActionsFXML/UpdateRegularHours.fxml"); // Executing sub-screen load
        // Re-initialize the table columns and behavior for the newly loaded screen
        setupTable(); // Configuring table
        // Populate the table with default day names and empty times
        initializeEmptyDays(); // Setting defaults
    } // End method

    @FXML // Link to FXML menu button
    void showSpecialHoursScreen(ActionEvent event) { // Start method
        // Load the FXML layout for date-specific schedule overrides
        loadSubScreen("/managmentGUI/ActionsFXML/UpdateSpecialHours.fxml"); // Executing sub-screen load
        // Populate the dropdowns and set date picker constraints
        setupSpecialHoursFields(); // Configuring inputs
    } // End method

    // --- 4. Internal UI Component Setup ---

    /**
     * Populates time dropdowns and configures the DatePicker constraints.
     */
    private void setupSpecialHoursFields() { // Start method
        // Guard Clause: Prevent initialization if FXML components haven't been injected yet
        if (comboSpecialOpen == null) { // Start null check
            return; // Exit method
        } // End null check

        // Create a list of time intervals every 30 minutes for 24 hours
        ObservableList<String> hours = FXCollections.observableArrayList(); // List initialization
        for (int i = 0; i < 24; i++) { // Loop through hours
            hours.addAll(String.format("%02d:00", i), String.format("%02d:30", i)); // Add 00 and 30 slots
        } // End loop
        
        // Assign the generated time list to the dropdown components
        comboSpecialOpen.setItems(hours); // Populating open combo
        comboSpecialClose.setItems(hours); // Populating close combo
        
        // Set standard default values for convenience
        comboSpecialOpen.setValue("09:00"); // Default open
        comboSpecialClose.setValue("22:00"); // Default close

        // Configure the DatePicker to restrict dates to a 30-day future window
        dpSpecialDate.setDayCellFactory(picker -> new DateCell() { // Start of cell factory
            @Override // Overriding updateItem
            public void updateItem(LocalDate date, boolean empty) { // Start updateItem
                super.updateItem(date, empty); // Parent call
                // Disable dates in the past or beyond the 30-day look-ahead limit
                setDisable(empty || date.isBefore(LocalDate.now()) || date.isAfter(LocalDate.now().plusDays(30))); // Constraint logic
            } // End updateItem
        }); // End factory
        
        // Initialize the date picker to the current day
        dpSpecialDate.setValue(LocalDate.now()); // Default date
    } // End method

    /**
     * Configures the TableView for inline editing of regular hours.
     */
    private void setupTable() { // Start method
        // Guard Clause: Ensure the table component is injected
        if (tableHours == null) { // Null check
            return; // Exit
        } // End check

        // Map column references to the data properties in DayScheduleRow
        colDay.setCellValueFactory(new PropertyValueFactory<>("day")); // Day mapping
        colOpen.setCellValueFactory(new PropertyValueFactory<>("openTime")); // Open mapping
        colClose.setCellValueFactory(new PropertyValueFactory<>("closeTime")); // Close mapping
        
        // Enable text-based cell editing for the time columns
        colOpen.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn()); // Setting edit factory
        colClose.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn()); // Setting edit factory

        // Commit logic: Update the internal data object when a user finishes editing a cell
        colOpen.setOnEditCommit(e -> e.getRowValue().setOpenTime(e.getNewValue())); // Commit open time
        colClose.setOnEditCommit(e -> e.getRowValue().setCloseTime(e.getNewValue())); // Commit close time

        // Connect the table to the observable data list and enable editing mode
        tableHours.setItems(scheduleData); // Linking data
        tableHours.setEditable(true); // Enabling UI editing
    } // End method

    /**
     * Populates the schedule list with a clean week starting from Sunday.
     */
    private void initializeEmptyDays() { // Start method
        // Hardcoded array to ensure correct weekly order
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; // Data definition
        // Clear previous entries
        scheduleData.clear(); // List cleanup
        // Iterate through days and create initial row objects with placeholder times
        for (String day : days) { // Start loop
            scheduleData.add(new DayScheduleRow(day, "00:00", "00:00")); // Add row to list
        } // End loop
    } // End method

    // --- 5. Server Communication Logic ---

    /**
     * Collects data from the table and transmits a regular hours update to the server.
     */
    @FXML // Link to FXML update button
    void updateRegularHours(ActionEvent event) { // Start method
        // Convert the tabular list into a Map format compatible with the domain model
        Map<String, TimeRange> updatedMap = new HashMap<>(); // Initialize map
        for (DayScheduleRow row : scheduleData) { // Iterate through rows
            updatedMap.put(row.getDay(), new TimeRange(row.getOpenTime(), row.getCloseTime())); // Map day to range
        } // End conversion loop
        
        // Encapsulate the command and data in a protocol message
        ArrayList<Object> message = new ArrayList<>(); // Initialize message
        message.add("UPDATE_REGULAR_HOURS"); // Command header
        message.add(1); // Hardcoded restaurant ID (per original logic)
        message.add(updatedMap); // Updated data map
        
        // Send request to the server
        client.handleMessageFromClientUI(message); // Transmitting
    } // End method

    /**
     * Transmits a date-specific hours override to the server.
     */
    @FXML // Link to FXML update button
    void updateSpecialHours(ActionEvent event) { // Start method
        // Validation: Ensure a date is selected before sending
        if (dpSpecialDate == null || dpSpecialDate.getValue() == null) { // Null check
            return; // Exit
        } // End check
        
        // Build the message list for the special hours protocol
        ArrayList<Object> msg = new ArrayList<>(); // Initialize list
        msg.add("UPDATE_SPECIAL_HOURS"); // Command header
        msg.add(1); // Hardcoded restaurant ID
        msg.add(dpSpecialDate.getValue()); // Target date
        msg.add(comboSpecialOpen.getValue()); // New open time
        msg.add(comboSpecialClose.getValue()); // New close time

        // Send request to the server
        client.handleMessageFromClientUI(msg); // Transmitting
    } // End method

    // --- 6. Sub-Menu Navigation Handlers ---

    @FXML void createNewSubscriber(ActionEvent event) { // Triggered by menu
        loadSubScreen("/managmentGUI/ActionsFXML/CreateNewSubscriber.fxml"); // Loading sub-view
    } // End method

    @FXML void viewSubscribersList(ActionEvent event) { // Triggered by menu
        loadSubScreen("/managmentGUI/ActionsFXML/SubscribersList.fxml"); // Loading sub-view
    } // End method

    @FXML void viewActiveReservations(ActionEvent event) { // Triggered by menu
        loadSubScreen("/managmentGUI/ActionsFXML/ActiveReservations.fxml"); // Loading sub-view
    } // End method

    @FXML void viewWaitingList(ActionEvent event) { // Triggered by menu
        loadSubScreen("/managmentGUI/ActionsFXML/WaitingList.fxml"); // Loading sub-view
    } // End method

    @FXML void viewCurrentDiners(ActionEvent event) { // Triggered by menu
        loadSubScreen("/managmentGUI/ActionsFXML/CurrentDiners.fxml"); // Loading sub-view
    } // End method
    
    /**
     * Allows staff members to access the customer-facing portal.
     */
    @FXML // Link to portal button
    void clickCustomerPortal(ActionEvent event) { // Start method
        try { // Start navigation try block
            // Define the relative path to the customer portal landing frame
            String fxmlPath = "/clientGUI/fxmlFiles/RemoteLoginFrame.fxml"; // Path definition
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath)); // Loader initialization
            Parent root = loader.load(); // Loading UI graph

            // Inject shared session dependencies into the next controller via the Pipe
            Object nextController = loader.getController(); // Accessing controller
            if (nextController instanceof BaseMenuController) { // Checking interface support
                ((BaseMenuController) nextController).setClient(client, userType, userId); // Injecting session
            } // End injection check

            // Transition the current stage to the customer portal scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Getting stage
            stage.setTitle("Bistro - Customer Portal"); // Updating title
            stage.setScene(new Scene(root)); // Switching scene
            stage.show(); // Displaying window

        } catch (IOException e) { // Catch navigation failures
            appendLog("Navigation Error: " + e.getMessage()); // Log user error
            e.printStackTrace(); // Print technical error
        } // End try-catch
    } // End method
    
    /**
     * Processes server responses regarding operational updates.
     */
    @Override // Overriding from BaseMenuController/ChatIF
    public void display(Object message) { // Start method
        // Handle generic server feedback envelopes
        if (message instanceof ServiceResponse) { // Start check
            ServiceResponse response = (ServiceResponse) message; // Casting object
            // Ensure UI updates occur on the JavaFX Application Thread
            Platform.runLater(() -> { // Start runLater lambda
                // Append the raw server status to the dashboard log
                appendLog("Server Response: " + response.getStatus()); // Logging status
                // Logic: Show success alert if the update was confirmed
                if (response.getStatus() == ServiceResponse.ServiceStatus.UPDATE_SUCCESS) { // If success
                    new Alert(Alert.AlertType.INFORMATION, "Success! Operating hours updated.").show(); // Inform user
                } // End inner if
            }); // End lambda
        } // End outer if
    } // End method

    /**
     * Thread-safe helper to update the UI log area.
     */
    private void appendLog(String msg) { // Start method
        // Verify component existence and redirect to the UI thread
        if (txtLog != null) { // Null check
            Platform.runLater(() -> txtLog.appendText("> " + msg + "\n")); // Appending text
        } // End check
    } // End method

    /**
     * Helper POJO for binding weekly schedule data to the TableView.
     */
    public static class DayScheduleRow { // Inner class start
        private String day; // Day name
        private String openTime; // Opening timestamp
        private String closeTime; // Closing timestamp
        
        public DayScheduleRow(String day, String openTime, String closeTime) { // Constructor
            this.day = day; // Initializing day
            this.openTime = openTime; // Initializing open
            this.closeTime = closeTime; // Initializing close
        } // End constructor
        
        // Standard getters and setters for TableView property binding
        public String getDay() { return day; } // Getter
        public String getOpenTime() { return openTime; } // Getter
        public void setOpenTime(String ot) { this.openTime = ot; } // Setter
        public String getCloseTime() { return closeTime; } // Getter
        public void setCloseTime(String ct) { this.closeTime = ct; } // Setter
    } // Inner class end
} // End of RepresentativeDashboardController class