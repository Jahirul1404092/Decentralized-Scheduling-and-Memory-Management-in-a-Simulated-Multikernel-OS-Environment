package multikernel;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Collects and exports runtime metrics from all cores:
 *   - Task turnaround and waiting times
 *   - CPU utilization per core
 */
public class MetricsCollector {

    /** Represents one completed task record. */
    private static class TaskRecord {
        int coreId;
        int taskId;
        long arrivalTime;
        long startTime;
        long endTime;
    }

    /** Represents one core's utilization record. */
    private static class CoreUtilization {
        long busyMs;
        long totalMs;
    }

    private final List<TaskRecord> taskRecords = new ArrayList<>();
    private final Map<Integer, CoreUtilization> coreUtilization = new HashMap<>();

    // ----------------------------------------------------------------------
    // Task-level metrics
    // ----------------------------------------------------------------------

    /** Called by each core when it finishes running a task. */
    public synchronized void recordTaskCompletion(int coreId, Task task,
                                                  long startWall, long endWall) {
        TaskRecord record = new TaskRecord();
        record.coreId = coreId;
        record.taskId = task.getId();
        record.arrivalTime = task.getArrivalTime();
        record.startTime = startWall;
        record.endTime = endWall;
        taskRecords.add(record);
    }

    /** Computes average turnaround time (end - arrival). */
    public synchronized double getAverageTurnaround() {
        if (taskRecords.isEmpty()) return 0.0;
        double total = 0;
        for (TaskRecord r : taskRecords) {
            total += (r.endTime - r.arrivalTime);
        }
        return total / taskRecords.size();
    }

    /** Computes average waiting time (start - arrival). */
    public synchronized double getAverageWaiting() {
        if (taskRecords.isEmpty()) return 0.0;
        double total = 0;
        for (TaskRecord r : taskRecords) {
            total += (r.startTime - r.arrivalTime);
        }
        return total / taskRecords.size();
    }

    /** Exports per-task metrics to CSV. */
    public synchronized void exportCSV(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("coreId,taskId,arrivalTime,startTime,endTime,turnaroundMs,waitingMs");
            for (TaskRecord r : taskRecords) {
                long turnaround = r.endTime - r.arrivalTime;
                long waiting = r.startTime - r.arrivalTime;
                pw.printf(Locale.US,
                        "%d,%d,%d,%d,%d,%d,%d%n",
                        r.coreId, r.taskId,
                        r.arrivalTime, r.startTime, r.endTime,
                        turnaround, waiting);
            }
            System.out.println("✔ Task metrics exported to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------
    // Core utilization metrics
    // ----------------------------------------------------------------------

    /** Called by each core when it stops. */
    public synchronized void recordCoreUtilization(int coreId, long busyMs, long totalMs) {
        CoreUtilization util = coreUtilization.getOrDefault(coreId, new CoreUtilization());
        util.busyMs += busyMs;
        util.totalMs += totalMs;
        coreUtilization.put(coreId, util);
    }

    /** Returns utilization percentage per core. */
    public synchronized Map<Integer, Double> getCoreUtilizationPercent() {
        Map<Integer, Double> result = new HashMap<>();
        for (Map.Entry<Integer, CoreUtilization> entry : coreUtilization.entrySet()) {
            int coreId = entry.getKey();
            CoreUtilization u = entry.getValue();
            double percent = (u.totalMs == 0) ? 0.0 : (100.0 * u.busyMs / u.totalMs);
            result.put(coreId, percent);
        }
        return result;
    }

    /** Exports utilization stats to CSV. */
    public synchronized void exportUtilizationCSV(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("coreId,busyMs,totalMs,utilizationPercent");
            for (Map.Entry<Integer, CoreUtilization> entry : coreUtilization.entrySet()) {
                int coreId = entry.getKey();
                CoreUtilization u = entry.getValue();
                double pct = (u.totalMs == 0) ? 0.0 : (100.0 * u.busyMs / u.totalMs);
                pw.printf(Locale.US, "%d,%d,%d,%.2f%n",
                        coreId, u.busyMs, u.totalMs, pct);
            }
            System.out.println("✔ Utilization metrics exported to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /** Returns a map of TaskID -> Turnaround time (ms) for chart visualization */
    public synchronized Map<Integer, Long> getTaskTurnarounds() {
        Map<Integer, Long> map = new LinkedHashMap<>();
        for (var r : taskRecords) {
            long turnaround = r.endTime - r.arrivalTime;
            map.put(r.taskId, turnaround);
        }
        return map;
    }

}
