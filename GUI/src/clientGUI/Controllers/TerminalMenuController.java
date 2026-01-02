package clientGUI.Controllers;

import javafx.scene.control.Button;

import client.ChatClient;
import common.ChatIF;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
public class TerminalController implements ChatIF {

    /** Persistent network client (injected on application startup) */
    private ChatClient client;
    
    
    // FXML Button Bindings
    @FXML
    private Button btnLostReservationCode;
    
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
    private void handleLostReservationCode(ActionEvent event) {
        // TODO: implement reservation code recovery flow
    }
    
    /**
     * Handles "Manage Reservation" button click.
     */
    @FXML
    private void handleManageReservation(ActionEvent event) {
        // TODO: implement reservation management flow
    }
    
    /**
     * Handles "Join Waiting List" button click.
     */
    @FXML
    private void handleJoinWaitingList(ActionEvent event) {
        // TODO: implement waiting list registration flow
    }
    
    /**
     * Handles "I'm Here" button click.
     */
    @FXML
    private void handleArrival(ActionEvent event) {
        // TODO: implement arrival confirmation flow
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
