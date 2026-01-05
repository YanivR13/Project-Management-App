package terminalGUI.Controllers.TerminalControllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import client.ChatClient;
import common.ChatIF;
import common.LoginSource;
import common.ServiceResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ocsf.server.ConnectionToClient;

public class TerminalWaitingListSizeController implements ChatIF {
    private ChatClient client;
    
    @FXML
    private TextField txtDiners;
    
    @FXML
    private Label lblError;

    public void setClient(ChatClient client) {
        this.client = client;
        if (client != null) {
            client.setUI(this);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // נחזור ל-TerminalMenu (נטפל בזה בשלב הבא)
    }

    @FXML
    private void handleContinue(ActionEvent event) {
    	int diners;
    	if (txtDiners.getText().isEmpty()) {
    	    showError("Please enter number of diners");
    	    return;
    	}
    	
    	try {
    	    diners = Integer.parseInt(txtDiners.getText());
    	} catch (NumberFormatException e) {
    	    showError("Number of diners must be a number");
    	    return;
    	}
    	
    	if (diners <= 0) {
    	    showError("Number of diners must be positive");
    	    return;
    	}
    	
    	ArrayList<Object> msg = new ArrayList<>();

        msg.add("JOIN_WAITING_LIST");
        msg.add(diners);

    	
    	if (client != null) {
    		System.out.println("Sending JOIN_WAITING_LIST message: " + msg);
    	    client.handleMessageFromClientUI(msg);
    	} else {
    	    showError("No server connection");
    	}
    }
    
    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

	@Override
	public void display(Object message) {
		Platform.runLater(() -> handleServerMessage(message));
	}
	
	private void handleServerMessage(Object message) {

	    if (!(message instanceof ServiceResponse)) {
	        return;
	    }

	    ServiceResponse response = (ServiceResponse) message;

	    switch (response.getStatus()) {

	        case RESERVATION_SUCCESS:
	            long code = (Long) response.getData();
	            showPopup(
	                "Reservation Confirmed",
	                "A table is available now!\nConfirmation code: " + code,
	                Alert.AlertType.INFORMATION
	            );
	            break;

	        case UPDATE_SUCCESS:
	            showPopup(
	                "Waiting List",
	                "You were added to the waiting list.\nWe will notify you soon.",
	                Alert.AlertType.INFORMATION
	            );
	            break;

	        case RESERVATION_FULL:
	            showPopup(
	                "No Availability",
	                "No tables available in the next 2 hours.",
	                Alert.AlertType.WARNING
	            );
	            break;

	        case INTERNAL_ERROR:
	            showPopup(
	                "Error",
	                response.getMessage(),
	                Alert.AlertType.ERROR
	            );
	            break;
	    }
	}

	
	private void showPopup(String title, String message, AlertType type) {
	    Platform.runLater(() -> {
	        Alert alert = new Alert(type);
	        alert.setTitle(title);
	        alert.setHeaderText(null);
	        alert.setContentText(message);
	        alert.showAndWait();
	    });
	}
}
