package clientGUI.Controllers.MenuControlls;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.util.Random;
import java.util.ArrayList;
import client.ChatClient;
import clientGUI.Controllers.ICustomerActions;
import common.Bill;
import common.Visit;

/**
 * Controller class for the Payment UI screen.
 * Handles the final bill calculation, display, and submission to the server.
 * Implements ICustomerActions for consistent customer-related functionality.
 */
public class PaymentUIController extends BaseMenuController implements ICustomerActions {

    @FXML private Label lblTableId;
    @FXML private Label lblStartTime;
    @FXML private Label lblBaseAmount;
    @FXML private Label lblDiscount;
    @FXML private Label lblFinalAmount;
    @FXML private Button btnConfirmPay;

    private ChatClient client;
    private String userType;
    private int userId;

    private long currentBillId;
    private long currentConfirmationCode;

    /**
     * Initializes the payment screen with visit details and calculates the final price.
     * * @param client       The active ChatClient instance for server communication.
     * @param userType     The type of user (e.g., "Subscriber" or "Occasional").
     * @param userId       The unique ID of the logged-in user.
     * @param visit        The Visit object containing table and billing information.
     * @param isSubscriber Boolean flag to determine if a member discount applies.
     */
    public void setupPayment(ChatClient client, String userType, int userId, Visit visit, boolean isSubscriber) {
        this.client = client;
        this.userType = userType;
        this.userId = userId;
        
        // Store IDs required for the database update transaction
        this.currentBillId = visit.getBillId(); 
        this.currentConfirmationCode = visit.getConfirmationCode();
        
        // Display visit details on the UI
        lblTableId.setText(String.valueOf(visit.getTableId()));
        lblStartTime.setText(visit.getStartTime());

        // Generate a random base price for simulation (Range: 150 - 450)
        Random random = new Random();
        double randomBaseAmount = 150 + (300 * random.nextDouble());
        lblBaseAmount.setText(String.format("%.2f ₪", randomBaseAmount));

        // Calculate discount based on user subscription status
        double discountPercent = isSubscriber ? 10.0 : 0.0;
        if (isSubscriber) {
            lblDiscount.setText("10% (Member)");
            lblDiscount.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            lblDiscount.setText("0%");
            lblDiscount.setStyle("-fx-text-fill: red;");
        }

        // Calculate and display the final total
        double finalTotal = randomBaseAmount * (1 - (discountPercent / 100));
        lblFinalAmount.setText(String.format("%.2f ₪", finalTotal));
    }

    /**
     * Event handler for the "Confirm & Pay" button.
     * Extracts values from the UI, creates a Bill object, and sends it to the server.
     * * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    private void onPayClicked(ActionEvent event) {
        try {
        	// 1. Extract and clean numeric data from UI labels
            double base = Double.parseDouble(lblBaseAmount.getText().replace(" ₪", ""));
            double discount = Double.parseDouble(lblDiscount.getText().replaceAll("[^0-9.]", ""));
            double finalPrice = Double.parseDouble(lblFinalAmount.getText().replace(" ₪", ""));

            // 2. Create a Bill object with the synchronized database IDs
            Bill bill = new Bill(currentBillId, currentConfirmationCode, base, discount, finalPrice);

            // 3. Construct the message protocol and send to the server
            ArrayList<Object> message = new ArrayList<>();
            message.add("PROCESS_PAYMENT");
            message.add(bill);
            client.handleMessageFromClientUI(message);

            // 4. Show success confirmation and navigate back to the main menu
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Payment Confirmation");
            alert.setHeaderText("Transaction Successful!");
            alert.setContentText("Payment processed and Table " + lblTableId.getText() + " is now available.\nReturning to Main Menu...");
            alert.showAndWait();

            returnToMainMenu(event);

        } catch (Exception e) {
        	// Log error if parsing fails
            e.printStackTrace();
        }
    }

    /**
     * Navigates the user back to their respective main menu based on their user type.
     * * @param event The ActionEvent used to identify the current stage.
     */
    private void returnToMainMenu(ActionEvent event) {
    	// Determine the FXML path based on user role
        String path = "Subscriber".equalsIgnoreCase(userType) ? 
            "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml" : 
            "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml";
            
        navigateTo(client, event, userType, userId, path, "Bistro - Main Menu");
    }

    @Override public void viewOrderHistory(ChatClient client, int userId) {}
    @Override public void editPersonalDetails(ChatClient client, int userId) {}
}