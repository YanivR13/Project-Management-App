package serverLogic.menuLogic;

import java.io.IOException;
import java.util.ArrayList;

import common.Reservation;
import common.ServiceResponse;
import dbLogic.restaurantDB.CreateOrderController;
import ocsf.server.ConnectionToClient;

/**
 * The CreateOrderHandler class is a specialized service handler within the server's 
 * command-routing architecture. It specifically manages the "CREATE_RESERVATION" command.
 * * Role: It acts as an intermediary that:
 * 1. Decapsulates the {@link Reservation} DTO from the incoming OCSF message.
 * 2. Coordinates with the database controller to execute business rules (availability, upgrades).
 * 3. Transmits a structured {@link ServiceResponse} back to the client.
 * * This design ensures that the {@link MainControllers.ServerController} remains 
 * clean and focused only on routing, while specific logic is handled here.
 * * @author Software Engineering Student
 * @version 1.0
 */
public class CreateOrderHandler {

    /**
     * Entry point for processing a reservation creation request.
     * Extracts the data, triggers the business logic, and responds to the client.
     * * @param messageList The ArrayList protocol: [0] "CREATE_RESERVATION", [1] {@link Reservation} object.
     * @param client      The specific OCSF {@link ConnectionToClient} handle.
     */
    public void handle(ArrayList<Object> messageList, ConnectionToClient client) {
        try {
            /**
             * STEP 1: Object Extraction
             * The protocol expects a Reservation DTO at index 1.
             * This object contains the user_id (int) and party details needed for the DB.
             */
            Reservation reservation = (Reservation) messageList.get(1);

            /**
             * STEP 2: Business Logic Delegation
             * We delegate the complex work (checking table inventory, lookahead logic, 
             * and SQL insertion) to the CreateOrderController.
             * This controller returns a ServiceResponse, which is our standardized "envelope".
             */
            ServiceResponse result = CreateOrderController.processNewReservation(reservation);

            /**
             * STEP 3: Response Transmission
             * We send the structured ServiceResponse back to the client.
             * This allows the JavaFX UI to use the 'status' enum to decide which 
             * popup (Success/Suggestion/Full) to show.
             */
            client.sendToClient(result);

        } catch (ClassCastException e) {
            /**
             * Error Handling: Protocol Violation.
             * Triggered if the object at index 1 is not a Reservation instance.
             */
            System.err.println("CreateOrderHandler Error: Expected Reservation object at index 1.");
            sendErrorMessage(client, "INTERNAL_ERROR: Invalid reservation data format.");
        } catch (IOException e) {
            /**
             * Error Handling: Network Failure.
             * Triggered if the OCSF socket connection is lost during transmission.
             */
            System.err.println("CreateOrderHandler Error: Failed to send response to client.");
            e.printStackTrace();
        } catch (Exception e) {
            /**
             * General Exception Safety:
             * Ensures the server provides feedback to the client even during unexpected logic failures.
             */
            e.printStackTrace();
            sendErrorMessage(client, "INTERNAL_ERROR: Server encountered an unexpected issue.");
        }
    }

    /**
     * Helper method to send standardized error responses to the client.
     * Ensures that even in failure, the client receives a ServiceResponse object
     * that it can parse without crashing.
     * * @param client  The network connection to the client.
     * @param message The descriptive error message.
     */
    private void sendErrorMessage(ConnectionToClient client, String message) {
        try {
            // Create a failure-state response following the system protocol
            ServiceResponse errorResponse = new ServiceResponse(
                ServiceResponse.ReservationResponseStatus.INTERNAL_ERROR, 
                message
            );
            client.sendToClient(errorResponse);
        } catch (IOException e) {
            System.err.println("Critical Error: Could not send error response to client: " + e.getMessage());
        }
    }
}