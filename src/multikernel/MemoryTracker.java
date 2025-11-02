package multikernel;

import java.util.*;

public class MemoryTracker {
    private final Map<Integer, List<Integer>> memoryLog = new HashMap<>(); // coreId → memory snapshots

    public synchronized void record(int coreId, int usedMemory) {
        memoryLog.computeIfAbsent(coreId, k -> new ArrayList<>()).add(usedMemory);
    }

    public Map<Integer, List<Integer>> getMemoryLog() {
        return memoryLog;
    }

    public int getMaxSteps() {
        return memoryLog.values().stream().mapToInt(List::size).max().orElse(0);
    }
}
