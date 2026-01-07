package managmentGUI;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class TimeReportGraphController implements Initializable {

    @FXML
    private BarChart<String, Number> timeBarChart;

    @FXML
    private CategoryAxis xAxis; // ציר ה-X: מייצג את הביקורים

    @FXML
    private NumberAxis yAxis;   // ציר ה-Y: מייצג דקות איחור

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // הגדרת תוויות לצירים
        xAxis.setLabel("Visit (Time/ID)");
        yAxis.setLabel("Delay in Minutes");
        timeBarChart.setTitle("Customer Delays Report");
    }

    /**
     * פונקציה שמקבלת את הנתונים מה-DashboardController וממלאת את הגרף
     */
    public void initData(List<Map<String, Object>> reportData) {
        // יצירת סדרת נתונים חדשה
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Minutes of Delay");

        // מעבר על הנתונים שהגיעו מהשרת
        for (Map<String, Object> entry : reportData) {
            // חילוץ נתונים (שים לב שהשמות תואמים למה שכתבנו ב-reportsDBController)
            String timeLabel = entry.get("reserved").toString();
            Number delay = (Number) entry.get("delay");

            // הוספת נקודה לגרף: (שם הביקור, מספר דקות האיחור)
            series.getData().add(new XYChart.Data<>(timeLabel, delay));
        }

        // הוספת הסדרה לגרף
        timeBarChart.getData().clear(); // ניקוי נתונים קודמים אם היו
        timeBarChart.getData().add(series);
    }
}