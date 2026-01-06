package terminalGUI.Controllers.TerminalControllers;

import java.util.ArrayList;

import client.ChatClient;
import common.ChatIF;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class ManageReservationController implements ChatIF{
    
    private ChatClient client;
    
    @FXML private Button btnCancel, btnExitWaiting, btnPay, btnBack;

    public void setClient(ChatClient client) {
        this.client = client;
        this.client.setUI(this);
    }

    @FXML
    void onCancelReservation(ActionEvent event) {
    	javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Cancel Reservation");
        dialog.setHeaderText("Reservation Cancellation");
        dialog.setContentText("Please enter your confirmation code:");

        dialog.showAndWait().ifPresent(codeStr -> {
            try {
                long code = Long.parseLong(codeStr.trim());

                ArrayList<Object> message = new ArrayList<>();
                message.add("CANCEL_RESERVATION");
                message.add(code);
                client.handleMessageFromClientUI(message);
                
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "The code must be a number.", AlertType.ERROR);
            }
        });
    }

    @FXML
    void onExitWaitingList(ActionEvent event) {	
    	// Creating the input dialog for the confirmation code
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Exit Waiting List");
        dialog.setHeaderText("Confirmation Code Required");
        dialog.setContentText("Please enter your Waiting List Confirmation Code:");

        dialog.showAndWait().ifPresent(codeStr -> {
            try {
                // Parse the input as a long
                long code = Long.parseLong(codeStr.trim());

                // Structure the message for the ServerController 
                ArrayList<Object> message = new ArrayList<>();
                message.add("CANCEL_WAITING_LIST_BY_CODE");
                message.add(code);

                // Send to server via ChatClient
                client.handleMessageFromClientUI(message);

            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "The code must be a numeric value.", AlertType.ERROR);
            }
        });
    }

    @FXML
    void onPayReservation(ActionEvent event) {
            
    }

    @FXML
    void onBackClicked(ActionEvent event) {
    	try {
            // Load the Terminal Menu screen [cite: 2]
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/Terminal/TerminalMenuFrame.fxml"));
            Parent root = loader.load();

            // Inject ChatClient into the menu controller
            TerminalMenuController controller = loader.getController();
            controller.setClient(client);

            // Switch scene
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Bistro - Service Terminal");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load menu screen.", AlertType.ERROR);
        }
    }
    
    /**
     * Helper for standardized alerts.
     */
    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @Override
    public void display(Object message) {
    	if (message instanceof String) {
            String response = (String) message;

            Platform.runLater(() -> {
                switch (response) {
                    case "CANCEL_SUCCESS":
                        showAlert("Success", "Your reservation has been canceled successfully.", AlertType.INFORMATION);
                        break;
                    case "CANCEL_FAILED":
                        showAlert("Failure", "Reservation not found or cannot be canceled.", AlertType.ERROR);
                        break;
                    case "CANCEL_WAITING_SUCCESS":
                        showAlert("Success", "You have been successfully removed from the waiting list.", AlertType.INFORMATION);
                        break;
                    case "NOT_ON_WAITING_LIST":
                        showAlert("Notice", "No active waiting list entry found for this code.", AlertType.WARNING);
                        break;
                    case "SERVER_ERROR":
                        showAlert("Error", "A server error occurred. Please try again later.", AlertType.ERROR);
                        break;

                    default:
                        System.out.println("[Terminal] Received message: " + response);
                        break;
                }
            });
        }
    }

}