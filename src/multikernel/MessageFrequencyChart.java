package multikernel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Map;

/**
 * Displays the number of messages sent per core as a bar chart.
 */
public class MessageFrequencyChart {

    public static void showMessageFrequency(Map<Integer, Integer> sentPerCore) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Integer> e : sentPerCore.entrySet()) {
            int coreId = e.getKey();
            int count = e.getValue();
            dataset.addValue(count, "Messages Sent", "Core " + coreId);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Inter-Core Message Frequency",
                "Core",
                "# Messages Sent",
                dataset
        );

        ChartFrame frame = new ChartFrame("Message Frequency", chart);
        frame.pack();
        frame.setVisible(true);
    }
}
