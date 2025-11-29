package smarthome.devices;

public interface Schedulable {
    void schedule(String timeExpression, Runnable task);
}

