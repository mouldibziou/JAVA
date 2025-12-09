package smarthome.devices;

import java.util.UUID;

public abstract class SmartDevice implements Controllable, EnergyConsumer {

    //chabeb zidou 'type' lahne, as an attribute yaani. W zidouna maah getType() method.
    private final String id;
    private String name;
    private boolean isOn;
    private String roomName;

    public SmartDevice(String name) {
        this.id = UUID.randomUUID().toString();   // generate unique ID
        this.name = name;
        this.isOn = false;
        this.roomName = "Unassigned";
    }

    public SmartDevice(String id, String name, String light) {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isOn() {
        return isOn;
    }

    protected void setOn(boolean on) {
        this.isOn = on;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    // Abstract: subclasses decide how to show details
    public abstract String getStatus();

    @Override
    public String toString() {
        return "[" + id.substring(0, 6) + "] "
                + name + " (" + (isOn ? "ON" : "OFF") + ")";
    }
}



'''khedmti ena (tannoubi)'

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

    // Controllable interface implementation (can be overridden)
    @Override
    public void turnOn() {
        if (isOn) {
            System.out.println(name + " is already ON.");
        } else {
            isOn = true;
            System.out.println(name + " has been turned ON.");
        }
    }

    @Override
    public void turnOff() {
        if (!isOn) {
            System.out.println(name + " is already OFF.");
        } else {
            isOn = false;
            System.out.println(name + " has been turned OFF.");
        }
    }

    // Abstract methods - must be implemented by subclasses
    public abstract String getStatus();

    // EnergyConsumer interface - concrete implementation in subclasses
    @Override
    public abstract double getEnergyConsumption();

    @Override
    public String toString() {
        return "[" + id.substring(0, 8) + "] " + name +
                " (" + type + ") - " + (isOn ? "ON" : "OFF");
    }
}
