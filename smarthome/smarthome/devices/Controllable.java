package smarthome.devices;



/**
 * Interface for devices that can be controlled remotely
 */
public interface Controllable {
    void executeCommand(String command);
    boolean isResponding();
}

