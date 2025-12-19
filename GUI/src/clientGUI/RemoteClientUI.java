package clientGUI;

import client.ChatClient;
import clientGUI.Controllers.RemoteLoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the Remote Access Application.
 */
public class RemoteClientUI extends Application {
    private ChatClient client;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Correct path to fxmlFiles folder
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmlFiles/RemoteLoginFrame.fxml"));
        Parent root = loader.load();
        
        RemoteLoginController controller = loader.getController();

        try {
            // Chain: GUI -> CLIENT
            client = new ChatClient("localhost", 5555, controller);
            controller.setClient(client);
            controller.appendLog("Connected to server successfully.");
        } catch (Exception e) {
            controller.appendLog("Status: Offline - Could not connect to server.");
        }

        Scene scene = new Scene(root);
        // Correct path to cssStyle folder
        scene.getStylesheets().add(getClass().getResource("cssStyle/style.css").toExternalForm());
        
        primaryStage.setTitle("Bistro - Remote Access Portal");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}