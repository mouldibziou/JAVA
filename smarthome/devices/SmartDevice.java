package smarthome.devices;

import java.util.UUID;

public abstract class SmartDevice implements Controllable, EnergyConsumer {

    //chabeb zidou 'type' lahne, as an attribute yaani. W zidouna maah getType method.
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

