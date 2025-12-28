package clientGUI.Controllers;

import client.ChatClient;
import clientGUI.Controllers.MenuControlls.BaseMenuController;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The ICustomerActions interface defines the behavioral contract for customer-related activities.
 * It utilizes Java 8 default methods to provide a centralized navigation engine, 
 * reducing code duplication across different customer portal controllers (e.g., Subscriber vs Occasional).
 * * <p>This interface handles the transition between different functional frames in the 
 * restaurant management system while ensuring session data (client, userType, userId) 
 * is correctly propagated.</p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public interface ICustomerActions {

    /**
     * Navigates the user to the New Reservation screen.
     * * @param client   The active OCSF network client.
     * @param event    The ActionEvent from the UI button click.
     * @param userType The category of the user (e.g., "Subscriber").
     * @param userId   The unique database ID of the user.
     */
    default void createNewReservation(ChatClient client, ActionEvent event, String userType, int userId) {
        navigateTo(client, event, userType, userId, "/clientGUI/fxmlFiles/MenuFXML/NewReservationFrame.fxml", "Bistro - New Reservation");
    }

    /**
     * Navigates the user to the Reservation Cancellation screen.
     */
    default void payBill(ChatClient client, ActionEvent event, String userType, int userId) {
        navigateTo(client, event, userType, userId, "/clientGUI/fxmlFiles/MenuFXML/CancelReservationFrame.fxml", "Bistro - Cancel Reservation");
    }

    /**
     * Navigates the user to the screen where they can view existing reservations or process payments.
     */
    default void viewReservation(ChatClient client, ActionEvent event, String userType, int userId) {
        navigateTo(client, event, userType, userId, "/clientGUI/fxmlFiles/MenuFXML/ViewReservationFrame.fxml", "Bistro - View & Pay");
    }
    
    /**
     * Abstract method to be implemented for retrieving past reservation data from the DB.
     */
    void viewOrderHistory(ChatClient client, int userId);

    /**
     * Abstract method to be implemented for allowing users to update their profile info.
     */
    void editPersonalDetails(ChatClient client, int userId);
    
    /**
     * Sends a command to the server to remove a customer from a digital waiting list.
     * Uses the OCSF communication protocol (ArrayList containing Command and Data).
     * * @param client           The network client.
     * @param confirmationCode The code identifying the specific waiting list entry.
     */
    default void exitWaitingList(ChatClient client, String confirmationCode) {
    	
    }

    /**
     * The Central Navigation Engine.
     * Handles FXML loading, Controller initialization, and Stage switching.
     * * @param client   The network client instance.
     * @param event    The trigger event used to locate the current Window/Stage.
     * @param userType User category string.
     * @param userId   User database ID.
     * @param path     The relative path to the FXML layout file.
     * @param title    The text to display on the new window's title bar.
     */
    default void navigateTo(ChatClient client, ActionEvent event, String userType, int userId, String path, String title) {
        try {
            // Step 1: Initialize the FXML Loader with the target resource path
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            
            // Step 2: Access the controller of the newly loaded scene
            Object controller = loader.getController();
            
            /**
             * Dependency Injection:
             * If the target controller inherits from BaseMenuController, we inject 
             * the current session state to ensure continuity across the application.
             */
            if (controller instanceof BaseMenuController) {
                ((BaseMenuController) controller).setClient(client, userType, userId);
            }

            // Step 3: Scene and Stage Transition
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            
            // Apply global CSS for consistent look and feel
            scene.getStylesheets().add(getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm());
            
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            // Logs critical errors during scene loading (e.g., missing FXML or CSS files)
            e.printStackTrace();
        }
    }
}