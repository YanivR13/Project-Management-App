package serverGUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.application.Platform;
import common.ServerIF;
import controllers.ServerController;

public class ServerPortFrameController implements ServerIF {

    @FXML
    private Button btnStart;

    @FXML
    private Button btnExit;

    @FXML
    private TextArea txtLog;

    private ServerController server;

    @FXML
    public void clickStart(ActionEvent event) {
        if (server != null) {
            appendLog("Server is already running!");
            return;
        }

        appendLog("Attempting to start server...");

        server = new ServerController(5555, this);

        try {
            server.listen();
            appendLog("Server started listening on port 5555");

        } catch (Exception e) {
            appendLog("Error starting server: " + e.getMessage());
        }
    }

    @FXML
    public void clickExit(ActionEvent event) {
        appendLog("Exiting...");

        if (server != null) {
            try {
                appendLog("Stopping server...");
                
                server.stopListening();
                
                server.close();

            } catch (Exception e) {
                appendLog("Error while closing server: " + e.getMessage());
            }
        }

        System.exit(0);
    }

    @Override
    public void appendLog(String msg) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                txtLog.appendText(msg + "\n");
            }
        });
    }
}