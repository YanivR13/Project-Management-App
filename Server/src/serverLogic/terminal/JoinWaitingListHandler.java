package serverLogic.terminal;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.ServiceResponse;
import common.ServiceResponse.ServiceStatus;
import dbLogic.restaurantDB.JoinWaitingListDBController;
import dbLogic.restaurantDB.SeatingAvailabilityController;
import dbLogic.restaurantDB.TableDBController;
import dbLogic.restaurantDB.VisitDBController;
import ocsf.server.ConnectionToClient;

public class JoinWaitingListHandler {

    public void handle(ArrayList<Object> messageList, ConnectionToClient client) {

        System.out.println("JOIN_WAITING_LIST HANDLER CALLED");

        try {
            // 1) number of guests
            int numberOfGuests = (int) messageList.get(1);

            // 2) user id
            Integer userId = (Integer) client.getInfo("userId");
            if (userId == null) {
                sendError(client, "User not authenticated");
                return;
            }

            // 3) confirmation code
            long confirmationCode = System.currentTimeMillis();

            // 4) candidate tables
            List<Integer> candidateTables =
                    TableDBController.getCandidateTables(numberOfGuests);

            System.out.println("DEBUG – candidate tables: " + candidateTables);

            Integer chosenTableId = null;

            // 5) check each candidate table against future reservations
            if (candidateTables != null) {
                for (Integer tableId : candidateTables) {
                    boolean ok =
                        SeatingAvailabilityController
                            .canSeatWithFutureReservations(
                                tableId,
                                LocalDateTime.now()
                            );

                    if (ok) {
                        chosenTableId = tableId;
                        break;
                    }
                }
            }

            // 6) if table found -> ARRIVED + VISIT(ACTIVE)
            if (chosenTableId != null) {

                // mark table as unavailable
                TableDBController.setTableUnavailable(chosenTableId);

                // insert visit with ACTIVE status AND create connected bill
                int billId =
                	    VisitDBController.insertVisitAndCreateBill(
                	        confirmationCode,
                	        chosenTableId,
                	        userId
                	    );


                // insert waiting list entry
                JoinWaitingListDBController.insertWaitingListEntry(
                    confirmationCode,
                    userId,
                    numberOfGuests,
                    "ARRIVED"
                );

                client.sendToClient(
                    new ServiceResponse(
                        ServiceStatus.UPDATE_SUCCESS,
                        confirmationCode
                    )
                );
                return;
            }

            // 7) no table fits -> WAITING
            JoinWaitingListDBController.insertWaitingListEntry(
                confirmationCode,
                userId,
                numberOfGuests,
                "WAITING"
            );

            client.sendToClient(
                new ServiceResponse(
                    ServiceStatus.UPDATE_SUCCESS,
                    confirmationCode
                )
            );

        } catch (Exception e) {
            e.printStackTrace();
            sendError(client, "INTERNAL_ERROR");
        }
    }

    private void sendError(ConnectionToClient client, String msg) {
        try {
            client.sendToClient(
                new ServiceResponse(ServiceStatus.INTERNAL_ERROR, msg)
            );
        } catch (IOException ignored) {}
    }
}
