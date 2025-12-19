package clientGUI.Controllers;

import java.util.ArrayList;
import client.ChatClient;

/**
 * Interface defining the behavioral contract for all customer types.
 * Standard restaurant actions are provided as default methods to ensure code reuse.
 */
public interface ICustomerActions {

    /**
     * Shared logic for initiating a new reservation.
     */
    default void createNewReservation(ChatClient client) {
        // Logic to transition to the reservation form will be added here
        System.out.println("Switching to New Reservation Screen...");
    }

    /**
     * Shared logic for canceling an existing reservation via server communication.
     */
    default void cancelReservation(ChatClient client, String confirmationCode) {
        ArrayList<String> message = new ArrayList<>();
        message.add("CANCEL_RESERVATION");
        message.add(confirmationCode);
        if (client != null) {
            client.handleMessageFromClientUI(message);
        }
    }

    /**
     * Shared logic for viewing reservation details from the database.
     */
    default void viewReservation(ChatClient client, String confirmationCode) {
        ArrayList<String> message = new ArrayList<>();
        message.add("GET_RESERVATION_DETAILS");
        message.add(confirmationCode);
        if (client != null) {
            client.handleMessageFromClientUI(message);
        }
    }

    /**
     * Shared logic for removing a customer from a waiting list.
     */
    default void exitWaitingList(ChatClient client, String confirmationCode) {
        ArrayList<String> message = new ArrayList<>();
        message.add("EXIT_WAITING_LIST");
        message.add(confirmationCode);
        if (client != null) {
            client.handleMessageFromClientUI(message);
        }
    }

    /**
     * Subscriber-only method: viewing past visits and orders.
     */
    void viewOrderHistory(ChatClient client);

    /**
     * Subscriber-only method: updating personal profile information.
     */
    void editPersonalDetails(ChatClient client);
}