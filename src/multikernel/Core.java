package multikernel;

public class Core extends Thread {

    private final int coreId;
    private final Scheduler scheduler;
    private final MemoryManager memoryManager;
    private final MessageBus messageBus;
    private final MetricsCollector metricsCollector;
    private final MemoryTracker memoryTracker;

    // control loop
    private volatile boolean running = true;

    // utilization tracking
    private long coreStartTimeMs;
    private long coreEndTimeMs;
    private long busyTimeMs = 0L; // total time spent "executing tasks"

    // total number of cores in the system (used for routing decisions)
    private final int numCores;

    public Core(int coreId,
                Scheduler scheduler,
                MemoryManager memoryManager,
                MessageBus messageBus,
                MetricsCollector metricsCollector,
                MemoryTracker memoryTracker,
                int numCores) {
        this.coreId = coreId;
        this.scheduler = scheduler;
        this.memoryManager = memoryManager;
        this.messageBus = messageBus;
        this.metricsCollector = metricsCollector;
        this.memoryTracker = memoryTracker;
        this.numCores = numCores;
    }

    @Override
    public void run() {
        coreStartTimeMs = System.currentTimeMillis();

        while (running) {
            // 1. Service incoming messages first:
            //    This includes async offload and sync resource requests from other cores.
            MessageBus.Message incoming = messageBus.pollMessage(coreId);
            if (incoming != null) {
                handleIncomingMessage(incoming);
            }

            // 2. Pull next task from this core's scheduler
            Task task = scheduler.getNextTask();

            if (task == null) {
                // no local work right now
                try { Thread.sleep(5); } catch (InterruptedException ignored) {}
                continue;
            }

            long taskStartWall = System.currentTimeMillis(); // when we decided to run it

            // 3. Try to allocate memory locally
            boolean allocated = memoryManager.allocate(task.getMemoryRequired());
            if (!allocated) {
                // can't run locally because memory is tight
                // Strategy:
                //   Attempt synchronous request to another core for help.
                //   If they say RESOURCE_GRANTED, they'll enqueue it; we skip it.
                //   If denied, we put it back in our own queue and try later.

                int targetCore = pickOtherCore();
                MessageBus.Message req = new MessageBus.Message(
                        "RESOURCE_REQUEST",
                        task,
                        coreId,
                        targetCore,
                        null // correlationId auto-generated
                );

                try {
                    MessageBus.Message reply =
                            messageBus.sendSyncRequest(coreId, targetCore, req);

                    if ("RESOURCE_GRANTED".equals(reply.getType())) {
                        // remote core accepted and (in its handler) enqueued the task.
                        // We do NOT execute it here. We just continue loop.
                        continue;
                    } else {
                        // "RESOURCE_DENIED" (or anything else): requeue locally and try later
                        scheduler.addTask(task);
                        continue;
                    }
                } catch (InterruptedException e) {
                    // If sync request was interrupted, just requeue.
                    scheduler.addTask(task);
                    continue;
                }
            }

            // 4. Record memory snapshot for heatmap *after* allocation
            memoryTracker.record(coreId, memoryManager.getUsedMemory());

            // 5. "Run" the task (simulate CPU busy time)
            long execStart = System.currentTimeMillis();
            try {
                Thread.sleep(task.getBurstTime());
            } catch (InterruptedException ignored) {}
            long execEnd = System.currentTimeMillis();

            // 6. Free memory and record snapshot again
            memoryManager.deallocate(task.getMemoryRequired());
            memoryTracker.record(coreId, memoryManager.getUsedMemory());

            long taskEndWall = System.currentTimeMillis();

            // 7. Report per-task metrics
            metricsCollector.recordTaskCompletion(
                    coreId,
                    task,
                    taskStartWall,
                    taskEndWall
            );

            // 8. Update utilization accounting
            busyTimeMs += (execEnd - execStart);
        }

        coreEndTimeMs = System.currentTimeMillis();

        // When this core stops, report utilization
        metricsCollector.recordCoreUtilization(
                coreId,
                busyTimeMs,
                coreEndTimeMs - coreStartTimeMs
        );
    }

    /**
     * Graceful stop: core exits run() loop, computes utilization, and returns.
     */
    public void stopCore() {
        running = false;
    }

    /**
     * Pick a "partner" core to attempt offload / sync resource request.
     * Simple ring: next core ID mod numCores.
     */
    private int pickOtherCore() {
        if (numCores <= 1) {
            return coreId; // degenerate case
        }
        return (coreId + 1) % numCores;
    }

    /**
     * Handle incoming inter-core messages:
     * - OFFLOAD_REQUEST: other core is asking us to take a task asynchronously
     * - RESOURCE_REQUEST: other core is trying synchronous migration (blocked waiting)
     *   We must reply with RESOURCE_GRANTED or RESOURCE_DENIED using sendSyncReply.
     */
    private void handleIncomingMessage(MessageBus.Message msg) {
        String type = msg.getType();

        switch (type) {

            case "OFFLOAD_REQUEST": {
                // Asynchronous offload. If we have room, enqueue task here.
                Task incomingTask = msg.getTask();
                if (incomingTask != null && memoryManager.canFit(incomingTask.getMemoryRequired())) {
                    scheduler.addTask(incomingTask);
                }
                // If we can't fit, we just ignore it for now (best-effort).
                break;
            }

            case "RESOURCE_REQUEST": {
                // Another core is blocked waiting for us to answer.
                Task requestedTask = msg.getTask();
                boolean canTake = false;
                if (requestedTask != null) {
                    canTake = memoryManager.canFit(requestedTask.getMemoryRequired());
                }

                if (canTake && requestedTask != null) {
                    // We accept responsibility to eventually run this task.
                    scheduler.addTask(requestedTask);

                    // Send synchronous reply: RESOURCE_GRANTED
                    MessageBus.Message replyGranted = new MessageBus.Message(
                            "RESOURCE_GRANTED",
                            requestedTask,
                            coreId,                   // from me
                            msg.getFromCore(),        // to requester
                            msg.getCorrelationId()    // MUST echo correlationId so they unblock
                    );
                    messageBus.sendSyncReply(coreId, msg.getFromCore(), replyGranted);

                } else {
                    // We can't take it -> synchronous negative reply
                    MessageBus.Message replyDenied = new MessageBus.Message(
                            "RESOURCE_DENIED",
                            requestedTask,
                            coreId,
                            msg.getFromCore(),
                            msg.getCorrelationId()
                    );
                    messageBus.sendSyncReply(coreId, msg.getFromCore(), replyDenied);
                }
                break;
            }

            default: {
                // Unknown message types can be ignored or logged
                // e.g., future message types like "STATUS_PING", etc.
                break;
            }
        }
    }
}
