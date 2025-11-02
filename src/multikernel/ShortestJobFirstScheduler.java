package multikernel;

import java.util.PriorityQueue;
import java.util.Comparator;

public class ShortestJobFirstScheduler implements Scheduler {

    private final PriorityQueue<Task> queue = new PriorityQueue<>(Comparator.comparingInt(Task::getBurstTime));

    @Override
    public synchronized void addTask(Task task) {
        queue.offer(task);
    }

    @Override
    public synchronized Task getNextTask() {
        return queue.poll();
    }
}
