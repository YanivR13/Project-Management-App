package clientGUI.Controllers.SubscriberControlls; // Defining the package for subscriber-related controllers

import java.util.ArrayList; // Importing ArrayList for data list management
import common.Reservation; // Importing the Reservation entity class
import client.ChatClient; // Importing the main client communication class
import common.ChatIF; // Importing the communication interface for server responses
import javafx.application.Platform; // Importing Platform for UI thread safety
import javafx.event.ActionEvent; // Importing ActionEvent for UI interaction handling
import javafx.scene.control.Alert; // Importing Alert class for displaying dialog boxes
import javafx.scene.control.Button; // Importing Button component
import javafx.scene.control.TableColumn; // Importing TableColumn for table configuration
import javafx.scene.control.TableView; // Importing TableView for displaying data grids
import javafx.scene.control.cell.PropertyValueFactory; // Importing factory for mapping object properties to columns
import javafx.fxml.FXML; // Importing FXML annotation for UI element injection
import javafx.scene.Node; // Importing Node for accessing the scene graph
import javafx.stage.Stage; // Importing Stage for window management

/**
 * Controller class for the Reservation History screen.
 * This class fetches and displays all past and future reservations for a specific subscriber.
 */
public class ReservationHistoryController implements ChatIF { // Class start implementing ChatIF interface
	
    // Reference to the active network client
	private ChatClient client;

    /**
     * Injects the client and registers this controller as the active UI listener.
     */
	public void setClient(ChatClient client) { // Start of setClient method
	    this.client = client; // Assigning the provided client instance
	    client.setUI(this); // Setting this controller to receive server messages
	} // End of setClient method
	
    // --- FXML UI Components ---
    
	@FXML private Button btnBack; // Reference to the back button
	
	@FXML private TableView<Reservation> reservationsTable; // Reference to the main data table

	@FXML private TableColumn<Reservation, Long> codeCol; // Column for the confirmation code
	@FXML private TableColumn<Reservation, String> dateCol; // Column for the reservation date
	@FXML private TableColumn<Reservation, String> timeCol; // Column for the reservation time
	@FXML private TableColumn<Reservation, Integer> guestCol; // Column for the number of guests
	@FXML private TableColumn<Reservation, String> statusCol; // Column for the reservation status
	
    /**
     * Initializes the table columns by mapping them to Reservation class methods.
     * Called automatically by JavaFX when the FXML is loaded.
     */
	@FXML // Link to FXML
	private void initialize() { // Start of initialize method
		
        // Mapping the confirmation code column to the 'confirmationCode' property
        codeCol.setCellValueFactory(new PropertyValueFactory<>("confirmationCode")); // Property mapping

        // Mapping the date column to the 'getReservationDate' method logic
	    dateCol.setCellValueFactory(new PropertyValueFactory<>("reservationDate")); // Property mapping

        // Mapping the time column to the 'getReservationTime' method logic
	    timeCol.setCellValueFactory(new PropertyValueFactory<>("reservationTime")); // Property mapping

        // Mapping the guest count column to the 'numberOfGuests' property
	    guestCol.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests")); // Property mapping

        // Mapping the status column to the 'statusString' property logic
	    statusCol.setCellValueFactory(new PropertyValueFactory<>("statusString")); // Property mapping
        
	} // End of initialize method

	/**
     * Closes the current history window.
     */
	@FXML // Link to FXML action
	private void clickBack(ActionEvent event) { // Start of clickBack method
        // Identify the current window stage from the event source
	    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Getting stage
        // Close the window
	    stage.close(); // Closing window
	} // End of clickBack method
	
    /**
     * Prepares and sends a request to the server to fetch history for a specific user.
     */
	public void loadReservationsForUser(int userId) { // Start of loadReservationsForUser method
        // Guard Clause: Ensure the client connection is initialized
	    if (client == null) { // Check if client is null
	        System.out.println("Error: client is null"); // Log technical error
	        return; // Terminate execution
	    } // End of null check

        // Creating a message list following the communication protocol
	    ArrayList<Object> msg = new ArrayList<>(); // Initializing message list
	    msg.add("GET_RESERVATIONS_HISTORY"); // Adding server command
	    msg.add(userId); // Adding user ID as the parameter

        // Sending the request to the server
	    client.handleMessageFromClientUI(msg); // Transmitting message
	} // End of loadReservationsForUser method

    /**
     * Processes incoming data from the server and updates the UI table.
     */
	@Override // Overriding from ChatIF
	public void display(Object message) { // Start of display method
        
        // Logical Branching: Handle list-based responses and generic errors separately
	    if (message instanceof ArrayList) { // Start if message is a list
            
            // Casting the message to a generic ArrayList
	        ArrayList<?> data = (ArrayList<?>) message; // Casting
	        
            // Extracting the command identifier from the first index
	        String command = data.get(0).toString(); // Getting command string

            // Refactored: Using switch-case to handle different server commands
            switch (command) { // Start switch on command
                
                case "RESERVATION_HISTORY": // Case for historical data retrieval
                    
                    // Extract the actual list of Reservation objects from the second index
                    ArrayList<Reservation> reservations = (ArrayList<Reservation>) data.get(1); // Casting list

                    // Ensure the UI update runs on the main JavaFX Application Thread
                    Platform.runLater(() -> { // Start of Platform.runLater
                        
                        // Check if no records were found in the database
                        if (reservations == null || reservations.isEmpty()) { // Start empty check
                            showNoReservationsAndClose(); // Show alert and close
                            return; // Exit lambda
                        } // End empty check
                        
                        // Clear existing table entries and populate with new data
                        reservationsTable.getItems().clear(); // Clearing table
                        reservationsTable.getItems().addAll(reservations); // Adding all items
                        
                    }); // End of Platform.runLater
                    break; // Exit switch case

                default: // For any unhandled command strings within a list
                    break; // Exit switch
            } // End of switch block
	    } // End of if message is list
	    else { // If the message is not a list (Unexpected format or technical error)
            // Show a technical error popup and close the window
	    	Platform.runLater(() -> showUnexpectedErrorAndClose()); // Run error handling
	    } // End of else block
        
	} // End of display method
	
	/**
	 * Displays a message indicating that no reservations exist and closes the window.
	 */
	private void showNoReservationsAndClose() { // Start helper method

	    // Initializing an information alert
	    Alert alert = new Alert(Alert.AlertType.INFORMATION); // Create alert
	    alert.setTitle("Reservation History"); // Set title
	    alert.setHeaderText(null); // Clear header
	    alert.setContentText("You have no reservations in your history."); // Set message
	    alert.showAndWait(); // Display and wait

        // Identify the stage and close it
	    Stage stage = (Stage) reservationsTable.getScene().getWindow(); // Get stage
	    stage.close(); // Close stage
        
	} // End helper method
	
	/**
	 * Displays an unexpected error message and closes the window.
	 */
    private void showUnexpectedErrorAndClose() { // Start helper method
        
        // Initializing an error alert
        Alert alert = new Alert(Alert.AlertType.ERROR); // Create alert
        alert.setTitle("Unexpected Error"); // Set title
        alert.setHeaderText(null); // Clear header
        alert.setContentText( // Set detailed error message
            "An unexpected error occurred while loading your reservation history.\n" +
            "Please try again later."
        ); // End content text
        alert.showAndWait(); // Display and wait

        // Identify the stage and close it
        Stage stage = (Stage) reservationsTable.getScene().getWindow(); // Get stage
        stage.close(); // Close stage
        
    } // End helper method
} // End of ReservationHistoryController class