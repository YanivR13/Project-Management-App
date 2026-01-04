package terminalGUI.Controllers.TerminalControllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import client.ChatClient;
import common.ChatIF;
import common.LoginSource;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
		// TODO Auto-generated method stub
		
	}
}
