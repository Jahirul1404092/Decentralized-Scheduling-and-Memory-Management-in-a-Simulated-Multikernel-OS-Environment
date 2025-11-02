package multikernel;

import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
public class MetricsCollector {

    // Inner class to hold task execution records
    public static class Record {
        public int coreId;
        public int taskId;
        public long arrivalTime;
        public long startTime;
        public long endTime;

        public Record(int coreId, int taskId, long arrival, long start, long end) {
            this.coreId = coreId;
            this.taskId = taskId;
            this.arrivalTime = arrival;
            this.startTime = start;
            this.endTime = end;
        }
    }

    private final List<Record> records = new ArrayList<>();

    // Method to log each task's execution timeline
    public synchronized void recordExecution(int coreId, Task task, long startTime, long endTime) {
        records.add(new Record(coreId, task.getId(), task.getArrivalTime(), startTime, endTime));
    }

    // Allow other classes to access the collected records
    public List<Record> getRecords() {
        return records;
    }

    // Print summary statistics to the console
    public void printSummary() {
        System.out.println("------ Task Execution Summary ------");
        for (Record r : records) {
            long turnaround = r.endTime - r.arrivalTime;
            long waitTime = r.startTime - r.arrivalTime;
            System.out.printf("Task %d | Core %d | Wait: %d ms | Exec: %d ms | Turnaround: %d ms\n",
                    r.taskId, r.coreId, waitTime, r.endTime - r.startTime, turnaround);
        }
        System.out.println("------------------------------------");
    }
    public void exportToCSV(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Task ID,Core ID,Wait Time (ms),Exec Time (ms),Turnaround Time (ms)");
            for (Record r : records) {
                long wait = r.startTime - r.arrivalTime;
                long exec = r.endTime - r.startTime;
                long turnaround = r.endTime - r.arrivalTime;
                writer.printf("%d,%d,%d,%d,%d\n", r.taskId, r.coreId, wait, exec, turnaround);
            }
            System.out.println("CSV exported: " + filename);
        } catch (IOException e) {
            System.err.println("Error writing CSV: " + e.getMessage());
        }
    }
    public double computeAverageTurnaround() {
        if (records.isEmpty()) return 0.0;

        long total = 0;
        for (Record r : records) {
            total += (r.endTime - r.arrivalTime);
        }
        return total / (double) records.size();
    }

}
