package multikernel;

public interface Scheduler {
    void addTask(Task task);
    Task getNextTask();
}
