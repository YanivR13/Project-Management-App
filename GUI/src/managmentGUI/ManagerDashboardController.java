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
        // בדיקה ראשונית: האם מדובר ב-ArrayList
        if (message instanceof ArrayList) {
            ArrayList<Object> responseList = (ArrayList<Object>) message;

            // תיקון קריטי: בודקים שהרשימה לא ריקה ושהאיבר הראשון הוא אכן String (כותרת של דוח)
            // אם האיבר הראשון הוא לא String (למשל הוא אובייקט Visit), הקוד ידלג ישר ל-super.display
            if (!responseList.isEmpty() && responseList.get(0) instanceof String) {
                String header = (String) responseList.get(0);

                // 1. בדיקה עבור דוח זמנים ואיחורים
                if ("REPORT_TIME_DATA_SUCCESS".equals(header)) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) responseList.get(1);
                    Platform.runLater(() -> {
                        if (data == null || data.isEmpty()) {
                            showErrorAlert("No Data Found", "No time and delay records were found for the selected month.");
                        } else {
                            appendLog("Time Report Data Received Successfully.");
                            showGraph(data);
                        }
                    });
                    return; 
                } 
                
                // 2. בדיקה עבור דוח מנויים ורשימות המתנה
                else if ("RECEIVE_SUBSCRIBER_REPORTS".equals(header)) {
                    List<Map<String, Object>> subData = (List<Map<String, Object>>) responseList.get(1);
                    Platform.runLater(() -> {
                        if (subData == null || subData.isEmpty()) {
                            showErrorAlert("No Data Found", "No subscriber or waiting list activity found for the selected month.");
                        } else {
                            appendLog("Subscriber Report Data Received Successfully.");
                            showSubGraph(subData);
                        }
                    });
                    return; 
                }
                
                // 3. בדיקה אם קיבלנו שגיאת דוח ספציפית מהשרת
                else if ("REPORT_ERROR".equals(header)) {
                    String errorMsg = (String) responseList.get(1);
                    Platform.runLater(() -> {
                        showErrorAlert("System Error", "Error: " + errorMsg);
                    });
                    return; 
                }
            }
        }

        /**
         * אם הגענו לכאן, זה אומר שאו שזו לא רשימה, או שזו רשימה של אובייקטים (כמו Visit).
         * אנחנו מעבירים אותה למחלקת האב (RepresentativeDashboardController) שתטפל בהצגה בטבלה.
         */
        super.display(message); 
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
    
    private void showErrorAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}