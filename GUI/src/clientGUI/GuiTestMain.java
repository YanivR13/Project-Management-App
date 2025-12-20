package clientGUI;

import clientGUI.Controllers.RemoteLoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class GuiTestMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Paths to try
        String[] pathsToTry = {
            "/clientGUI/fxmlFiles/RemoteLoginFrame.fxml", // Option A
            "/fxmlFiles/RemoteLoginFrame.fxml",           // Option B
            "fxmlFiles/RemoteLoginFrame.fxml"             // Relative to package
        };

        URL fxmlLocation = null;
        for (String path : pathsToTry) {
            fxmlLocation = getClass().getResource(path);
            if (fxmlLocation != null) {
                System.out.println("SUCCESS: Found FXML at: " + path);
                break;
            } else {
                System.out.println("DEBUG: Not found at: " + path);
            }
        }

        if (fxmlLocation == null) {
            System.err.println("CRITICAL: RemoteLoginFrame.fxml could not be located in any path.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            
            RemoteLoginController controller = loader.getController();
            controller.setClient(null); 

            Scene scene = new Scene(root);
            // Verify CSS path as well
            URL cssResource = getClass().getResource("/clientGUI/cssStyle/style.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            primaryStage.setTitle("Bistro - UI Test Mode");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}