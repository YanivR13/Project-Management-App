package gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import server.ServerController; // נייבא את הלוגיקה של השרת

public class ServerPortFrameController implements Initializable {

	@FXML
	private Button btnExit;

	@FXML
	private Button btnStart;

	@FXML
	private TextArea txtLog;
	
	private ServerController server;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		txtLog.setEditable(false); // לוגים לקריאה בלבד
	}
	
	// פונקציה להוספת שורות ללוג בצורה בטוחה
	public void appendLog(String str) {
		javafx.application.Platform.runLater(() -> {
			txtLog.appendText(str + "\n");
		});
	}

	@FXML
	void clickStart(ActionEvent event) {
		// כאן אנחנו מפעילים את השרת האמיתי
		if (server == null) {
			server = new ServerController(5555, this);
			// אנחנו צריכים דרך לחבר את השרת בחזרה למסך הזה כדי שיכתוב לוגים
			// (נצטרך להוסיף את זה ב-ServerController בהמשך)
		}
		
		try {
			server.listen(); // פקודת OCSF
		} catch (Exception e) {
			appendLog("ERROR - Could not listen for clients!");
		}
	}

	@FXML
	void clickExit(ActionEvent event) {
		System.exit(0);
	}
}