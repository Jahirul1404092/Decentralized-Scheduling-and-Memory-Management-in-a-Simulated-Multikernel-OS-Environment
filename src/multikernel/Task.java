package multikernel;
public class Task {
    private final int id;
    private final int burstTime;
    private final int memoryRequired;
    private final long arrivalTime;

    public Task(int id, int burstTime, int memoryRequired, long arrivalTime) {
        this.id = id;
        this.burstTime = burstTime;
        this.memoryRequired = memoryRequired;
        this.arrivalTime = arrivalTime;
    }

    // Getters
    public int getId() { return id; }
    public int getBurstTime() { return burstTime; }
    public int getMemoryRequired() { return memoryRequired; }
    public long getArrivalTime() { return arrivalTime; }

    @Override
    public String toString() {
        return "Task{id=" + id + ", burst=" + burstTime + ", mem=" + memoryRequired + "}";
    }
}
