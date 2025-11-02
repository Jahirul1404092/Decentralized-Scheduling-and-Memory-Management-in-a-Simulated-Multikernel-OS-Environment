package multikernel;

public class MessageBus {
    public synchronized void send(int fromCoreId, Message msg) {
        System.out.println("Message from Core " + fromCoreId + ": " + msg.getContent());
    }
}
