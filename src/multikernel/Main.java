package multikernel;

import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting multikernel OS simulation...");

        // === Choose ONE scheduler ===
        Scheduler scheduler = new RoundRobinScheduler();
//        Scheduler scheduler = new ShortestJobFirstScheduler();

        // Memory managers (can be per core)
        MemoryManager memoryManager1 = new MemoryManager(1000);
        MemoryManager memoryManager2 = new MemoryManager(1000);

        // Shared components
        MessageBus messageBus = new MessageBus();
        MetricsCollector metrics = new MetricsCollector();
        
        MemoryTracker memoryTracker = new MemoryTracker();


        // Create and start cores
        Core core1 = new Core(1, scheduler, memoryManager1, messageBus, metrics, memoryTracker);
        Core core2 = new Core(2, scheduler, memoryManager2, messageBus, metrics, memoryTracker);

        core1.start();
        core2.start();

        // === Task generation ===
        for (int i = 0; i < 50; i++) {
            int burst = 200 + (int)(Math.random() * 800);   // 200–1000 ms
            int mem = 50 + (int)(Math.random() * 150);      // 50–200 MB
            Task task = new Task(i, burst, mem, System.currentTimeMillis());
            System.out.println("Task created: " + task);
            scheduler.addTask(task);
        }

        // Let the simulation run
        try {
            Thread.sleep(10000); // Give enough time for all tasks
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stop and join cores
        core1.stopCore();
        core2.stopCore();

        try {
            core1.join();
            core2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Simulation complete.");

        // === Output summary and chart ===
        metrics.printSummary();
        metrics.exportToCSV("metrics_output.csv");
        ChartGenerator.showTurnaroundChart(metrics.getRecords());
        MemoryHeatmapChart.show(memoryTracker);
//        GanttChartGenerator.showGanttChart(metrics.getRecords());

        // === Optional: Compare schedulers ===
        compareSchedulers();
    }

    // Optional feature: automated scheduler comparison chart
    public static void compareSchedulers() {
        Map<String, Double> results = new LinkedHashMap<>();

        results.put("Round Robin", simulateAndGetAverage(new RoundRobinScheduler(), "RoundRobin"));
        results.put("SJF", simulateAndGetAverage(new ShortestJobFirstScheduler(), "SJF"));

        ComparisonChartGenerator.showTurnaroundComparison(results);
    }

    private static double simulateAndGetAverage(Scheduler scheduler, String label) {
        MemoryManager memoryManager1 = new MemoryManager(1000);
        MemoryManager memoryManager2 = new MemoryManager(1000);
        MessageBus messageBus = new MessageBus();
        MetricsCollector metrics = new MetricsCollector();

        MemoryTracker memoryTracker = new MemoryTracker();
        Core core1 = new Core(1, scheduler, memoryManager1, messageBus, metrics, memoryTracker);
        Core core2 = new Core(2, scheduler, memoryManager2, messageBus, metrics, memoryTracker);
        core1.start();
        core2.start();

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

        core1.stopCore();
        core2.stopCore();

        try {
            core1.join();
            core2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        metrics.exportToCSV(label + "_metrics.csv");
        return metrics.computeAverageTurnaround();
    }
}
