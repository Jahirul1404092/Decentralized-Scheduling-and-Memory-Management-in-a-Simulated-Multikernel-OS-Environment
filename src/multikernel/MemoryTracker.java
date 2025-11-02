package multikernel;

import java.util.*;

/**
 * Tracks per-core memory usage snapshots.
 * Each core ID has a list of memory values recorded over time.
 */
public class MemoryTracker {

    private final Map<Integer, List<Integer>> memoryLog = new HashMap<>();

    /** Record current memory usage for a core. Called from Core.java */
    public synchronized void record(int coreId, int usedMemory) {
        memoryLog.computeIfAbsent(coreId, k -> new ArrayList<>()).add(usedMemory);
    }

    /** Expose the log for heatmap chart */
    public synchronized Map<Integer, List<Integer>> getMemoryLog() {
        return memoryLog;
    }

    /** Optionally clear for next run */
    public synchronized void reset() {
        memoryLog.clear();
    }
}
