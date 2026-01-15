package CardReader;

import java.util.ArrayList;
import client.ChatClient;

/**
 * קונטרולר צד לקוח - משמש רק כגשר לשליחת הודעות לשרת.
 * אין פה חיבור ל-Database!
 */
public class CardReaderController {
    
    private static ChatClient client;

    public static void setClient(ChatClient chatClient) {
        client = chatClient;
    }
    
    public static ChatClient getClient() {
        return client;
    }

    // שליחת בקשת לוגין לשרת
  //abc
    public void validateSubscriber(String id) {
        ArrayList<Object> message = new ArrayList<>();
        // שינוי קריטי: משתמשים ב-LOGIN_SUBSCRIBER כדי שיהיה זהה לטרמינל
        message.add("LOGIN_SUBSCRIBER"); 
        message.add(id);
        client.handleMessageFromClientUI(message);
    }
    
    // שליחת בקשה לשחזור קודים לשרת
    public void getLostConfirmationCodes(String id) {
        ArrayList<Object> message = new ArrayList<>();
        message.add("CARD_READER_GET_CODES");
        message.add(id);
        client.handleMessageFromClientUI(message);
    }

    // שליחת בקשה לאימות קוד הגעה לשרת
    public void verifyConfirmationCode(String code, String id) {
        ArrayList<Object> message = new ArrayList<>();
        message.add("CARD_READER_VERIFY_CODE");
        message.add(code);
        message.add(id);
        client.handleMessageFromClientUI(message);
    }
}