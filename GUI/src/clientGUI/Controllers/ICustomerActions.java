package clientGUI.Controllers;

import client.ChatClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Final Interface for all customer types.
 * Standardizes navigation to shared reservation screens across the GUI project.
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
     * Navigates to the shared Cancellation screen (using TableView).
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
     * Navigates to the shared View & Pay screen.
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
            stage.setTitle("Bistro - View & Pay");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Navigation error (View/Pay): " + e.getMessage());
        }
    }

    /**
     * Shared logic for removing a customer from a waiting list.
     */
    default void exitWaitingList(ChatClient client, String confirmationCode) {
        java.util.ArrayList<String> message = new java.util.ArrayList<>();
        message.add("EXIT_WAITING_LIST");
        message.add(confirmationCode);
        if (client != null) {
            client.handleMessageFromClientUI(message);
        }
    }

    // Abstract methods to be implemented by specific controllers (Subscriber vs Occasional)
    void viewOrderHistory(ChatClient client);
    void editPersonalDetails(ChatClient client);
}