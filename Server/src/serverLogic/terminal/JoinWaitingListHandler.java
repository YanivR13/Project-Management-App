package serverLogic.terminal;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import common.Reservation;
import common.ServiceResponse;
import common.ServiceResponse.ServiceStatus;
import dbLogic.restaurantDB.CreateOrderController;
import dbLogic.restaurantDB.WaitingListDBController;
import ocsf.server.ConnectionToClient;

public class JoinWaitingListHandler {

    // חייב להתאים ל-CreateOrderController
    private static final DateTimeFormatter SQL_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");

    public void handle(ArrayList<Object> messageList, ConnectionToClient client) {

        try {
            // ===============================
            // STEP 1: Extract data
            // ===============================
            int diners = (int) messageList.get(1);

            // userId נשלף מה-Connection (לא מה-GUI!)
            Integer userId = (Integer) client.getInfo("userId");
            if (userId == null) {
                sendError(client, "User not authenticated.");
                return;
            }

            // ===============================
            // STEP 2: Round current time
            // ===============================
            LocalDateTime baseTime = roundUpToHalfHour(LocalDateTime.now());

            // ===============================
            // STEP 3: Try up to 2 hours ahead
            // ===============================
            for (int attempt = 0; attempt <= 4; attempt++) {

                LocalDateTime attemptTime = baseTime.plusMinutes(30L * attempt);
                String dateTimeStr = attemptTime.format(SQL_FORMATTER);

                Reservation reservation =
                        new Reservation(userId, dateTimeStr, diners);

                ServiceResponse response =
                        CreateOrderController.processNewReservation(reservation);

                // ===============================
                // CASE 1: SUCCESS
                // ===============================
                if (response.getStatus() == ServiceStatus.RESERVATION_SUCCESS) {

                    // ניסיון ראשון → הזמנה רגילה
                    if (attempt == 0) {
                        client.sendToClient(response);
                        return;
                    }

                    // ניסיון מאוחר → waiting list
                    WaitingListDBController.insertWaitingListEntry(reservation,(Long) response.getData());

                    client.sendToClient(new ServiceResponse(ServiceStatus.UPDATE_SUCCESS,"Added to waiting list"));
                    return;
                }

                // ===============================
                // CASE 2: OUT OF HOURS
                // ===============================
                if (response.getStatus() == ServiceStatus.RESERVATION_OUT_OF_HOURS) {
                    continue;
                }
            }

            // ===============================
            // CASE 3: No availability
            // ===============================
            client.sendToClient(new ServiceResponse(ServiceStatus.RESERVATION_FULL,"No availability in the next 2 hours"));

        } catch (Exception e) {
            e.printStackTrace();
            sendError(client, "INTERNAL_ERROR: Waiting list process failed.");
        }
    }

    // ===============================
    // Time rounding helper
    // ===============================
    private LocalDateTime roundUpToHalfHour(LocalDateTime time) {
        int minute = time.getMinute();

        if (minute == 0 || minute == 30) {
            return time.withSecond(0).withNano(0);
        }

        if (minute < 30) {
            return time.withMinute(30).withSecond(0).withNano(0);
        }

        return time.plusHours(1)
                   .withMinute(0)
                   .withSecond(0)
                   .withNano(0);
    }

    private void sendError(ConnectionToClient client, String msg) {
        try {
            client.sendToClient(
                    new ServiceResponse(
                            ServiceStatus.INTERNAL_ERROR,
                            msg
                    )
            );
        } catch (IOException ignored) {}
    }
}
