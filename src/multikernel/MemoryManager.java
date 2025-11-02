package multikernel;
import java.util.concurrent.locks.ReentrantLock;

public class MemoryManager {

    private final int totalMemory;
    private int usedMemory;

    private final ReentrantLock lock = new ReentrantLock(true);

    public MemoryManager(int totalMemory) {
        this.totalMemory = totalMemory;
        this.usedMemory = 0;
    }

    // Try to allocate memory. Return true if success.
    public boolean allocate(int amount) {
        lock.lock();
        try {
            if (usedMemory + amount > totalMemory) {
                return false;
            }
            usedMemory += amount;
            return true;
        } finally {
            lock.unlock();
        }
    }

    // Free memory. Safe if called with >allocated, will clamp at 0.
    public void deallocate(int amount) {
        lock.lock();
        try {
            usedMemory -= amount;
            if (usedMemory < 0) {
                usedMemory = 0;
            }
        } finally {
            lock.unlock();
        }
    }

    // Check if we COULD fit this much memory without actually allocating yet.
    public boolean canFit(int amount) {
        lock.lock();
        try {
            return (usedMemory + amount) <= totalMemory;
        } finally {
            lock.unlock();
        }
    }

    public int getUsedMemory() {
        lock.lock();
        try {
            return usedMemory;
        } finally {
            lock.unlock();
        }
    }

    public int getTotalMemory() {
        return totalMemory;
    }
}
