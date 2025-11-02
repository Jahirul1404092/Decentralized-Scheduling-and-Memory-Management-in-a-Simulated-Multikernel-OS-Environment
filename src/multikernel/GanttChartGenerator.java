//package multikernel;
//
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.data.gantt.Task;
//import org.jfree.data.gantt.TaskSeries;
//import org.jfree.data.gantt.TaskSeriesCollection;
//
//import javax.swing.*;
//import java.util.Date;
//import java.util.List;
//import multikernelSimpleTimePeriod;
////import org.jfree.data.time.SimpleTimePeriod;
//
///**
// * Generates a Gantt-style chart showing task execution timeline across cores.
// */
//public class GanttChartGenerator {
//
//    public static void showGanttChart(List<MetricsCollector.Record> records) {
//        TaskSeriesCollection dataset = new TaskSeriesCollection();
//
//        // Group tasks by core
//        for (int coreId = 1; coreId <= 2; coreId++) {
//            TaskSeries coreSeries = new TaskSeries("Core " + coreId);
//
//            for (MetricsCollector.Record r : records) {
//                if (r.coreId == coreId) {
//                    long start = r.startTime;
//                    long end = r.endTime;
//
//                    // Use your custom SimpleTimePeriod class
//                    coreSeries.add(new Task("Task " + r.taskId,
//                            new multikernel.SimpleTimePeriod(new Date(start), new Date(end))));
//                }
//            }
//
//            dataset.add(coreSeries);
//        }
//
//        // Create the Gantt chart
//        JFreeChart chart = ChartFactory.createGanttChart(
//                "Gantt Chart - Core Execution Timeline",
//                "Cores",
//                "Time",
//                dataset,
//                true,
//                true,
//                false);
//
//        // Display it in a window
//        JFrame frame = new JFrame("Core Execution Timeline");
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        frame.setContentPane(new ChartPanel(chart));
//        frame.setSize(900, 600);
//        frame.setVisible(true);
//    }
//}
