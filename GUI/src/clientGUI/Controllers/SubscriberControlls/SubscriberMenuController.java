package clientGUI.Controllers.SubscriberControlls;

import java.util.ArrayList;
import client.ChatClient;
import clientGUI.Controllers.ICustomerActions;
import clientGUI.Controllers.RemoteLoginController;
import common.ChatIF;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class SubscriberMenuController implements ChatIF, ICustomerActions {

    private ChatClient client;

    @FXML private Button btnNewRes, btnCancelRes, btnViewRes, btnExitWait, btnHistory, btnEditProfile, btnLogout;
    @FXML private TextArea txtLog;

    public void setClient(ChatClient client) {
        this.client = client;
    }

    @FXML void clickNewRes(ActionEvent event) { createNewReservation(client, event, "Subscriber"); }
    @FXML void clickCancelRes(ActionEvent event) { cancelReservation(client, event, "Subscriber"); }
    @FXML void clickViewRes(ActionEvent event) { viewReservation(client, event, "Subscriber"); }
    @FXML void clickExitWait(ActionEvent event) { exitWaitingList(client, "SUB_ACTIVE_ID"); }

    @Override @FXML public void viewOrderHistory(ChatClient client) {
        ArrayList<String> message = new ArrayList<>();
        message.add("GET_SUBSCRIBER_HISTORY");
        client.handleMessageFromClientUI(message);
    }

    @Override @FXML public void editPersonalDetails(ChatClient client) { }
    @FXML void clickHistory(ActionEvent event) { viewOrderHistory(client); }
    @FXML void clickEditProfile(ActionEvent event) { editPersonalDetails(client); }

    @FXML
    void clickLogout(ActionEvent event) {
        navigateToPortal(event);
    }

    @Override
    public void display(Object message) {
        if (message != null) appendLog(message.toString());
    }

    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }

    private void navigateToPortal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml"));
            Parent root = loader.load();
            RemoteLoginController controller = loader.getController();
            controller.setClient(client);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            // תיקון נתיב CSS
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            stage.setTitle("Bistro - Remote Access Portal");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); 
            appendLog("Error: " + e.getMessage());
        }
    }
}