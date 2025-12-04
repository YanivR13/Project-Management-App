package gui;

import client.ChatClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. טעינת קובץ העיצוב (FXML)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("OrderFrame.fxml"));
        Parent root = loader.load();

        // 2. קבלת ה-Controller שיצרנו הרגע
        OrderFrameController controller = loader.getController();

        // 3. יצירת החיבור לשרת (הלוגיקה)
        // אנחנו מעבירים את ה-controller כמי שיודע להציג (ChatIF)
        try {
            ChatClient client = new ChatClient("localhost", 5555, controller);
            
            // 4. חיבור הלקוח לתוך הקונטרולר (כדי שהכפתורים יעבדו)
            controller.setClient(client);
            
        } catch (Exception e) {
            System.out.println("Could not connect to server: " + e.getMessage());
            // נמשיך בכל זאת כדי להראות את החלון (גם אם לא מחובר)
        }

        // 5. בניית החלון והצגתו
        Scene scene = new Scene(root);
        
        // טעינת ה-CSS שיצרנו
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        
        primaryStage.setTitle("Order Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}