package CardReader;


import java.util.List;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import client.ChatClient; // מייבא את מחלקת הלקוח מהחבילה שלו
import CardReader.CardReaderController; // מייבא את הקונטרולר של הטרמינל






public class CardReaderView extends Application implements common.ChatIF {

    private Stage primaryStage;
    
    private CardReaderController controller = new CardReaderController();
    private String currentSubscriberID; 

    
 // --- משתנים חדשים שנוסיף כדי שנוכל לעדכן אותם מהשרת ---
    private Label loginStatusLabel = new Label(""); // להודעות שגיאה בלוגין
    private Label verifyMessageLabel = new Label(""); // להודעות הצלחה/כישלון של קוד
    private VBox codesContainer = new VBox(10); // המכולה שתציג את רשימת הקודים האבודים
    
    
    
    
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showWelcomeScreen();
    }

    private void showWelcomeScreen() {
        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("main-background");

        Label title = new Label("Card Reader Simulation");
        title.getStyleClass().add("title-label");

        Button connectBtn = new Button("Connect");
        connectBtn.getStyleClass().add("connect-button");
        connectBtn.setOnAction(e -> showLoginScreen());

        layout.getChildren().addAll(title, connectBtn);
        setupAndShowScene(layout, "Card Reader - Welcome");
    }

    private void showLoginScreen() {
    	
    	    // שלב קריטי: רישום ה-UI הנוכחי בתוך הלקוח כדי שמתודת ה-display תופעל
    	    if (CardReaderController.getClient() != null) {
    	        CardReaderController.getClient().setUI(this);
    	    } else {
    	        System.err.println("Error: ChatClient is not initialized!");
    	    }

    	    // ... שאר הקוד של ה-VBox והכפתורים שבנית קודם ...
    	
    	
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("main-background");
        
        Label title = new Label("Card Reader Simulation");
        title.getStyleClass().add("title-label");

        Label instruction = new Label("Please enter your Subscriber ID:");
        instruction.getStyleClass().add("instruction-label");

        TextField idInput = new TextField();
        idInput.setPromptText("Enter ID here...");
        idInput.getStyleClass().add("id-input-field");

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("login-button");
        
 

     // הקוד הנכון שצריך להיות:
     loginBtn.setOnAction(e -> {
         String id = idInput.getText();
         this.currentSubscriberID = id; 
         
         // פנייה לקונטרולר של הלקוח (הוא כבר יודע לשלוח הודעה לשרת)
         controller.validateSubscriber(id); 
         
         loginStatusLabel.setText("Connecting...");
     });
        
  // הוספנו את loginStatusLabel לרשימה כדי שנראה הודעות שגיאה
     layout.getChildren().addAll(title, instruction, idInput, loginBtn, loginStatusLabel);
        setupAndShowScene(layout, "Card Reader - Login");
    }

    
 // הוספנו את loginStatusLabel לרשימה כדי שנראה הודעות שגיאה
  
    
    private void showSubscriberMenu() {
        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("main-background");

        Label welcomeMsg = new Label("Subscriber Menu");
        welcomeMsg.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

   
        Button enterCodeBtn = new Button("Enter Confirmation Code");
        enterCodeBtn.getStyleClass().add("arrival-code-button");
        
        enterCodeBtn.setOnAction(e -> showEnterCodeScreen());

        Button lostCodeBtn = new Button("I lost my confirmation code");
        lostCodeBtn.getStyleClass().add("lost-code-button");
        lostCodeBtn.setOnAction(e -> showRecoveredCodesScreen());

        Button disconnectBtn = new Button("Disconnect");
        disconnectBtn.getStyleClass().add("disconnect-button"); 
        disconnectBtn.setOnAction(e -> showWelcomeScreen());

        layout.getChildren().addAll(welcomeMsg, enterCodeBtn, lostCodeBtn, disconnectBtn);
        setupAndShowScene(layout, "Subscriber Menu");
    }
    
    
    private void showRecoveredCodesScreen() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("main-background");

        Label title = new Label("Your Active Confirmation Codes");
        title.getStyleClass().add("title-label");

        // במקום ליצור VBox מקומי, נשתמש בזה שהגדרנו למעלה (הגלובלי)
        codesContainer.getChildren().clear();
        codesContainer.setAlignment(Pos.CENTER);
        codesContainer.getChildren().add(new Label("Fetching codes from server..."));

        // שלב 3: שליחת הבקשה לשרת (אין "List<String> codes ="!)
        controller.getLostConfirmationCodes(currentSubscriberID); 

        Button backBtn = new Button("Back to Menu");
        backBtn.getStyleClass().add("connect-button");
        backBtn.setOnAction(e -> showSubscriberMenu());

        layout.getChildren().addAll(title, codesContainer, backBtn);
        setupAndShowScene(layout, "Recover Codes");
    }

    
    
    private void setupAndShowScene(VBox layout, String title) {
        Scene scene = new Scene(layout, 450, 450);
        String cssPath = "/CardReader/CardReader.css";
        if (getClass().getResource(cssPath) != null) {
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        }
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    
    
    private void showEnterCodeScreen() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("main-background");

        Label title = new Label("Enter Confirmation Code");
        title.getStyleClass().add("title-label");

        TextField codeInput = new TextField();
        codeInput.setPromptText("Type your code here (e.g. XY789)");
        codeInput.getStyleClass().add("id-input-field");

        // הסרנו את המשתנה המקומי messageLabel והשתמשנו ב-verifyMessageLabel הגלובלי
        verifyMessageLabel.setText(""); 
        
        Button submitBtn = new Button("Confirm Arrival");
        submitBtn.getStyleClass().add("login-button"); 
        
        submitBtn.setOnAction(e -> {
            // שלב 1: איפוס הודעה והצגת סטטוס זמני
            verifyMessageLabel.setText("Processing...");
            verifyMessageLabel.setStyle("-fx-text-fill: blue;");

            // שלב 2: שליחת הבקשה לשרת דרך הקונטרולר
            controller.verifyConfirmationCode(codeInput.getText(), currentSubscriberID); 
        });
        
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("connect-button");
        backBtn.setOnAction(e -> showSubscriberMenu());

        // כאן התיקון הקריטי: הוספת verifyMessageLabel ל-layout
        layout.getChildren().addAll(title, codeInput, submitBtn, verifyMessageLabel, backBtn);
        setupAndShowScene(layout, "Enter Code");
    }
    
    
    @Override
    public void display(Object message) {
        Platform.runLater(() -> {
            // 1. טיפול בתוצאת לוגין (Boolean)
            if (message instanceof Boolean) {
                if ((Boolean) message) {
                    showSubscriberMenu();
                } else {
                    loginStatusLabel.setText("Login Failed!");
                }
            } 
            // 2. טיפול בשחזור קודים אבודים (List) - מציג את הרשימה על המסך
            else if (message instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> codes = (List<String>) message;
                codesContainer.getChildren().clear(); // מנקה את הודעת ה-"Fetching..."
                
                if (codes.isEmpty()) {
                    codesContainer.getChildren().add(new Label("No active codes found."));
                } else {
                    for (String codeStr : codes) {
                        Label codeLabel = new Label(codeStr);
                        codeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1565c0; -fx-font-weight: bold;");
                        codesContainer.getChildren().add(codeLabel);
                    }
                }
            }
            // 3. טיפול באימות קוד הגעה (String) - מציג הצלחה או כישלון
            else if (message instanceof String) {
                verifyMessageLabel.setText((String) message);
                if (((String) message).contains("Success")) {
                    verifyMessageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } else {
                    verifyMessageLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
    }
    
    private void appendLog(String message) {
        System.out.println("[LOG]: " + message);
    } 
    
    
    
    
    
    
 // המתודה המרכזית היחידה שצריכה להישאר!
    public static void main(String[] args) {
        try {
            // יצירת הלקוח (וודא שהשרת פועל!)
            ChatClient chatClient = new ChatClient("localhost", 5555, null); 
            
            // חיבור הלקוח לקונטרולר
            CardReaderController.setClient(chatClient);
            
            // הרצת הממשק
            launch(args);
        } catch (Exception e) {
            System.err.println("Connection failed! Displaying UI anyway...");
            launch(args); 
        }
    }
} // סוף הקלאס

    
    




