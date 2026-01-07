package managmentGUI;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import java.util.*;

public class SubReportGraphController {
    @FXML private BarChart<String, Number> subBarChart;
    @FXML private CategoryAxis xAxis;

    public void initData(List<Map<String, Object>> reportData) {
        XYChart.Series<String, Number> resSeries = new XYChart.Series<>();
        resSeries.setName("Reservations");

        XYChart.Series<String, Number> waitSeries = new XYChart.Series<>();
        waitSeries.setName("Waiting List");

        for (Map<String, Object> entry : reportData) {
            String fullDate = entry.get("date").toString();
            String dayOnly = fullDate.substring(fullDate.lastIndexOf("-") + 1);

            resSeries.getData().add(new XYChart.Data<>(dayOnly, (Number) entry.get("reservations")));
            waitSeries.getData().add(new XYChart.Data<>(dayOnly, (Number) entry.get("waiting")));
        }

        subBarChart.getData().clear();
        subBarChart.getData().addAll(resSeries, waitSeries);
    }
}