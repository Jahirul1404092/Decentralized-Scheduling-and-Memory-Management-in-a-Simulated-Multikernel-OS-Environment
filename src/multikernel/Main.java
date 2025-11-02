package multikernel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import java.util.*;

/**
 * Complete Main class for Multikernel simulation.
 * Includes:
 *  - Full metrics export and visualization (utilization, heatmap, etc.)
 *  - Scheduler comparison experiment (Round Robin vs SJF)
 */
public class Main {

	static int numCores = 8;
	static int numTasks = 200;
    static int totalMemoryPerCore = 1000;
    public static void main(String[] args) {
        System.out.println("========== Multikernel Simulation ==========");

//        int numCores = 8;
//        int numTasks = 50;
//        int totalMemoryPerCore = 1000;
        boolean useSJF = false; // toggle scheduler here for base run

        // ----- SETUP COMPONENTS -----
        MessageBus messageBus = new MessageBus(numCores);
        MetricsCollector metricsCollector = new MetricsCollector();
        MemoryTracker memoryTracker = new MemoryTracker();

        List<Core> cores = new ArrayList<>();
        List<Scheduler> schedulers = new ArrayList<>();
        List<MemoryManager> memoryManagers = new ArrayList<>();

        for (int i = 0; i < numCores; i++) {
            Scheduler scheduler = useSJF ? new ShortestJobFirstScheduler() : new RoundRobinScheduler();
            MemoryManager memoryManager = new MemoryManager(totalMemoryPerCore);
            schedulers.add(scheduler);
            memoryManagers.add(memoryManager);

            Core core = new Core(i, scheduler, memoryManager, messageBus, metricsCollector, memoryTracker, numCores);
            cores.add(core);
        }

        // ----- GENERATE TASKS -----
        System.out.println("Generating workload...");
        List<Task> tasks = generateTasks(numTasks);

        // Simple distribution of tasks to cores (round robin)
        for (int i = 0; i < tasks.size(); i++) {
            schedulers.get(i % numCores).addTask(tasks.get(i));
        }

        // ----- START CORES -----
        System.out.println("Starting cores...");
        for (Core c : cores) {
            c.start();
        }

        try {
            Thread.sleep(20000); // simulation time
        } catch (InterruptedException ignored) {}

        // ----- STOP CORES -----
        System.out.println("Stopping cores...");
        for (Core c : cores) {
            c.stopCore();
        }

        for (Core c : cores) {
            try {
                c.join();
            } catch (InterruptedException ignored) {}
        }

        // ===== EXPORT METRICS =====
        System.out.println("Exporting metrics...");
        metricsCollector.exportCSV("task_metrics.csv");
        metricsCollector.exportUtilizationCSV("core_utilization.csv");

        // ===== VISUALIZATIONS =====
        System.out.println("Generating charts...");
        CPUUtilizationChart.showCPUUtilization(metricsCollector.getCoreUtilizationPercent());
        MessageFrequencyChart.showMessageFrequency(messageBus.getSentCountSnapshot());

        // ===== SUMMARY =====
        printSummary(metricsCollector, messageBus);

        // ===== MEMORY HEATMAP =====
        System.out.println("Generating Memory Heatmap...");
        MemoryHeatmapChart.showHeatmap(memoryTracker.getMemoryLog());

        // ===== TASK TURNAROUND CHART =====
        System.out.println("Generating Task Turnaround Chart...");
        ChartGenerator.showTurnaroundChart(metricsCollector.getTaskTurnarounds());

        // ===== SCHEDULER COMPARISON (RoundRobin vs SJF) =====
        rundecentralizeSchedulerComparison();
        
        runglobalSchedulerComparison();
        
        System.out.println("\n✅ Simulation and comparison complete.");
        
        
    }
    
    // Optional feature: automated scheduler comparison chart
    public static void runglobalSchedulerComparison() {
    	System.out.println("\n========== Running Scheduler Comparison ==========");
    	
        Map<String, Double> results = new LinkedHashMap<>();
        System.out.println("\n========== Running Round Robin Scheduler ==========");
        results.put("Round Robin", simulateAndGetAverage(new RoundRobinScheduler(), "RoundRobin", false));
        System.out.println("\n========== Running Shortest Job First Scheduler ==========");
        results.put("SJF", simulateAndGetAverage(new ShortestJobFirstScheduler(), "SJF", true));

        ComparisonChartGenerator.showTurnaroundComparison(results);
    }

