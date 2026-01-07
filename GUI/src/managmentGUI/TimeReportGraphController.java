package managmentGUI;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;

public class TimeReportGraphController {

    @FXML private BarChart<String, Number> timeBarChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private PieChart arrivalPieChart;
    @FXML private PieChart departurePieChart;

    public void initData(List<Map<String, Object>> reportData) {
        // 1. הגדרת גרף עמודות (איחורים ושהייה)
        XYChart.Series<String, Number> delaySeries = new XYChart.Series<>();
        delaySeries.setName("Avg Delay");
        XYChart.Series<String, Number> durationSeries = new XYChart.Series<>();
        durationSeries.setName("Avg Stay Duration");

        // 2. הכנה לגרפי עוגה (ספירת שעות)
        Map<Integer, Integer> arrivalCounts = new HashMap<>();
        Map<Integer, Integer> departureCounts = new HashMap<>();

        for (Map<String, Object> entry : reportData) {
            // נתוני עמודות
            String day = entry.get("date").toString();
            day = day.substring(day.lastIndexOf("-") + 1);
            delaySeries.getData().add(new XYChart.Data<>(day, (Number) entry.get("delay")));
            durationSeries.getData().add(new XYChart.Data<>(day, (Number) entry.get("duration")));

            // נתוני עוגה - ספירת שעות
            int arrH = (int) entry.get("arr_hour");
            int depH = (int) entry.get("dep_hour");
            arrivalCounts.put(arrH, arrivalCounts.getOrDefault(arrH, 0) + 1);
            departureCounts.put(depH, departureCounts.getOrDefault(depH, 0) + 1);
        }

        // הזנת נתונים לגרף עמודות
        timeBarChart.getData().clear();
        timeBarChart.getData().addAll(delaySeries, durationSeries);

        // הזנת נתונים לגרפי עוגה
        fillPieChart(arrivalPieChart, arrivalCounts);
        fillPieChart(departurePieChart, departureCounts);
    }

    private void fillPieChart(PieChart chart, Map<Integer, Integer> counts) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        counts.forEach((hour, count) -> {
            pieData.add(new PieChart.Data(hour + ":00", count));
        });
        chart.setData(pieData);
    }
}