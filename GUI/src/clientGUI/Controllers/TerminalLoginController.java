package clientGUI.Controllers;

import client.ChatClient;
import clientGUI.Controllers.OccasionalControlls.OccasionalLoginController;
import clientGUI.Controllers.SubscriberControlls.SubscriberLoginController;
import common.ChatIF;
import commonLogin.LoginSource;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

/**
 * Controller for the Terminal Login selection screen.
 * Allows choosing between Subscriber and Occasional login paths
 * originating from a physical terminal.
 */
public class TerminalLoginController implements ChatIF {

    /** Shared network client instance */
    private ChatClient client;

    /**
     * Injects the ChatClient instance.
     * @param client The active network client.
     */
    public void setClient(ChatClient client) {
        this.client = client;
    }

    /**
     * Event handler for Subscriber login selection (Terminal).
     */
    @FXML
    void clickSubscriber(ActionEvent event) {
        loadScreen(event,
                "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberLoginFrame.fxml",
                true,
                "Subscriber Login");
    }

    /**
     * Event handler for Occasional (Guest) login selection (Terminal).
     */
    @FXML
    void clickOccasional(ActionEvent event) {
        loadScreen(event,
                "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalLoginFrame.fxml",
                false,
                "Occasional Login");
    }

    /**
     * Core navigation method – identical in spirit to RemoteLoginController,
     * but sets LoginSource.TERMINAL.
     */
    private void loadScreen(ActionEvent event,
                            String fxmlPath,
                            boolean isSubscriber,
                            String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (isSubscriber) {
                SubscriberLoginController c = (SubscriberLoginController) controller;
                c.setClient(client);
                c.setLoginSource(LoginSource.TERMINAL);
            } else {
                OccasionalLoginController c = (OccasionalLoginController) controller;
                c.setClient(client);
                c.setLoginSource(LoginSource.TERMINAL);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/clientGUI/cssStyle/GlobalStyles.css").toExternalForm()
            );

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Override
	public void display(Object message) {
		// TODO Auto-generated method stub
		
	}
}
