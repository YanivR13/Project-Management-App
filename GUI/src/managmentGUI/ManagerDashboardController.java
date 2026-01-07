package managmentGUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.ServiceResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * קונטרולר עבור דשבורד מנהל.
 * יורש את כל הפונקציונליות של נציג ומוסיף יכולות הפקת דוחות.
 */
public class ManagerDashboardController extends RepresentativeDashboardController {

    @Override
    public void onClientReady() {
        super.onClientReady();
        appendLog("Manager Mode Active: Additional reporting tools enabled.");
    }
    
    @FXML
    public void openMonthSelection(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
        		    getClass().getResource("/managmentGUI/ActionsFXML/monthSelection.fxml")
        		);
            Parent root = loader.load();
            
            MonthSelectionController ctrl = loader.getController();
            ctrl.setClient(this.client); 
            
            Stage stage = new Stage();
            stage.setTitle("Month Selection");
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (Exception e) {
            System.out.println("Failed to open month screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void display(Object message) {
        if (message instanceof ArrayList) {
            ArrayList<Object> responseList = (ArrayList<Object>) message;
            String header = (String) responseList.get(0);

            // 1. בדיקה עבור דוח זמנים
            if ("REPORT_TIME_DATA_SUCCESS".equals(header)) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) responseList.get(1);
                Platform.runLater(() -> {
                    appendLog("Time Report Data Received Successfully.");
                    showGraph(data);
                });
            } 
            // 2. בדיקה עבור דוח מנויים (החלק החדש)
            else if ("RECEIVE_SUBSCRIBER_REPORTS".equals(header)) {
                List<Map<String, Object>> subData = (List<Map<String, Object>>) responseList.get(1);
                Platform.runLater(() -> {
                    if (subData == null || subData.isEmpty()) {
                        appendLog("No subscriber data found for the selected month.");
                    } else {
                        appendLog("Subscriber Report Data Received Successfully.");
                        showSubGraph(subData); // פתיחת גרף המנויים
                    }
                });
            }
            // 3. בדיקה אם קיבלנו שגיאת דוח
            else if ("REPORT_ERROR".equals(header)) {
                String errorMsg = (String) responseList.get(1);
                Platform.runLater(() -> appendLog("Error: " + errorMsg));
            }
            else {
                super.display(message); 
            }
        }
    }
    
    public void showGraph(List<Map<String, Object>> reportData) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/managmentGUI/ActionsFXML/TimeReportGraph.fxml"));
                Parent root = loader.load();
                TimeReportGraphController graphCtrl = loader.getController();
                graphCtrl.initData(reportData);
                
                Stage stage = new Stage();
                stage.setTitle("Time & Delays Report");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * מתודה לפתיחת גרף דוח מנויים (הזמנות מול המתנות)
     */
    public void showSubGraph(List<Map<String, Object>> reportData) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/managmentGUI/ActionsFXML/SubscriberReportGraph.fxml"));
                Parent root = loader.load();
                
                // השגת הקונטרולר של גרף המנויים
                SubReportGraphController subGraphCtrl = loader.getController();
                subGraphCtrl.initData(reportData);
                
                Stage stage = new Stage();
                stage.setTitle("Subscriber Activity Report");
                stage.setScene(new Scene(root));
                stage.show();
                
            } catch (IOException e) {
                appendLog("Error loading Subscriber Graph FXML.");
                e.printStackTrace();
            }
        });
    }
}