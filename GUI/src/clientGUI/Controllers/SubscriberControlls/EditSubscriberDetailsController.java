package clientGUI.Controllers.SubscriberControlls;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.ArrayList;

import client.ChatClient;
import common.ChatIF;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

public class EditSubscriberDetailsController implements ChatIF {
    private ChatClient client;
    private int userId;
    
    // ===== Text Fields =====
    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtEmail;

    // ===== Buttons =====
    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    public void setClient(ChatClient client) {
        this.client = client;
        client.setUI(this);
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @FXML
    private void clickCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void clickSave(ActionEvent event) {

        if (client == null) {
            System.out.println("Error: client is null");
            return;
        }

        String username = txtUsername.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        // אם הכל ריק – אין מה לעדכן
        if (username.isEmpty() && phone.isEmpty() && email.isEmpty()) {
            System.out.println("No fields were filled. Nothing to update.");
            return;
        }

        ArrayList<Object> msg = new ArrayList<>();
        msg.add("UPDATE_SUBSCRIBER_DETAILS");
        msg.add(userId);

        // שדות ריקים נשלחים כ-null
        msg.add(username.isEmpty() ? null : username);
        msg.add(phone.isEmpty() ? null : phone);
        msg.add(email.isEmpty() ? null : email);

        client.handleMessageFromClientUI(msg);
    }

	@Override
	public void display(Object message) {
		// TODO Auto-generated method stub
		
	}
}
