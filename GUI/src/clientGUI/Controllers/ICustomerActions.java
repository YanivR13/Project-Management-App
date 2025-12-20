package clientGUI.Controllers;

import client.ChatClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
// 1

/**
 * Interface defining the behavioral contract for all customer types.
 * Standard restaurant actions are provided as default methods to handle navigation to shared screens.
 */
public interface ICustomerActions {

    /**
     * Navigates to the shared New Reservation screen.
     */
    default void createNewReservation(ChatClient client, ActionEvent event, String userType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/ReservationFXML/NewReservationFrame.fxml"));
            Parent root = loader.load();

            clientGUI.Controllers.ReservationControlls.NewReservationController controller = loader.getController();
            controller.setClient(client, userType);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/style.css").toExternalForm());
            stage.setTitle("Bistro - New Reservation");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Navigation error (New Res): " + e.getMessage());
        }
    }

    /**
     * Navigates to the shared Cancellation screen (showing the TableView).
     */
    default void cancelReservation(ChatClient client, ActionEvent event, String userType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/ReservationFXML/CancelReservationFrame.fxml"));
            Parent root = loader.load();

            clientGUI.Controllers.ReservationControlls.CancelReservationController controller = loader.getController();
            controller.setClient(client, userType);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/style.css").toExternalForm());
            stage.setTitle("Bistro - Cancel Reservation");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Navigation error (Cancel): " + e.getMessage());
        }
    }

    /**
     * Navigates to the shared View Reservation details screen.
     */
    default void viewReservation(ChatClient client, ActionEvent event, String userType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/ReservationFXML/ViewReservationFrame.fxml"));
            Parent root = loader.load();

            clientGUI.Controllers.ReservationControlls.ViewReservationController controller = loader.getController();
            controller.setClient(client, userType);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/style.css").toExternalForm());
            stage.setTitle("Bistro - View Details");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Navigation error (View): " + e.getMessage());
        }
    }

    /**
     * Shared logic for removing a customer from a waiting list (Action only, no new screen).
     */
    default void exitWaitingList(ChatClient client, String confirmationCode) {
        java.util.ArrayList<String> message = new java.util.ArrayList<>();
        message.add("EXIT_WAITING_LIST");
        message.add(confirmationCode);
        if (client != null) {
            client.handleMessageFromClientUI(message);
        }
    }

    void viewOrderHistory(ChatClient client);
    void editPersonalDetails(ChatClient client);
}