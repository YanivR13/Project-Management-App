package clientGUI;

import client.ChatClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX entry point for the Client application.
 * 
 * This class loads the GUI (FXML), initializes the controller,
 * creates the ChatClient instance, and connects the UI layer
 * with the communication layer.
 */
public class ClientUI extends Application {

    /**
     * Launches the JavaFX application.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes and displays the main client window.
     *
     * @param primaryStage The main window (Stage) created by JavaFX.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        // 1. Load the FXML layout for the client window.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("OrderFrame.fxml"));
        Parent root = loader.load();

        // 2. Retrieve the controller associated with the FXML file.
        OrderFrameController controller = loader.getController();

        // 3. Create a ChatClient instance to communicate with the server.
        // The controller is passed as the ChatIF implementation,
        // allowing it to receive messages from the server.
        try {
            ChatClient client = new ChatClient("localhost", 5555, controller);

            // 4. Provide the ChatClient to the controller
            // so GUI elements (buttons) can send messages to the server.
            controller.setClient(client);

            // Optional: Notify the user that the connection succeeded.
            controller.display("Connected to server!");

        } catch (Exception e) {
            System.out.println("Could not connect to server: " + e.getMessage());
            // The GUI still loads, even if the server is offline.
        }

        // 5. Create and display the JavaFX window.
        Scene scene = new Scene(root);

        // Load optional CSS styling.
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle("Order Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
