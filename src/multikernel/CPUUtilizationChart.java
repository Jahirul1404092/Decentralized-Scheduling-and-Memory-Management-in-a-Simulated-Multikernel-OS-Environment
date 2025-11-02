package multikernel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Map;

/**
 * Displays per-core CPU utilization as a bar chart.
 */
public class CPUUtilizationChart {

    public static void showCPUUtilization(Map<Integer, Double> coreUtilPct) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Double> e : coreUtilPct.entrySet()) {
            int coreId = e.getKey();
            double pct = e.getValue();
            dataset.addValue(pct, "Utilization (%)", "Core " + coreId);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Per-Core CPU Utilization",
                "Core",
                "Utilization (%)",
                dataset
        );

        ChartFrame frame = new ChartFrame("CPU Utilization", chart);
        frame.pack();
        frame.setVisible(true);
    }
}
