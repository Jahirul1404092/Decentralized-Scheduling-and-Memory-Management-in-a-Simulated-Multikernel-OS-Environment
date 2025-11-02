package multikernel;

public class Core extends Thread {
    private final int coreId;
    private final Scheduler scheduler;
    private final MemoryManager memoryManager;
    private final MessageBus messageBus;
    private final MetricsCollector metrics;
    private final MemoryTracker memoryTracker;

    private boolean running = true;

    public Core(int coreId, Scheduler scheduler, MemoryManager memoryManager,
                MessageBus messageBus, MetricsCollector metrics, MemoryTracker memoryTracker) {
        this.coreId = coreId;
        this.scheduler = scheduler;
        this.memoryManager = memoryManager;
        this.messageBus = messageBus;
        this.metrics = metrics;
        this.memoryTracker = memoryTracker;
    }

    @Override
    public void run() {
        while (running) {
            Task task = scheduler.getNextTask();

            if (task != null) {
                System.out.println("Core " + coreId + " picked up task: " + task);

                if (memoryManager.allocate(task.getMemoryRequired())) {
                    System.out.println("Core " + coreId + " allocated memory for task: " + task.getId());

                    // Record immediately after allocation
                    if (memoryTracker != null)
                        memoryTracker.record(coreId, memoryManager.getUsedMemory());

                    simulateExecution(task);

                    // Record right before deallocation
                    if (memoryTracker != null)
                        memoryTracker.record(coreId, memoryManager.getUsedMemory());

                    memoryManager.deallocate(task.getMemoryRequired());
                    System.out.println("Core " + coreId + " deallocated memory for task: " + task.getId());
                } else {
                    System.out.println("Core " + coreId + " failed to allocate memory for task: " + task.getId());
                    messageBus.send(coreId, new Message("MEMORY_FAIL", task));
                }
            } else {
                try {
                    Thread.sleep(10); // Idle wait
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Sample memory periodically (background)
            if (memoryTracker != null)
                memoryTracker.record(coreId, memoryManager.getUsedMemory());

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void simulateExecution(Task task) {
        long startTime = System.currentTimeMillis();
        System.out.println("Core " + coreId + " is executing task: " + task);
        try {
            Thread.sleep(task.getBurstTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        metrics.recordExecution(coreId, task, startTime, endTime);
        System.out.println("Core " + coreId + " finished task: " + task);
    }

    public void stopCore() {
        running = false;
    }
}
