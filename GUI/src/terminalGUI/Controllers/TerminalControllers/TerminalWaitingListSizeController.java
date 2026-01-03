package terminalGUI.Controllers.TerminalControllers;

import client.ChatClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class TerminalWaitingListSizeController {
    private ChatClient client;

    public void setClient(ChatClient client) {
        this.client = client;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // נחזור ל-TerminalMenu (נטפל בזה בשלב הבא)
    }

    @FXML
    private void handleContinue(ActionEvent event) {
        // כרגע ריק – לוגיקה תבוא בשלב הבא
    }
}
