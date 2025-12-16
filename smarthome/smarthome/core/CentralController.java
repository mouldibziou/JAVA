package smarthome.core;

import java.util.ArrayList;
import java.util.List;

import smarthome.devices.EnergyConsumer;
import smarthome.devices.Light2;
import smarthome.devices.SmartDevice;
import smarthome.devices.Thermostat;
import smarthome.exceptions.DeviceNotFoundException;

public class CentralController {

    private final Home home;

    public CentralController(Home home) {
        if (home == null)
            throw new IllegalArgumentException("Home cannot be null.");
        this.home = home;
    }

    public Home getHome() {
        return home;
    }

    // ===== ROOM MANAGEMENT =====
    public void addRoom(Room room) {
        home.addRoom(room);
    }

    private Room findRoomByName(String roomName) throws DeviceNotFoundException {
        for (Room r : home.getRooms()) {
            if (r.getName().equalsIgnoreCase(roomName))
                return r;
        }
        throw new DeviceNotFoundException("Room not found: " + roomName);
    }

    // ===== DEVICE MANAGEMENT =====
    public void addDeviceToRoom(String roomName, SmartDevice device) throws DeviceNotFoundException {
        Room room = findRoomByName(roomName);
        room.addDevice(device);

        // keep device aware of room (SmartDevice must have setRoomName)
        device.setRoomName(room.getName());

        System.out.println(device.getName() + " added to " + room.getName());
    }

    public void removeDeviceFromRoom(String roomName, SmartDevice device) throws DeviceNotFoundException {
        Room room = findRoomByName(roomName);
        room.removeDevice(device);
    }

    // ===== GLOBAL ACTIONS =====
    public void turnOffAllDevices() {
        for (Room r : home.getRooms()) {
            for (SmartDevice d : r.getDevices()) {
                d.turnOff();
            }
        }
        System.out.println("All devices turned OFF.");
    }

    public void turnOnAllDevices() {
        for (Room r : home.getRooms()) {
            for (SmartDevice d : r.getDevices()) {
                d.turnOn();
            }
        }
        System.out.println("All devices turned ON.");
    }

    public void turnOffRoomDevices(String roomName) throws DeviceNotFoundException {
        Room room = findRoomByName(roomName);
        for (SmartDevice d : room.getDevices()) {
            d.turnOff();
        }
        System.out.println("All devices in " + roomName + " turned OFF.");
    }

    // ===== LISTING =====
    public void listAllDevices() {
        System.out.println("\n===== ALL DEVICES =====");
        for (Room r : home.getRooms()) {
            System.out.println("Room: " + r.getName());
            for (SmartDevice d : r.getDevices()) {
                System.out.println(" - " + d.getStatus());
            }
        }
    }

    // ===== LOOKUP =====
    public SmartDevice findDeviceById(String id) {
        for (Room r : home.getRooms()) {
            for (SmartDevice d : r.getDevices()) {
                if (d.getId().equals(id)) {
                    return d;
                }
            }
        }
        return null;
    }

    // ===== ENERGY =====
    public double getTotalEnergyConsumption() {
        double total = 0.0;
        for (Room r : home.getRooms()) {
            for (SmartDevice d : r.getDevices()) {
                total += d.getEnergyConsumption();
            }
        }
        return total;
    }

    // More “real” optimization: dim lights + eco for energy consumers + reduce
    // thermostat target if possible
    public void optimizeEnergy() {
        for (Room r : home.getRooms()) {
            for (SmartDevice d : r.getDevices()) {

                // 1) If device supports EnergyConsumer => ECO
                if (d instanceof EnergyConsumer ec) {
                    ec.setEnergyMode("ECO");
                }

                // 2) If it's a Light2 and ON => dim to 40%
                if (d instanceof Light2 light && light.isOn()) {
                    //
                    light.setBrightness(Math.min(light.getBrightness(), 40));
                }

                // 3) If it's a Thermostat => you can reduce target if your Thermostat has a
                // method for it
                // If your Thermostat doesn't have setTargetTemp, remove this block
                if (d instanceof Thermostat t) {
                    // Example only (depends on your Thermostat class!)
                    // t.setTargetTemperature(20.0);
                }
            }
        }
        System.out.println("Energy optimization applied.");
    }
}
