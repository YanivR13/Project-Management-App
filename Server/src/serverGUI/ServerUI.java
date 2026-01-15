package serverGUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerPortFrame.fxml"));
        Parent root = loader.load();
        
        ServerPortFrameController controller = loader.getController();
        
        Scene scene = new Scene(root);

        primaryStage.setTitle("Server Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}