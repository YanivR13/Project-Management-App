package clientGUI.Controllers;

import client.ChatClient;
import clientGUI.Controllers.OccasionalControlls.OccasionalLoginController;
import clientGUI.Controllers.SubscriberControlls.SubscriberLoginController;
import javafx.event.ActionEvent;
import common.ChatIF;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.Node;

public class RemoteLoginController implements ChatIF {
    private ChatClient client;

    @FXML
    private TextArea txtLog;

    public void setClient(ChatClient client) {
        this.client = client;
    }

    @Override
    public void display(Object message) {
        if (message != null) {
            appendLog(message.toString());
        }
    }

    public void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("> " + message + "\n"));
    }

    @FXML
    void clickOccasional(ActionEvent event) {
        loadScreen(event, "OccasionalLoginFrame.fxml", "Occasional Customer Login");
    }

    @FXML
    void clickSubscriber(ActionEvent event) {
        loadScreen(event, "SubscriberLoginFrame.fxml", "Subscriber Login");
    }

    private void loadScreen(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/" + fxmlFile));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            
            if (controller instanceof SubscriberLoginController) {
                ((SubscriberLoginController) controller).setClient(client);
            } else if (controller instanceof OccasionalLoginController) {
                ((OccasionalLoginController) controller).setClient(client);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/style.css").toExternalForm());
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            appendLog("Error: " + e.getMessage());
        }
    }
}