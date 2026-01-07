package terminalGUI.Controllers.TerminalControllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;

import client.ChatClient;
import common.ChatIF;
import common.LoginSource;
import common.ServiceResponse;
import common.ServiceResponse.ServiceStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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
    private void backToTerminal(ActionEvent event) {
        try {
            // 1. טעינת קובץ ה-FXML של התפריט
            // שים לב: וודא שהנתיב כאן תואם למבנה התיקיות שלך
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/Terminal/TerminalMenuFrame.fxml"));
            Parent root = loader.load();

            // 2. קבלת הקונטרולר של התפריט והעברת הלקוח (Client) אליו
            // נניח ששם הקונטרולר הוא TerminalMenuController
            Object controller = loader.getController();
            if (controller instanceof TerminalMenuController) {
                ((TerminalMenuController) controller).setClient(this.client);
            }

            // 3. החלפת הסצנה בחלון הקיים
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load terminal menu");
        }
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
	
	@SuppressWarnings("unchecked")
	private void handleServerMessage(Object message) {

	    if (!(message instanceof ServiceResponse)) {
	        return;
	    }

	    ServiceResponse response = (ServiceResponse) message;

	    // --- החלק החדש שמוודא שהודעת השגיאה תוצג ---
	    if (response.getStatus() == ServiceStatus.INTERNAL_ERROR) {
	        if ("ALREADY_IN_LIST".equals(response.getData())) {
	            showError("You are already on the waiting list.");
	        } else {
	            showError("Error: " + response.getData());
	        }
	        return; // עוצר כאן כדי שלא ינסה לפתוח פופ-אפ של הצלחה
	    }
	    // ------------------------------------------

	    if (response.getStatus() != ServiceStatus.UPDATE_SUCCESS) {
	        return;
	    }

	    Object data = response.getData();

	    //  תרחיש כניסה מיידית למסעדה
	    if (data instanceof Map) {
	        Map<String, Object> map = (Map<String, Object>) data;

	        if ("IMMEDIATE".equals(map.get("mode"))) {
	            long confirmationCode = ((Number) map.get("confirmationCode")).longValue();
	            int tableId = ((Number) map.get("tableId")).intValue();

	            Alert alert = new Alert(Alert.AlertType.INFORMATION);
	            alert.setTitle("Table Available");
	            alert.setHeaderText(null);
	            alert.setContentText(
	                "A table is available – you can enter now.\n\n" +
	                "Table number: " + tableId + "\n" +
	                "Confirmation code: " + confirmationCode
	            );
	            alert.showAndWait();
	            return;
	        }
	    }

	    //  כל שאר המקרים: נכנס לרשימת המתנה
	    String confirmationCode = data.toString();

	    Alert alert = new Alert(Alert.AlertType.INFORMATION);
	    alert.setTitle("Waiting List");
	    alert.setHeaderText(null);
	    alert.setContentText(
	        "You have been added to the waiting list.\n\n" +
	        "Confirmation code: " + confirmationCode
	    );
	    alert.showAndWait();
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
