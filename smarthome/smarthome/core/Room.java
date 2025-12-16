package smarthome.core;

import java.util.ArrayList;
import java.util.List;
import smarthome.devices.SmartDevice;

public class Room {

    private String name;
    private List<SmartDevice> devices;

    public Room(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Room name cannot be empty");
        }
        this.name = name.trim();
        this.devices = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addDevice(SmartDevice device) {
        if (device == null) throw new IllegalArgumentException("Device cannot be null.");
        devices.add(device);
    }

    public void removeDevice(SmartDevice device) {
        devices.remove(device);
    }

    public int getNumOfDevices() {
        return devices.size();
    }

    public List<SmartDevice> getDevices() {
        return devices;
    }

    public SmartDevice findDeviceById(String id) {
        for (SmartDevice dev : devices) {
            if (dev.getId().equals(id)) return dev;
        }
        return null;
    }

    public SmartDevice findDeviceByType(String type) {
        for (SmartDevice dev : devices) {
            if (dev.getType().equalsIgnoreCase(type)) return dev;
        }
        return null;
    }
}
