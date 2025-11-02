package multikernel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.util.Map;

public class ComparisonChartGenerator {

    public static void showTurnaroundComparison(Map<String, Double> schedulerAverages) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Double> entry : schedulerAverages.entrySet()) {
            dataset.addValue(entry.getValue(), "Avg Turnaround Time", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Scheduler Comparison - Average Turnaround Time",
                "Scheduler",
                "Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        JFrame frame = new JFrame("Scheduler Performance Comparison");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(new ChartPanel(chart));
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
