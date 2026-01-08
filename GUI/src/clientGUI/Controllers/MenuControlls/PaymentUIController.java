package clientGUI.Controllers.MenuControlls; // Define the package for menu-related controllers

import javafx.event.ActionEvent; // Import for handling UI action events
import javafx.fxml.FXML; // Import for FXML injection annotation
import javafx.scene.control.Alert; // Import for alert dialog boxes
import javafx.scene.control.Alert.AlertType; // Import for defining alert styles
import javafx.scene.control.Button; // Import for button components
import javafx.scene.control.Label; // Import for text label components
import java.util.Random; // Import for generating random numbers
import java.util.ArrayList; // Import for dynamic array list structures
import client.ChatClient; // Import the main client communication class
import clientGUI.Controllers.ICustomerActions; // Import customer action interface
import common.Bill; // Import the Bill data transfer object
import common.Visit; // Import the Visit entity class

/**
 * Controller class for the Payment UI screen.
 * Handles the final bill calculation, display, and submission to the server.
 */
public class PaymentUIController extends BaseMenuController implements ICustomerActions { // Start class definition

    // Injecting UI labels linked to the FXML layout
    @FXML private Label lblTableId; // Label to display the table number
    @FXML private Label lblStartTime; // Label to display the visit start time
    @FXML private Label lblBaseAmount; // Label to display the initial price
    @FXML private Label lblDiscount; // Label to display the discount applied
    @FXML private Label lblFinalAmount; // Label to display the total after discount
    @FXML private Button btnConfirmPay; // Button to process the payment

    // Internal session and data storage fields
    //private ChatClient client; // Reference to the active network client
    //private String userType; // Stores the role of the user (e.g., Subscriber)
    //private int userId; // Stores the unique identifier of the user

    // Tracking IDs required for the backend transaction
    private long currentBillId; // The ID of the bill being processed
    private long currentConfirmationCode; // The code linking the bill to a reservation

    /**
     * Initializes the payment screen with visit details and calculates the final price.
     */
    public void setupPayment(ChatClient client, String userType, int userId, Visit visit, boolean isSubscriber) { // Start method
        
        // Initializing the session data fields
        this.client = client; // Assign client reference
        this.userType = userType; // Assign user role string
        this.userId = userId; // Assign user unique ID
        
        // Caching IDs needed for the database update later
        this.currentBillId = visit.getBillId(); // Retrieve bill ID from visit object
        this.currentConfirmationCode = visit.getConfirmationCode(); // Retrieve confirmation code
        
        // Updating the UI with the static visit information
        lblTableId.setText(String.valueOf(visit.getTableId())); // Set table ID text
        lblStartTime.setText(visit.getStartTime()); // Set start time text

        // Business Logic Simulation: Generate a random base price (Range: 150 - 450)
        Random random = new Random(); // Initialize random generator
        double randomBaseAmount = 150 + (300 * random.nextDouble()); // Perform random calculation
        lblBaseAmount.setText(String.format("%.2f ₪", randomBaseAmount)); // Format and display base price

        // Initialization of calculation variables
        double discountPercent = 0.0; // Default discount to zero
        
        // Refactored Logic: Using a cleaner approach for discount assignment and styling
        if (isSubscriber) { // Check if the user is a registered member
            discountPercent = 10.0; // Apply the 10% discount logic
            lblDiscount.setText("10% (Member)"); // Update label text for members
            lblDiscount.setStyle("-fx-text-fill: green; -fx-font-weight: bold;"); // Apply positive visual styling
        } else { // Handle non-subscriber (Occasional) users
            discountPercent = 0.0; // Ensure discount remains zero
            lblDiscount.setText("0%"); // Update label text
            lblDiscount.setStyle("-fx-text-fill: red;"); // Apply standard visual styling
        } // End of subscriber check

        // Final Calculation: Calculate the total after applying the percentage
        double finalTotal = randomBaseAmount * (1 - (discountPercent / 100)); // Mathematical calculation
        lblFinalAmount.setText(String.format("%.2f ₪", finalTotal)); // Display formatted total to user
        
    } // End of setupPayment method

    /**
     * Event handler for the "Confirm & Pay" button.
     */
    @FXML // Link to FXML action
    private void onPayClicked(ActionEvent event) { // Start of pay action method
        
        try { // Start error handling block for data parsing
            
            // Step 1: Extract numeric values from the UI labels using string cleanup
            double base = Double.parseDouble(lblBaseAmount.getText().replace(" ₪", "")); // Remove currency symbol and parse
            double discount = Double.parseDouble(lblDiscount.getText().replaceAll("[^0-9.]", "")); // Extract only numbers from discount text
            double finalPrice = Double.parseDouble(lblFinalAmount.getText().replace(" ₪", "")); // Remove currency symbol and parse

            // Step 2: Create a Bill DTO object with the parsed values and cached IDs
            Bill bill = new Bill(currentBillId, currentConfirmationCode, base, discount, finalPrice); // Initialize Bill object

            // Step 3: Construct the communication message and transmit to server
            ArrayList<Object> message = new ArrayList<>(); // Initialize message list
            message.add("PROCESS_PAYMENT"); // Add the command header
            message.add(bill); // Add the bill payload
            client.handleMessageFromClientUI(message); // Send list through the client

            // Step 4: UI Feedback - Show success message to the user
            Alert alert = new Alert(AlertType.INFORMATION); // Create information alert
            alert.setTitle("Payment Confirmation"); // Set window title
            alert.setHeaderText("Transaction Successful!"); // Set header text
            alert.setContentText("Payment processed and Table " + lblTableId.getText() + " is now available.\nReturning to Main Menu..."); // Set body text
            alert.showAndWait(); // Display dialog and wait for user to close it

            // Step 5: Transition - Return the user to the appropriate menu screen
            returnToMainMenu(event); // Call navigation helper

        } catch (Exception e) { // Catch any parsing or communication errors
            // Log the exception stack trace for debugging purposes
            e.printStackTrace(); // Print technical error details
        } // End of try-catch block
        
    } // End of onPayClicked method

    /**
     * Navigates the user back to their respective main menu based on their user type.
     */
    private void returnToMainMenu(ActionEvent event) {
        String path = "";
        if (userType != null) {
            switch (userType) {
                case "Terminal":
                    path = "/clientGUI/fxmlFiles/Terminal/ManageReservationFrame.fxml";
                    break;
                case "Subscriber":
                    path = "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml";
                    break;
                default:
                    path = "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml";
                    break;
            }
        }
        navigateTo(client, event, userType, userId, path, "Bistro - Main Menu");
    }

    // Interface requirement implementations (No logic changes permitted)
    @Override public void viewOrderHistory(ChatClient client, int userId) {} // Empty stub
    @Override public void editPersonalDetails(ChatClient client, int userId) {} // Empty stub
    
} // End of PaymentUIController class