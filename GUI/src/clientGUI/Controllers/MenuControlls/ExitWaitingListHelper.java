package clientGUI.Controllers.MenuControlls;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import client.ChatClient;
import java.util.ArrayList;

/**
 * A dedicated helper class to manage all waiting list operations 
 * for both Subscriber and Occasional menus.
 */
public class ExitWaitingListHelper {

    /**
     * Shows a confirmation dialog and sends a leave request to the server.
     */
    public static void requestLeaveWaitingList(ChatClient client, int userId) {
        Alert confirm = new Alert(AlertType.CONFIRMATION, 
            "Are you sure you want to leave the waiting list?", 
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Waiting List Confirmation");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                ArrayList<Object> message = new ArrayList<>();
                message.add("CANCEL_WAITING_LIST");
                message.add(userId);
                client.handleMessageFromClientUI(message);
            }
        });
    }

    /**
     * Handles server responses and displays the appropriate success/failure popup.
     */
    public static void handleServerResponse(String response) {
        Platform.runLater(() -> {
            switch (response) {
                case "CANCEL_WAITING_SUCCESS":
                    showPopup("Success", "Removed from waiting list successfully!", AlertType.INFORMATION);
                    break;
                case "NOT_ON_WAITING_LIST":
                    showPopup("Notice", "No active waiting entry found.", AlertType.WARNING);
                    break;
                case "SERVER_ERROR":
                    showPopup("Error", "Server error occurred. Please try again later.", AlertType.ERROR);
                    break;
            }
        });
    }

    /**
     * Internal helper to show the popup window.
     */
    private static void showPopup(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}