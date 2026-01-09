package serverLogic.cardReader;

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
        String command = (String) data.get(0); // שליפת הפקודה
        CardReaderDBController db = new CardReaderDBController(); // יצירת מופע של ה-DB Controller

        try {
            switch (command) {
                case "CARD_READER_LOGIN":
                    // אימות מנוי לפי ID
                    String subID = (String) data.get(1);
                    boolean isValid = db.validateSubscriber(subID);
                    client.sendToClient(isValid); // החזרת תשובה בוליאנית ללקוח
                    break;

                case "CARD_READER_GET_CODES":
                    // שליפת רשימת קודים אבודים
                    String idForCodes = (String) data.get(1);
                    List<String> codes = db.getLostConfirmationCodes(idForCodes);
                    client.sendToClient(codes); // החזרת רשימה ללקוח
                    break;

                case "CARD_READER_VERIFY_CODE":
                    // אימות קוד אישור ועדכון הגעה
                    String code = (String) data.get(1);
                    String subIDForVerify = (String) data.get(2);
                    String result = db.verifyConfirmationCode(code, subIDForVerify);
                    client.sendToClient(result); // החזרת הודעת טקסט ללקוח
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient("ERROR: Failed to process Card Reader request."); // דיווח על שגיאה ללקוח
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}