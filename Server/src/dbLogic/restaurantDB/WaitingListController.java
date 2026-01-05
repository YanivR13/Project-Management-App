package dbLogic.restaurantDB;

import java.time.LocalDateTime;
import java.util.List;

import MainControllers.ServerController;
import common.WaitingListEntry;
import serverLogic.scheduling.WaitingListScheduler;

public class WaitingListController {

	public static void handleTableFreed(int tableId) {
        try {
            // 1. קיבולת השולחן שהתפנה
            int tableCapacity = TableDBController.getTableCapacity(tableId);

            // 2. שליפת רשימת ההמתנה לפי סדר כניסה (FIFO)
            List<WaitingListEntry> waitingList =
            		VisitDBController.getWaitingEntriesOrderedByEntryTime();

            // 3. מעבר על התור
            for (WaitingListEntry entry : waitingList) {

                int guests = entry.getNumberOfGuests();

                // 3a. בדיקה אם השולחן בכלל מתאים
                if (guests > tableCapacity) {
                    continue; // לא מתאים – נמשיך לבא בתור
                }

                // 3b. בדיקה מול הזמנות עתידיות
                boolean canSeat =
                    SeatingAvailabilityController
                        .canSeatWithFutureReservations(
                            tableId,
                            LocalDateTime.now()
                        );

                if (canSeat) {
                    // 4. מצאנו מועמד – מעדכנים ל-NOTIFIED
                    JoinWaitingListDBController.updateStatus(
                        entry.getConfirmationCode(),
                        "NOTIFIED"
                    );

                    // כאן בעתיד: שליחת SMS / Email + טיימר 15 דקות
                    ServerController.log(
                    	    "[WAITING LIST] Notification sent to customer. " +
                    	    "confirmationCode=" + entry.getConfirmationCode() +
                    	    ", guests=" + entry.getNumberOfGuests()
                    	);
                    
                    WaitingListScheduler.startNoShowTimer(
                            entry.getConfirmationCode(),
                            tableId
                        );
                    
                    return; // חשוב: עוצרים אחרי הראשון שמתאים
                }
            }

            // אם לא מצאנו אף אחד – לא עושים כלום

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
