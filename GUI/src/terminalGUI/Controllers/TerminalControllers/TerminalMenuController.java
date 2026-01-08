package terminalGUI.Controllers.TerminalControllers;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import client.ChatClient;
import clientGUI.Controllers.MenuControlls.BaseMenuController;
import common.ChatIF;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;;

/**
 * Minimal controller for the Customer Service Terminal screen.
 *
 * This controller currently serves as a placeholder to:
 * 1. Allow the Terminal UI to load correctly.
 * 2. Maintain architectural consistency with other client controllers.
 * 3. Support future expansion (button actions, navigation, server requests).
 *
 * No button logic is implemented at this stage.
 *
 * @author Software Engineering Student
 * @version 1.0
 */
public class TerminalMenuController extends BaseMenuController implements ChatIF {

    /** Persistent network client (injected on application startup) */
    
    
    // FXML Button Bindings
    @FXML
    private Button btnLostConfirmationCode;
    
    @FXML
    private Button btnManageReservation;

    @FXML
    private Button btnJoinWaitingList;

    @FXML
    private Button btnArrival;
   

    /**
     * Injects the shared ChatClient instance into the controller.
     *
     * @param client Active network client
     */
    public void setClient(ChatClient client) {
        this.client = client;
    }
    
    /**
     * Handles "Lost Reservation Code" button click.
     */
    @FXML
    private void handleLostConfirmationCode(ActionEvent event) {
        // TODO: implement reservation code recovery flow
    }
    
    /**
     * Handles "Manage Reservation" button click.
     */
    @FXML
    private void handleManageReservation(ActionEvent event) {
    	try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/Terminal/ManageReservationFrame.fxml"));
            Parent root = loader.load();

            ManageReservationController controller = loader.getController();
            controller.setClient(this.client, "Terminal", -1); 

            Stage stage = (Stage) btnManageReservation.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Bistro - Manage Reservation");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Handles "Join Waiting List" button click.
     */
    @FXML
    private void handleJoinWaitingList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/clientGUI/fxmlFiles/Terminal/TerminalWaitingListSizeFrame.fxml"
                )
            );

            Parent root = loader.load();

            // Inject ChatClient
            TerminalWaitingListSizeController controller =
                    loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Join Waiting List");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }    
    }
    
    /**
     * Handles "I'm Here" button click.
     */
    @FXML
    private void handleArrival(ActionEvent event) {
    	try {
            // 1. Load the Arrival Code Entry screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/Terminal/TerminalArrivalFrame.fxml"));
            Parent root = loader.load();

            // 2. Access the new controller and inject the ChatClient
            VisitUIController controller = loader.getController();
            controller.setClient(client);
            controller.onClientReady(); // Register this screen to receive server messages

            // 3. Switch the Scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Bistro - Customer Arrival");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Receives messages from the server.
     * Currently logs messages to stdout (placeholder behavior).
     *
     * @param message Incoming server message
     */
    @Override
    public void display(Object message) {
        if (message != null) {
            Platform.runLater(() ->
                System.out.println("[Terminal] " + message)
            );
        }
    }
}
