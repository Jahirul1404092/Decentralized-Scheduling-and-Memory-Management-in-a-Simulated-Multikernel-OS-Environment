package multikernel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.title.TextTitle;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Generates a heatmap of per-core memory usage over time (Red gradient).
 */
public class MemoryHeatmapChart {

    public static void showHeatmap(Map<Integer, List<Integer>> memoryLog) {
        XYZDataset dataset = createDataset(memoryLog);

        NumberAxis xAxis = new NumberAxis("Time Step");
        NumberAxis yAxis = new NumberAxis("Core ID");

        XYBlockRenderer renderer = new XYBlockRenderer();
        double maxValue = getMaxValue(memoryLog);

        // 🔴 Red-based gradient palette (light pink → dark red)
        LookupPaintScale paintScale = new LookupPaintScale(0, maxValue, new Color(255, 245, 245)); // near white
        paintScale.add(maxValue * 0.25, new Color(255, 204, 204)); // light pink
        paintScale.add(maxValue * 0.5, new Color(255, 102, 102));  // medium red
        paintScale.add(maxValue * 0.75, new Color(255, 51, 51));   // bright red
        paintScale.add(maxValue, new Color(153, 0, 0));            // deep red

        renderer.setPaintScale(paintScale);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setInsets(new RectangleInsets(5, 5, 5, 5));
        plot.setBackgroundPaint(Color.lightGray);

        JFreeChart chart = new JFreeChart(
                "Memory Usage Heatmap",
                new Font("SansSerif", Font.BOLD, 16),
                plot,
                false
        );

        chart.addSubtitle(new TextTitle("Per-core memory usage over time"));

        ChartFrame frame = new ChartFrame("Memory Usage Heatmap", chart);
        frame.pack();
        frame.setVisible(true);
    }

    private static XYZDataset createDataset(Map<Integer, List<Integer>> memoryLog) {
        DefaultXYZDataset dataset = new DefaultXYZDataset();

        int coreCount = memoryLog.size();
        int maxSteps = memoryLog.values().stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        double[] x = new double[coreCount * maxSteps];
        double[] y = new double[coreCount * maxSteps];
        double[] z = new double[coreCount * maxSteps];

        int index = 0;
        for (Map.Entry<Integer, List<Integer>> entry : memoryLog.entrySet()) {
            int coreId = entry.getKey();
            List<Integer> memList = entry.getValue();

            for (int t = 0; t < memList.size(); t++) {
                x[index] = t;
                y[index] = coreId;
                z[index] = memList.get(t);
                index++;
            }
        }

        dataset.addSeries("Memory Usage", new double[][]{x, y, z});
        return dataset;
    }

    private static double getMaxValue(Map<Integer, List<Integer>> memoryLog) {
        double max = 1;
        for (List<Integer> list : memoryLog.values()) {
            for (Integer v : list) {
                if (v > max) max = v;
            }
        }
        return max;
    }
}
