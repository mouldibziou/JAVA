package smarthome.devices;

public interface Schedulable {
    void scheduleAction(String action, String time);
    String getSchedule();
}
