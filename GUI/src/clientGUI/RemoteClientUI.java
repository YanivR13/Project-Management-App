package clientGUI;

import client.ChatClient;
import clientGUI.Controllers.RemoteLoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import common.Reservation;

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
        // 1. טעינת ה-FXML והבקר
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmlFiles/RemoteLoginFrame.fxml"));
        Parent root = loader.load();
        RemoteLoginController controller = loader.getController();

        // 2. הכנת הסצנה והצגה מיידית של החלון
        Scene scene = new Scene(root);
        
        // הערה: נשאיר את ה-CSS כבוי בינתיים לבדיקה
        // scene.getStylesheets().add(getClass().getResource("cssStyle/style.css").toExternalForm());
        
        primaryStage.setTitle("Bistro - Remote Access Portal");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        
        // הצגת המסך עכשיו תבטיח שהמשתמש יראה את הכפתורים מיד
        primaryStage.show();

        // 3. ניסיון חיבור לשרת (רק אחרי שהמסך כבר מוצג)
        try {
            client = new ChatClient("localhost", 5555, controller);
            controller.setClient(client);
            controller.appendLog("Connected to server successfully.");
        } catch (Exception e) {
            // אם החיבור נכשל, עדיין נראה את המסך אבל תופיע הודעת שגיאה בלוג
            if (controller != null) {
                controller.appendLog("Status: Offline - Could not connect to server.");
            }
            e.printStackTrace();
        }
    }
}