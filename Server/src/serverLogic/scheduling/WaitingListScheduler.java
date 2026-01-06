package serverLogic.scheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import MainControllers.ServerController;
import dbLogic.restaurantDB.JoinWaitingListDBController;
import dbLogic.restaurantDB.WaitingListController;

public class WaitingListScheduler {

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    /**
     * Starts a 15-minute timer for a notified waiting-list entry.
     */
    public static void startNoShowTimer(
            long confirmationCode,
            int tableId
    ) {

        ServerController.log(
            "[WAITING LIST] 15-minute timer started for code: " + confirmationCode
        );

        scheduler.schedule(() -> {

            try {
                // בדיקה האם הסטטוס עדיין NOTIFIED
                String currentStatus =
                        JoinWaitingListDBController.getStatusByCode(confirmationCode);

                if ("NOTIFIED".equals(currentStatus)) {

                    // עדכון ל-NOSHOW
                    JoinWaitingListDBController.updateStatus(
                            confirmationCode,
                            "NOSHOW"
                    );

                    ServerController.log(
                        "[WAITING LIST] Customer NOSHOW. Code: " + confirmationCode
                    );

                    // השולחן עדיין פנוי → מפעילים שוב את התור
                    WaitingListController.handleTableFreed(tableId);
                }

            } catch (Exception e) {
                ServerController.log(
                    "[WAITING LIST] Error in no-show timer: " + e.getMessage()
                );
            }

        }, 15, TimeUnit.MINUTES);
    }
}
