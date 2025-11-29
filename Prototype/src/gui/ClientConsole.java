package gui;

// אנחנו מייבאים את הלוגיקה מהחבילה השכנה
import client.ChatIF;
import client.OrderClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * המחלקה הזו היא ה-UI כרגע.
 * היא אחראית רק על לקבל קלט מהמקלדת ולהציג פלט למסך השחור.
 */
public class ClientConsole implements ChatIF {

    // מחזיק את המנוע הלוגי
    OrderClient client;

    // בנאי
    public ClientConsole(String host, int port) {
        try {
            // יצירת המנוע. אנחנו מעבירים לו את 'this' (את עצמנו)
            // כי אנחנו ה-UI שיודע להציג הודעות.
            client = new OrderClient(host, port, this);
        } catch (IOException exception) {
            System.out.println("Error: Can't setup connection! Terminating client.");
            System.exit(1);
        }
    }

    // --- מימוש החוזה ChatIF ---
    // כשהמנוע ירצה להציג משהו, הוא יקרא לפונקציה הזו
    @Override
    public void display(String message) {
        System.out.println("> " + message);
    }
    // ---------------------------

    // לולאה שמחכה לקלט מהמשתמש
    public void accept() {
        try {
            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            String message;

            while (true) {
                message = fromConsole.readLine();
                
                // יצירת הרשימה ושליחה ללוגיקה (כמו במטלה)
                ArrayList<String> dataToSend = new ArrayList<>();
                dataToSend.add(message); // תאריך
                dataToSend.add("5");     // מספר אורחים
                
                // שליחה ללוגיקה (שנמצאת בחבילה אחרת)
                client.handleMessageFromClientUI(dataToSend);
            }
        } catch (Exception ex) {
            System.out.println("Unexpected error while reading from console!");
        }
    }

    // ה-Main שמריץ את הלקוח
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5555;
        
        ClientConsole chat = new ClientConsole(host, port);
        chat.accept(); 
    }
}