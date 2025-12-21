package clientGUI.Controllers;

import client.ChatClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface ICustomerActions {

    default void createNewReservation(ChatClient client, ActionEvent event, String userType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/ReservationFXML/NewReservationFrame.fxml"));
            Parent root = loader.load();
            clientGUI.Controllers.ReservationControlls.NewReservationController controller = loader.getController();
            controller.setClient(client, userType);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            // תיקון נתיב CSS
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            stage.setTitle("Bistro - New Reservation");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    default void cancelReservation(ChatClient client, ActionEvent event, String userType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/ReservationFXML/CancelReservationFrame.fxml"));
            Parent root = loader.load();
            clientGUI.Controllers.ReservationControlls.CancelReservationController controller = loader.getController();
            controller.setClient(client, userType);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            // תיקון נתיב CSS
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            stage.setTitle("Bistro - Cancel Reservation");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    default void viewReservation(ChatClient client, ActionEvent event, String userType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/ReservationFXML/ViewReservationFrame.fxml"));
            Parent root = loader.load();
            clientGUI.Controllers.ReservationControlls.ViewReservationController controller = loader.getController();
            controller.setClient(client, userType);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            // תיקון נתיב CSS
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            stage.setTitle("Bistro - View & Pay");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    default void exitWaitingList(ChatClient client, String confirmationCode) {
        java.util.ArrayList<String> message = new java.util.ArrayList<>();
        message.add("EXIT_WAITING_LIST");
        message.add(confirmationCode);
        if (client != null) client.handleMessageFromClientUI(message);
    }

    void viewOrderHistory(ChatClient client);
    void editPersonalDetails(ChatClient client);
}