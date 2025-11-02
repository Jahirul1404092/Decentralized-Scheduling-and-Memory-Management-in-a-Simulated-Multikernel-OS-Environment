package multikernel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MemoryHeatmapChart {

    public static void show(MemoryTracker tracker) {
        Map<Integer, List<Integer>> log = tracker.getMemoryLog();

        if (log == null || log.isEmpty()) {
            System.out.println("No memory data to plot.");
            return;
        }

        int coreCount = log.size();
        int maxSteps = tracker.getMaxSteps();
        int totalPoints = coreCount * maxSteps;

        double[] x = new double[totalPoints];
        double[] y = new double[totalPoints];
        double[] z = new double[totalPoints];

        int idx = 0;
        double maxMemory = 0;

        for (Map.Entry<Integer, List<Integer>> entry : log.entrySet()) {
            int core = entry.getKey();
            List<Integer> usage = entry.getValue();

            for (int t = 0; t < usage.size(); t++) {
                x[idx] = t;
                y[idx] = core;
                z[idx] = usage.get(t);
                if (usage.get(t) > maxMemory) {
                    maxMemory = usage.get(t);
                }
                idx++;
            }
        }

        System.out.println("Generated " + idx + " data points for heatmap.");
        System.out.println("Max memory value: " + maxMemory);

        DefaultXYZDataset dataset = new DefaultXYZDataset();
        dataset.addSeries("MemoryUsage", new double[][]{x, y, z});

        // Handle zero-memory case to prevent exception
        LookupPaintScale paintScale;
        if (maxMemory <= 0) {
            System.out.println("No memory usage recorded (maxMemory = 0). Using default color scale.");
            paintScale = new LookupPaintScale(0, 1, Color.LIGHT_GRAY);
            paintScale.add(0, Color.LIGHT_GRAY);
            paintScale.add(1, new Color(200, 50, 50));
        } else {
            paintScale = new LookupPaintScale(0, maxMemory, Color.WHITE);
            for (int i = 0; i <= maxMemory; i += Math.max(10, (int)(maxMemory / 20))) {
                int red = Math.min(255, (int)(255 * i / maxMemory));
                int blue = 255 - red;
                paintScale.add(i, new Color(red, 50, blue));
            }
        }

        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setBlockWidth(1.0);
        renderer.setBlockHeight(1.0);
        renderer.setPaintScale(paintScale);

        NumberAxis xAxis = new NumberAxis("Time Step");
        NumberAxis yAxis = new NumberAxis("Core ID");

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.white);

        JFreeChart chart = new JFreeChart("Memory Usage Heatmap",
                JFreeChart.DEFAULT_TITLE_FONT, plot, false);

        JFrame frame = new JFrame("Memory Heatmap");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(new ChartPanel(chart));
        frame.setSize(900, 600);
        frame.setVisible(true);
    }
}
