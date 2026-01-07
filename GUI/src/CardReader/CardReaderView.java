package CardReader;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CardReaderView extends Application {

    private Stage primaryStage;
    
   
    private CardReaderController controller = new CardReaderController();
    private String currentSubscriberID; 

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
            if (controller.validateSubscriber(id)) {
                this.currentSubscriberID = id;
                showSubscriberMenu();
            } else {
                System.out.println("Subscriber not found!");
            }
        });

        layout.getChildren().addAll(title, instruction, idInput, loginBtn);
        setupAndShowScene(layout, "Card Reader - Login");
    }

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

        List<String> codes = controller.getLostConfirmationCodes();
        
        VBox codesList = new VBox(10);
        codesList.setAlignment(Pos.CENTER);

        if (codes == null || codes.isEmpty()) {
            codesList.getChildren().add(new Label("No active orders found for your ID."));
        } else {
            for (String codeInfo : codes) {
                Label codeLabel = new Label(codeInfo);
                codeLabel.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #ff9800; -fx-border-radius: 5;");
                codesList.getChildren().add(codeLabel);
            }
        }

        Button backBtn = new Button("Back to Menu");
        backBtn.getStyleClass().add("connect-button");
        backBtn.setOnAction(e -> showSubscriberMenu());

        layout.getChildren().addAll(title, codesList, backBtn);
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

        Label messageLabel = new Label(""); 
        Button submitBtn = new Button("Confirm Arrival");
        submitBtn.getStyleClass().add("login-button"); 
        
        submitBtn.setOnAction(e -> {
            String result = controller.verifyConfirmationCode(codeInput.getText());
            messageLabel.setText(result);
            if (result.startsWith("Success")) {
                messageLabel.setStyle("-fx-text-fill: green;");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("connect-button");
        backBtn.setOnAction(e -> showSubscriberMenu());

        layout.getChildren().addAll(title, codeInput, submitBtn, messageLabel, backBtn);
        setupAndShowScene(layout, "Enter Code");
    }

    public static void main(String[] args) {
        launch(args);
    }
}