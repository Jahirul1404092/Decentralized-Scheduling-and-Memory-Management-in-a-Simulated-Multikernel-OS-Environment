package multikernel;

public class MemoryManager {
    private int totalMemory;
    private int usedMemory;

    public MemoryManager(int totalMemory) {
        this.totalMemory = totalMemory;
        this.usedMemory = 0;
    }

    public synchronized boolean allocate(int mem) {
        if (usedMemory + mem <= totalMemory) {
            usedMemory += mem;
            return true;
        }
        return false;
    }

    public synchronized void deallocate(int mem) {
        usedMemory -= mem;
        if (usedMemory < 0) usedMemory = 0;
    }

    public int getUsedMemory() {
        return usedMemory;
    }
}
