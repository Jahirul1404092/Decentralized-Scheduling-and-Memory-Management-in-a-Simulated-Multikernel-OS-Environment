package multikernel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.chart.renderer.xy.XYBlockRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MemoryHeatmapChart {

    public static void show(MemoryTracker tracker) {
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        Map<Integer, List<Integer>> log = tracker.getMemoryLog();

        int maxSteps = tracker.getMaxSteps();
        int coreCount = log.size();

        int totalPoints = maxSteps * coreCount;
        double[] x = new double[totalPoints];
        double[] y = new double[totalPoints];
        double[] z = new double[totalPoints];

        int idx = 0;
        for (Map.Entry<Integer, List<Integer>> entry : log.entrySet()) {
            int core = entry.getKey();
            List<Integer> usage = entry.getValue();

            for (int t = 0; t < usage.size(); t++) {
                x[idx] = t;
                y[idx] = core;
                z[idx] = usage.get(t);
                idx++;
            }
        }

        dataset.addSeries("MemoryUsage", new double[][]{x, y, z});

        LookupPaintScale paintScale = new LookupPaintScale();
        for (int i = 0; i <= 1000; i += 100) {
            paintScale.add(i, new Color(255, 255 - i / 4, 255 - i / 4));
        }

        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setPaintScale(paintScale);
        renderer.setBlockWidth(1.0);
        renderer.setBlockHeight(1.0);

        NumberAxis xAxis = new NumberAxis("Time Step");
        NumberAxis yAxis = new NumberAxis("Core ID");

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.white);

        JFreeChart chart = new JFreeChart("Memory Usage Heatmap", JFreeChart.DEFAULT_TITLE_FONT, plot, false);

        JFrame frame = new JFrame("Memory Heatmap");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(new ChartPanel(chart));
        frame.setSize(800, 500);
        frame.setVisible(true);
    }
}
