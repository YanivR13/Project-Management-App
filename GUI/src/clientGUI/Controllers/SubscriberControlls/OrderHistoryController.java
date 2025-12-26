package clientGUI.Controllers.SubscriberControlls;

import java.util.ArrayList;

import client.ChatClient;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class OrderHistoryController {
	
	private ChatClient client;

	
	@FXML private Button btnBack;
	
	@FXML
	private void clickBack(ActionEvent event) {
	    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	    stage.close();
	}
	
	public void loadOrdersForUser(int userId) {
	    if (client == null) {
	        System.out.println("Error: client is null");
	        return;
	    }

	    ArrayList<Object> msg = new ArrayList<>();
	    msg.add("GET_ORDER_HISTORY"); // פקודה לשרת
	    msg.add(userId);              // הנתון

	    client.handleMessageFromClientUI(msg);
	}
}
