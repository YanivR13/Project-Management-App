package clientGUI.Controllers.MenuControlls;

import java.util.ArrayList;
import clientGUI.Controllers.ICustomerActions;
import common.Visit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the initial payment entry screen.
 * This class handles the verification of the confirmation code provided by the customer.
 */
public class PayBillEntryController extends BaseMenuController implements ICustomerActions {

    @FXML private TextField txtCode;
    @FXML private Button btnBack;
    @FXML private Button btnVerify;

    /**
     * Called when the client connection is ready. 
     * Sets this controller as the active UI in ChatClient to receive server responses.
     */
    @Override
    public void onClientReady() {
        if (client != null) client.setUI(this);
    }

    /**
     * Handles the "Verify" button click. 
     * Validates the input code and sends a request to the server to fetch visit details.
     * @param event The action event triggered by the button.
     */
    @FXML
    void onVerifyClicked(ActionEvent event) {
        String codeStr = txtCode.getText().trim();
        if (codeStr.isEmpty()) {
            showAlert("Error", "Please enter a confirmation code.", AlertType.WARNING);
            return;
        }

        try {
            long code = Long.parseLong(codeStr);
            ArrayList<Object> message = new ArrayList<>();
            message.add("GET_VISIT_BY_CODE");
            message.add(code);
            client.handleMessageFromClientUI(message);
        } catch (NumberFormatException e) {
            showAlert("Error", "Code must be a number.", AlertType.ERROR);
        }
    }

    /**
     * Callback method to handle messages received from the server.
     * Processes either a "NOT_FOUND" error or a successful "Visit" object retrieval.
     * @param message The response from the server.
     */
    @Override
    public void display(Object message) {
    	// Case 1: The confirmation code does not exist or visit is not active
        if (message instanceof String && message.equals("VISIT_NOT_FOUND")) {
            Platform.runLater(() -> {
                showAlert("Not Found", "No active visit found. Returning to menu.", AlertType.INFORMATION);
                returnToMainMenu();
            });
        } 
        
       // Case 2: Successful retrieval of the Visit object
        else if (message instanceof Visit) {
            Platform.runLater(() -> {
                Visit visit = (Visit) message;
                try {
                	// Load the FXML for the final payment summary screen
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/MenuFXML/PayBillFrame.fxml"));
                    Parent root = loader.load();

                    // Get the next controller and inject the session data
                    PaymentUIController paymentController = loader.getController();

                    /* * CRITICAL UPDATE: Passing the full Visit object.
                     * This ensures the PaymentUIController has access to billId and 
                     * confirmationCode required for the database update transaction.
                     */
                    paymentController.setupPayment(
                        client, 
                        userType, 
                        userId, 
                        visit, 
                        "Subscriber".equalsIgnoreCase(userType)
                    );

                    // Switch the current scene to the payment summary
                    Stage stage = (Stage) btnVerify.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Bistro - Final Payment Summary");
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to load the payment screen.", AlertType.ERROR);
                }
            });
        }
    }

    /**
     * Logic to determine the correct main menu path based on the user's role.
     */
    private void returnToMainMenu() {
    	// Determine path based on UserType
        String path = "Subscriber".equalsIgnoreCase(userType) ? 
            "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml" : 
            "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml";
        navigateTo(client, new ActionEvent(btnBack, null), userType, userId, path, "Bistro - Main Menu");
    }

    /**
     * Navigates back to the main menu when the "Back" button is clicked.
     */
    @FXML
    void onBackClicked(ActionEvent event) {
        returnToMainMenu();
    }

    /**
     * Helper method to display JavaFX Alert dialogs.
     */
    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override public void viewOrderHistory(client.ChatClient client, int userId) {}
    @Override public void editPersonalDetails(client.ChatClient client, int userId) {}
}