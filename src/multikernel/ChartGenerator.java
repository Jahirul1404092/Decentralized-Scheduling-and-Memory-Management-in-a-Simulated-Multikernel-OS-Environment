package multikernel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Map;

/**
 * Generates a bar chart for task turnaround time.
 */
public class ChartGenerator {

    public static void showTurnaroundChart(Map<Integer, Long> taskTurnarounds) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Long> entry : taskTurnarounds.entrySet()) {
            dataset.addValue(entry.getValue(), "Turnaround", "Task " + entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Task Turnaround Times",
                "Task",
                "Turnaround (ms)",
                dataset
        );

        ChartFrame frame = new ChartFrame("Task Turnaround", chart);
        frame.pack();
        frame.setVisible(true);
    }
}