    private static double simulateAndGetAverage(Scheduler scheduler, String label, boolean useSJF) {

//        int numCores = 4;
//        int totalMemoryPerCore = 1000;
        MessageBus bus = new MessageBus(4);
        
        MessageBus messageBus = new MessageBus(numCores);
        MetricsCollector metricsCollector = new MetricsCollector();
        MemoryTracker memoryTracker = new MemoryTracker();

        List<Core> cores = new ArrayList<>();
        List<Scheduler> schedulers = new ArrayList<>();
        List<MemoryManager> memoryManagers = new ArrayList<>();

        for (int i = 0; i < numCores; i++) {
            Scheduler schedulerr = useSJF ? new ShortestJobFirstScheduler() : new RoundRobinScheduler();
            MemoryManager memoryManager = new MemoryManager(totalMemoryPerCore);
            schedulers.add(schedulerr);
            memoryManagers.add(memoryManager);

            Core core = new Core(i, scheduler, memoryManager, messageBus, metricsCollector, memoryTracker, numCores);
            cores.add(core);
        }

        for (int i = 0; i < 50; i++) {
            int burst = 200 + (int)(Math.random() * 800);
            int mem = 50 + (int)(Math.random() * 150);
            Task task = new Task(i, burst, mem, System.currentTimeMillis());
            scheduler.addTask(task);
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
        	for (Core c : cores) c.start();
            Thread.sleep(20000);
            for (Core c : cores) {
                c.stopCore();
                c.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        

        metricsCollector.exportUtilizationCSV(label + "_metrics.csv");
        return metricsCollector.getAverageTurnaround();
    }

    // ----------------------------------------------------------------------
    // Scheduler Comparison Section (adds RR vs SJF comparison + CSVs)
    // ----------------------------------------------------------------------
    private static void rundecentralizeSchedulerComparison() {
        try {
            System.out.println("\n========== Running Scheduler Comparison ==========");

//            int numCores = 2;
//            int numTasks = 50;
            int totalMemoryPerCore = 1000;

            // Generate workload
            List<Task> workload = new ArrayList<>();
            Random rand = new Random();
            for (int i = 0; i < numTasks; i++) {
                int burst = 200 + rand.nextInt(800);
                int mem = 50 + rand.nextInt(150);
                workload.add(new Task(i + 1, burst, mem, System.currentTimeMillis()));
            }

            Map<String, Double> avgTurnaroundMap = new LinkedHashMap<>();

            // ------------------ ROUND ROBIN ------------------
            {
                MessageBus bus = new MessageBus(numCores);
                MetricsCollector metrics = new MetricsCollector();
                MemoryTracker tracker = new MemoryTracker();

                List<Core> cores = new ArrayList<>();
                List<Scheduler> scheds = new ArrayList<>();

                for (int i = 0; i < numCores; i++) {
                    Scheduler s = new RoundRobinScheduler();
                    MemoryManager mm = new MemoryManager(totalMemoryPerCore);
                    scheds.add(s);
                    cores.add(new Core(i, s, mm, bus, metrics, tracker, numCores));
                }

                for (int i = 0; i < workload.size(); i++) {
                    scheds.get(i % numCores).addTask(workload.get(i));
                }

                for (Core c : cores) c.start();
                Thread.sleep(20000);
                for (Core c : cores) {
                    c.stopCore();
                    c.join();
                }

                metrics.exportCSV("RoundRobin_metrics.csv");
                avgTurnaroundMap.put("Round Robin", metrics.getAverageTurnaround());
                System.out.println("📁 RoundRobin_metrics.csv saved");
            }

            // ------------------ SJF ------------------
            {
                MessageBus bus = new MessageBus(numCores);
                MetricsCollector metrics = new MetricsCollector();
                MemoryTracker tracker = new MemoryTracker();

                List<Core> cores = new ArrayList<>();
                List<Scheduler> scheds = new ArrayList<>();

                for (int i = 0; i < numCores; i++) {
                    Scheduler s = new ShortestJobFirstScheduler();
                    MemoryManager mm = new MemoryManager(totalMemoryPerCore);
                    scheds.add(s);
                    cores.add(new Core(i, s, mm, bus, metrics, tracker, numCores));
                }

                for (int i = 0; i < workload.size(); i++) {
                    scheds.get(i % numCores).addTask(workload.get(i));
                }

                for (Core c : cores) c.start();
                Thread.sleep(20000);
                for (Core c : cores) {
                    c.stopCore();
                    c.join();
                }

                metrics.exportCSV("SJF_metrics.csv");
                avgTurnaroundMap.put("SJF", metrics.getAverageTurnaround());
                System.out.println("📁 SJF_metrics.csv saved");
            }

            // ------------------ CHART GENERATION ------------------
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, Double> e : avgTurnaroundMap.entrySet()) {
                dataset.addValue(e.getValue(), "Avg Turnaround Time", e.getKey());
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Scheduler Comparison - Average Turnaround Time",
                    "Scheduler", "Time (ms)", dataset);

            // Show chart
            ChartFrame frame = new ChartFrame("Scheduler Comparison", chart);
            frame.pack();
            frame.setVisible(true);

            System.out.println("📊 Scheduler Comparison chart displayed.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------
    private static List<Task> generateTasks(int n) {
        Random rand = new Random();
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int burst = 200 + rand.nextInt(800);
            int mem = 50 + rand.nextInt(150);
            tasks.add(new Task(i + 1, burst, mem, System.currentTimeMillis()));
        }
        return tasks;
    }

    private static void printSummary(MetricsCollector metricsCollector, MessageBus messageBus) {
        System.out.println("\n========== SUMMARY ==========");
        double avgTurnaround = metricsCollector.getAverageTurnaround();
        double avgWait = metricsCollector.getAverageWaiting();

        System.out.printf("Average Turnaround Time: %.2f ms%n", avgTurnaround);
        System.out.printf("Average Waiting Time: %.2f ms%n", avgWait);

        Map<Integer, Double> utilMap = metricsCollector.getCoreUtilizationPercent();
        System.out.println("\n--- CPU Utilization per Core ---");
        for (Map.Entry<Integer, Double> e : utilMap.entrySet()) {
            System.out.printf("Core %d: %.2f%%%n", e.getKey(), e.getValue());
        }

        System.out.println("\n--- Message Frequency (sent) ---");
        Map<Integer, Integer> sentMap = messageBus.getSentCountSnapshot();
        for (Map.Entry<Integer, Integer> e : sentMap.entrySet()) {
            System.out.printf("Core %d: %d messages sent%n", e.getKey(), e.getValue());
        }

        System.out.println("\n--- Message Frequency (received) ---");
        Map<Integer, Integer> recvMap = messageBus.getRecvCountSnapshot();
        for (Map.Entry<Integer, Integer> e : recvMap.entrySet()) {
            System.out.printf("Core %d: %d messages received%n", e.getKey(), e.getValue());
        }
        System.out.println("=================================\n");
    }
}
