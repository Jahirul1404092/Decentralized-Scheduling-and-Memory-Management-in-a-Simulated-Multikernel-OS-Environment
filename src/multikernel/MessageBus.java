package multikernel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * MessageBus models inter-core communication.
 *
 * Features:
 *  - Asynchronous "fire-and-forget" messaging (sendAsync)
 *  - Synchronous request/reply (sendSyncRequest + sendSyncReply)
 *  - Message frequency accounting (sentCount / recvCount per core)
 *
 * Usage pattern for sync:
 *  Core A: call sendSyncRequest(A, B, requestMsg) -> blocks until reply
 *  Core B: inside its run loop, pollMessage(B) and respond with sendSyncReply(...)
 */
public class MessageBus {

    /**
     * Message structure.
     * type: semantic label, e.g. "RESOURCE_REQUEST", "RESOURCE_GRANTED", "OFFLOAD_REQUEST"
     * task: optional task being requested / migrated
     * fromCore / toCore: endpoints
     * correlationId: used to match sync replies to requests
     */
    public static class Message {
        private final String type;
        private final Task task;
        private final int fromCore;
        private final int toCore;
        private final String correlationId;

        public Message(String type, Task task, int fromCore, int toCore, String correlationId) {
            this.type = type;
            this.task = task;
            this.fromCore = fromCore;
            this.toCore = toCore;
            this.correlationId = (correlationId == null)
                    ? UUID.randomUUID().toString()
                    : correlationId;
        }

        public String getType() {
            return type;
        }

        public Task getTask() {
            return task;
        }

        public int getFromCore() {
            return fromCore;
        }

        public int getToCore() {
            return toCore;
        }

        public String getCorrelationId() {
            return correlationId;
        }

        @Override
        public String toString() {
            return "[Message " + type +
                    " from core " + fromCore +
                    " to core " + toCore +
                    " task=" + (task != null ? task.getId() : "null") +
                    " cid=" + correlationId + "]";
        }
    }

    // One incoming queue (inbox) per core.
    private final Map<Integer, BlockingQueue<Message>> inboxes = new HashMap<>();

    // Tracks how many messages each core has sent/received.
    // We use Integer counters protected by synchronized updaters below.
    private final Map<Integer, Integer> sentCount = new HashMap<>();
    private final Map<Integer, Integer> recvCount = new HashMap<>();

    // For synchronous messaging:
    // Each core can have multiple outstanding sync requests.
    // We map correlationId -> blocking queue that will get the reply.
    private final Map<Integer, Map<String, BlockingQueue<Message>>> pendingReplyMap = new HashMap<>();

    // number of cores total
    private final int numCores;

    public MessageBus(int numCores) {
        this.numCores = numCores;
        for (int coreId = 0; coreId < numCores; coreId++) {
            inboxes.put(coreId, new LinkedBlockingQueue<>());
            sentCount.put(coreId, 0);
            recvCount.put(coreId, 0);
            pendingReplyMap.put(coreId, new ConcurrentHashMap<>());
        }
    }

    // ---------------------
    // Internal accounting
    // ---------------------

    private synchronized void incrementSend(int coreId) {
        sentCount.put(coreId, sentCount.get(coreId) + 1);
    }

    private synchronized void incrementRecv(int coreId) {
        recvCount.put(coreId, recvCount.get(coreId) + 1);
    }

    // ---------------------
    // Asynchronous messaging
    // ---------------------

    /**
     * Fire-and-forget: place msg into receiver's inbox.
     */
    public void sendAsync(int fromCoreId, int toCoreId, Message msg) {
        BlockingQueue<Message> q = inboxes.get(toCoreId);
        if (q != null) {
            q.offer(msg);
            incrementSend(fromCoreId);
            incrementRecv(toCoreId);
        }
    }

    /**
     * Non-blocking receive: grab next message from this core's inbox if any.
     */
    public Message pollMessage(int coreId) {
        BlockingQueue<Message> q = inboxes.get(coreId);
        if (q == null) return null;
        return q.poll();
    }

    // ---------------------
    // Synchronous messaging
    // ---------------------

    /**
     * Core 'fromCoreId' sends a request to 'toCoreId' and BLOCKS until a response
     * with the same correlationId arrives.
     */
    public Message sendSyncRequest(int fromCoreId, int toCoreId, Message request) throws InterruptedException {
        BlockingQueue<Message> replyQueue = new LinkedBlockingQueue<>();
        pendingReplyMap.get(fromCoreId).put(request.getCorrelationId(), replyQueue);

        BlockingQueue<Message> destInbox = inboxes.get(toCoreId);
        if (destInbox != null) {
            destInbox.put(request);
            incrementSend(fromCoreId);
            incrementRecv(toCoreId);
        }

        Message reply = replyQueue.take();
        pendingReplyMap.get(fromCoreId).remove(request.getCorrelationId());
        return reply;
    }

    /**
     * Core 'fromCoreId' sends a reply back to 'toCoreId' that UNBLOCKS that core's waiting thread.
     */
    public void sendSyncReply(int fromCoreId, int toCoreId, Message reply) {
        Map<String, BlockingQueue<Message>> waitingMap = pendingReplyMap.get(toCoreId);
        BlockingQueue<Message> waiter = waitingMap.get(reply.getCorrelationId());
        if (waiter != null) {
            waiter.offer(reply);
            incrementSend(fromCoreId);
            incrementRecv(toCoreId);
        } else {
            sendAsync(fromCoreId, toCoreId, reply);
        }
    }

    // ---------------------
    // Convenience helpers
    // ---------------------

    /**
     * Simple offload request: best-effort "I can't run this task, can you?"
     * This is async. The receiving core can accept by enqueueing the task.
     */
    public void requestOffload(int fromCoreId, Task t) {
        // naive: try next core in ring
        int target = (fromCoreId + 1) % numCores;
        Message offloadReq =
                new Message("OFFLOAD_REQUEST", t, fromCoreId, target, null);
        sendAsync(fromCoreId, target, offloadReq);
    }

    /**
     * Accessor so you can visualize "# messages sent per core".
     * Make a defensive copy so callers can chart safely.
     */
    public Map<Integer, Integer> getSentCountSnapshot() {
        return new HashMap<>(sentCount);
    }

    /**
     * Accessor so you can visualize "# messages received per core".
     */
    public Map<Integer, Integer> getRecvCountSnapshot() {
        return new HashMap<>(recvCount);
    }
}
