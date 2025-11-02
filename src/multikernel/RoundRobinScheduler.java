package multikernel;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RoundRobinScheduler implements Scheduler {
    private final ConcurrentLinkedQueue<Task> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void addTask(Task task) {
        queue.add(task);
    }

    @Override
    public Task getNextTask() {
        return queue.poll();
    }
}
