package serverLogic.menuLogic;

import ocsf.server.ConnectionToClient;
import java.util.ArrayList;
import java.util.List;

import dbLogic.restaurantDB.RestaurantDBController;
import common.Reservation;
/**
 * Handles server-side logic for fetching order history of a subscriber.
 * Triggered by the "GET_ORDER_HISTORY" command.
 */
public class ReservationHistoryHandler {

    /**
     * Message Protocol:
     * Incoming:
     *   [0] = "GET_ORDER_HISTORY"
     *   [1] = userId (int)
     *
     * Outgoing:
     *   [0] = "ORDER_HISTORY"
     *   [1] = List<Order>
     */
    public void handle(ArrayList<Object> data, ConnectionToClient client) {

        try {
            /* STEP 1: Extract userId */
            int userId = (int) data.get(1);

            /* STEP 2: DB delegation */
            RestaurantDBController db = new RestaurantDBController();
            List<Reservation> reservations = db.getReservationsForUser(userId);

            /* STEP 3: Build response */
            ArrayList<Object> response = new ArrayList<>();
            response.add("RESERVATION_HISTORY");
            response.add(reservations);

            /* STEP 4: Send back to client */
            client.sendToClient(response);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient("ERROR: Failed to load order history.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
