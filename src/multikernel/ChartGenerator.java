package multikernel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.util.List;

public class ChartGenerator {

    public static void showTurnaroundChart(List<MetricsCollector.Record> records) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (MetricsCollector.Record r : records) {
            long turnaround = r.endTime - r.arrivalTime;
            dataset.addValue(turnaround, "Turnaround Time", "Task " + r.taskId);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Task Turnaround Time",
                "Task ID",
                "Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        JFrame frame = new JFrame("Simulation Results");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new ChartPanel(barChart));
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
