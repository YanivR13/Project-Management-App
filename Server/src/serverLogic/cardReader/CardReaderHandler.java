package serverLogic.cardReader;
import dbLogic.restaurantDB.VisitController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import dbLogic.cardReader.CardReaderDBController;
import ocsf.server.ConnectionToClient;

/**
 * Handler שמנהל את הלוגיקה של ה-Card Reader בשרת.
 */
public class CardReaderHandler {

    /**
     * מקבל הודעה מהשרת ומנתב אותה לפעולה המתאימה.
     * @param data רשימה הכוללת: [0] פקודה, [1...] נתונים.
     * @param client חיבור הלקוח.
     */
	public void handle(ArrayList<Object> data, ConnectionToClient client) {
	    String command = (String) data.get(0); 
	    CardReaderDBController db = new CardReaderDBController(); 

	    try {
	        switch (command) {
	            case "CARD_READER_LOGIN":
	                String subIDForLogin = (String) data.get(1);
	                boolean isValid = db.validateSubscriber(subIDForLogin);
	                client.sendToClient(isValid); 
	                break;

	            case "CARD_READER_GET_CODES": { // השתמש בסוגריים {} למניעת שגיאת Duplicate variable
	                // 1. קבלת ה-ID מהטרמינל (למשל "502")
	                String idFromTerminal = (String) data.get(1);
	                
	                // 2. קריאה ישירה ללוגיקה ב-viewReservationController (הקישור שביקשת)
	                List<String> codesList = dbLogic.restaurantDB.viewReservationController.getCodesBySubscriberId(idFromTerminal);
	                
	                // 3. שליחה חזרה לטרמינל
	                client.sendToClient(codesList); 
	                break;
	            }

	            case "CARD_READER_VERIFY_CODE":
	                String codeStr = (String) data.get(1);
	                long code = Long.parseLong(codeStr);
	                String result = dbLogic.restaurantDB.VisitController.processTerminalArrival(code);
	                client.sendToClient(result); 
	                break;
	        }
	    } catch (Exception e) {
	        System.err.println("Error in CardReaderHandler: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
    
    
    
}