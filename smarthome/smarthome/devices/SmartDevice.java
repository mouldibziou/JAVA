package smarthome.devices;

import java.util.UUID;

public abstract class SmartDevice implements Controllable, EnergyConsumer {

    private final String id;
    private String name;
    private boolean isOn;
    private String roomName;
    private String type; // Device type for searching

    public SmartDevice(String name, String type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Device name cannot be empty.");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Device type cannot be empty.");
        }

        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.isOn = false;
        this.roomName = "Unassigned";
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isOn() {
        return isOn;
    }

    public String getRoomName() {
        return roomName;
    }

    // Setters
    protected void setOn(boolean on) {
        this.isOn = on;
    }

    public void setRoomName(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new IllegalArgumentException("Room name cannot be empty.");
        }
        this.roomName = roomName;
    }

    // Public methods for turning on/off (not overriding interface methods)
    public void turnOn() {
        if (isOn) {
            System.out.println(name + " is already ON.");
        } else {
            isOn = true;
            System.out.println(name + " has been turned ON.");
        }
    }

    public void turnOff() {
        if (!isOn) {
            System.out.println(name + " is already OFF.");
        } else {
            isOn = false;
            System.out.println(name + " has been turned OFF.");
        }
    }

    // Controllable interface implementation
    @Override
    public void executeCommand(String command) {
        if (command == null) {
            System.out.println("Invalid command.");
            return;
        }

        switch (command.toUpperCase()) {
            case "ON":
                turnOn();
                break;
            case "OFF":
                turnOff();
                break;
            default:
                System.out.println("Unknown command: " + command);
        }
    }

    @Override
    public boolean isResponding() {
        return true; // Default implementation - device is responding
    }

    // Abstract methods - must be implemented by subclasses
    public abstract String getStatus();

    // EnergyConsumer interface - concrete implementation in subclasses
    @Override
    public abstract double getEnergyConsumption();

    @Override
    public abstract void setEnergyMode(String mode);

    @Override
    public String toString() {
        return "[" + id.substring(0, 8) + "] " + name +
                " (" + type + ") - " + (isOn ? "ON" : "OFF");
    }
}