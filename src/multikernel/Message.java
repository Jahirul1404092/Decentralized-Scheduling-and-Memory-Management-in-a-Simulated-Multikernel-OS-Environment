package multikernel;

public class Message {
    private String content;
    private Task task;

    public Message(String content, Task task) {
        this.content = content;
        this.task = task;
    }

    public String getContent() {
        return content;
    }

    public Task getTask() {
        return task;
    }
}
