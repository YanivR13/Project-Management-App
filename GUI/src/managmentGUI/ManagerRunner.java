package managmentGUI; // הצהרה על החבילה שבה נמצא ה-Runner

import client.ChatClient; // ייבוא מחלקת הלקוח של OCSF לתקשורת
import javafx.application.Application; // ייבוא מחלקת הבסיס של JavaFX
import javafx.fxml.FXMLLoader; // ייבוא ה-Loader לטעינת קבצי FXML
import javafx.scene.Parent; // ייבוא ה-Root node עבור ה-Scene
import javafx.scene.Scene; // ייבוא מחלקת ה-Scene
import javafx.stage.Stage; // ייבוא מחלקת ה-Stage הראשית

/**
 * נקודת הכניסה הראשית עבור דשבורד המנהל.
 * מחלקה זו מטפלת בחיבור הראשוני לשרת ובטעינת ממשק המנהל.
 */
public class ManagerRunner extends Application { 

    /**
     * מתודת ה-start מאתחלת את החלון הראשי ומגדירה את ה-Pipe לתקשורת.
     */
    @Override 
    public void start(Stage primaryStage) { 
        
        try { 
            
            // --- שלב 1: אתחול תקשורת רשת ---
            
            // יצירת לקוח רשת זמני המצביע ל-localhost בפורט 5555
            ChatClient client = new ChatClient("localhost", 5555, null); //

            // --- שלב 2: טעינת ממשק משתמש (UI) ---
            
            // אתחול ה-Loader עם הנתיב לקובץ ה-FXML של דשבורד המנהל
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ManagerDashboard.fxml")); 
            
            // טעינת הקובץ ויצירת ה-Root node הויזואלי
            Parent root = loader.load(); 

            // --- שלב 3: הזרקת נתונים (The Pipe) ---
            
            // גישה למופע של הקונטרולר שנוצר על ידי ה-FXMLLoader
            ManagerDashboardController controller = loader.getController(); 
            
            /**
             * הזרקת נתוני סשן:
             * אנו מגדירים את סוג המשתמש כ-"Manager".
             * בהתאם ללוגיקה של BaseMenuController, זה יגדיר את userId ל-0 (או -1 כפי שצוין בנציג).
             */
            controller.setClient(client, "Manager", 0); //

            // --- שלב 4: תצוגת החלון ---
            
            // הגדרת כותרת לחלון המנהל
            primaryStage.setTitle("Bistro System - Manager Mode"); 
            
            // הגדרת ה-Scene והצגת החלון
            primaryStage.setScene(new Scene(root)); 
            primaryStage.show(); 

        } catch (Exception e) { 
            // הדפסת שגיאה במקרה של כישלון בטעינה או בחיבור
            System.err.println("Error launching Manager Dashboard:"); 
            e.printStackTrace(); 
        } 
    } 

    /**
     * נקודת הכניסה הסטנדרטית של ה-JVM.
     */
    public static void main(String[] args) { 
        launch(args); 
    } 
}