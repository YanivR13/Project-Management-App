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
import javafx.stage.Stage;

public class ManageReservationController implements ChatIF{
    
    private ChatClient client;
    
    @FXML private Button btnCancel, btnExitWaiting, btnPay, btnBack;

    public void setClient(ChatClient client) {
        this.client = client;
    }

    @FXML
    void onCancelReservation(ActionEvent event) {
    	
    }

    @FXML
    void onExitWaitingList(ActionEvent event) {	
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
    	if (message != null) {
            Platform.runLater(() ->
                System.out.println("[Terminal] " + message)
            );
        }
    }

}