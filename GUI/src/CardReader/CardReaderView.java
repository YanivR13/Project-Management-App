package CardReader;


import java.util.List;

import javafx.geometry.Pos;
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

    
    
    private void showSubscriberMenu() { // שים לב שזה void ולא Object
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
	    try {
	        System.out.println(">>> Attempting to show Enter Code Screen...");

	        VBox layout = new VBox(20);
	        layout.setAlignment(Pos.CENTER);
	        layout.getStyleClass().add("main-background");

	        Label title = new Label("Enter Confirmation Code");
	        title.getStyleClass().add("title-label");

	        TextField codeInput = new TextField();
	        codeInput.setPromptText("Type your code here (e.g. 12345)");
	        codeInput.getStyleClass().add("id-input-field");

	        // תיקון קריטי: מאפסים את הטקסט של הלייבל הגלובלי לפני השימוש
	        verifyMessageLabel.setText("");
	        verifyMessageLabel.setStyle("-fx-text-fill: black;");

	        Button submitBtn = new Button("Confirm Arrival");
	        submitBtn.getStyleClass().add("login-button");
	        submitBtn.setOnAction(e -> {
	            verifyMessageLabel.setText("Processing...");
	            controller.verifyConfirmationCode(codeInput.getText(), currentSubscriberID); 
	        });

	        Button backBtn = new Button("Back");
	        backBtn.getStyleClass().add("connect-button");
	        backBtn.setOnAction(e -> showSubscriberMenu());

	        // לפני שמוסיפים את verifyMessageLabel, אנחנו מוודאים שהוא לא "תקוע" ב-Layout ישן
	        layout.getChildren().addAll(title, codeInput, submitBtn, verifyMessageLabel, backBtn);

	        System.out.println(">>> Switching scene now...");
	        setupAndShowScene(layout, "Enter Code");

	    } catch (Exception ex) {
	        System.err.println(">>> ERROR during screen transition:");
	        ex.printStackTrace(); // זה יראה לנו ב-Console את השגיאה המדויקת באדום
	    }
	}
    
    
//abc
	@Override
	public void display(Object message) {
	    System.out.println(">>> MESSAGE RECEIVED FROM SERVER: " + message);

	    Platform.runLater(() -> {
	        try {
	            // 1. טיפול בתוצאת לוגין (רשימה שמתחילה ב-LOGIN_SUCCESS)
	            if (message instanceof List && !((List<?>) message).isEmpty()) {
	                List<?> res = (List<?>) message;
	                if ("LOGIN_SUCCESS".equals(res.get(0))) {
	                    showSubscriberMenu();
	                    return;
	                }
	                
	                // 2. טיפול ברשימת קודים אבודים (שחזור קוד)
	                if (res.get(0) instanceof String && !res.get(0).toString().startsWith("LOGIN")) {
	                    @SuppressWarnings("unchecked")
	                    List<String> codes = (List<String>) message;
	                    codesContainer.getChildren().clear(); 
	                    if (codes.isEmpty()) {
	                        codesContainer.getChildren().add(new Label("No active codes found."));
	                    } else {
	                        for (String codeStr : codes) {
	                            Label codeLabel = new Label( codeStr);
	                            codeLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #1565c0; -fx-font-weight: bold;");
	                            codesContainer.getChildren().add(codeLabel);
	                        }
	                    }
	                    return;
	                }
	            }

	            // 3. טיפול בהודעות שגיאה או אימות הגעה (String)
	            if (message instanceof String) {
	                String response = (String) message;
	                // הצגת שגיאת לוגין באדום כפי שרואים בצילום המסך שלך
	                if (response.contains("not found") || response.contains("ERROR")) {
	                    loginStatusLabel.setText("Login Error: Subscriber ID not found.");
	                    loginStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
	                } else if (response.startsWith("SUCCESS_TABLE_")) {
	                    String tableId = response.split("_")[2];
	                    verifyMessageLabel.setText("Success! Table #" + tableId);
	                    verifyMessageLabel.setStyle("-fx-text-fill: green;");
	                } else {
	                    verifyMessageLabel.setText(response);
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
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

    
    




