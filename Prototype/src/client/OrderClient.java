package client;

import ocsf.client.AbstractClient;
import java.io.IOException;

public class OrderClient extends AbstractClient {

    // משתנה שמחזיק את ה"מסך" שלנו (מי שמימש את החוזה ChatIF)
    // שים לב: אנחנו לא יודעים מי זה, רק שהוא יודע לעשות display.
    ChatIF clientUI; 

    // בנאי: מקבל את פרטי ההתחברות ואת המסך שיציג את התוצאות
    public OrderClient(String host, int port, ChatIF clientUI) throws IOException {
        super(host, port); // מעביר את הכתובת והפורט ל-OCSF
        this.clientUI = clientUI; // שומר את הרפרנס למסך
        openConnection(); // מנסה להתחבר לשרת מיד
    }

    // -----------------------------------------------------------
    // מתודה 1: מה קורה כשהשרת שולח לי הודעה? (חובה לממש)
    // -----------------------------------------------------------
    @Override
    protected void handleMessageFromServer(Object msg) {
        // אנחנו לא מדפיסים פה כלום! אנחנו מעבירים את זה ל-UI.
        clientUI.display(msg.toString());
    }

    // -----------------------------------------------------------
    // מתודה 2: בקשה מה-UI לשלוח הודעה לשרת
    // -----------------------------------------------------------
    public void handleMessageFromClientUI(Object message) {
        try {
            sendToServer(message); // שליחה פיזית לשרת דרך OCSF
        } catch (IOException e) {
            clientUI.display("Could not send message to server. Terminating client.");
            quit();
        }
    }
    
    // מתודה לסגירה נקייה
    public void quit() {
        try {
            closeConnection();
        } catch(IOException e) {}
        System.exit(0);
    }
}