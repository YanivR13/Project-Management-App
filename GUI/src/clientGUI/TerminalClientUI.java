package clientGUI;

import client.ChatClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import terminalGUI.Controllers.TerminalControllers.TerminalLoginController;
import terminalGUI.Controllers.TerminalControllers.TerminalMenuController;

/**
 * Main entry point for the Bistro Customer Service Terminal application.
 * This class launches the JavaFX UI for the in-restaurant self-service terminal.
 *
 * Responsibilities:
 * 1. Bootstrapping the JavaFX application lifecycle.
 * 2. Loading and displaying the Terminal main screen.
 * 3. Initializing the ChatClient for server communication.
 * 4. Injecting the client into the Terminal controller.
 */
public class TerminalClientUI extends Application {

    /** Persistent network client instance */
    private ChatClient client;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // --- UI Initialization Phase ---

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("fxmlFiles/TerminalLoginFrame.fxml")
        );
        Parent root = loader.load();

        // Get controller
        TerminalLoginController controller = loader.getController();

        // Scene setup
        Scene scene = new Scene(root);
        primaryStage.setTitle("Bistro - Customer Service Terminal");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

        // --- Network Initialization Phase ---
        try {
            client = new ChatClient("localhost", 5555, controller);
            controller.setClient(client);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
