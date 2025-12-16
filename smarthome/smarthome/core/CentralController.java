package smarthome.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalTime;

import smarthome.devices.EnergyConsumer;
import smarthome.devices.Light2;
import smarthome.devices.Schedulable;
import smarthome.devices.SmartDevice;
import smarthome.devices.Thermostat;
import smarthome.exceptions.DeviceNotFoundException;

public class CentralController {

    private final Home home;
    private final Map<String, SmartDevice> deviceCache; // Cache for O(1) lookup

    /**
     * Constructs a CentralController for a specific home.
     * 
     * @param home The home to control.
     */
    public CentralController(Home home) {
        if (home == null)
            throw new IllegalArgumentException("Home cannot be null.");
        this.home = home;
        this.deviceCache = new HashMap<>();
        refreshCache();
    }

    /**
     * Rebuilds the device cache from the Home structure.
     */
    private void refreshCache() {
        deviceCache.clear();
        for (Room r : home.getRooms()) {
            for (SmartDevice d : r.getDevices()) {
                deviceCache.put(d.getId(), d);
            }
        }
    }

    /**
     * Gets the associated Home instance.
     * 
     * @return The Home object.
     */
    public Home getHome() {
        return home;
    }

    // ===== ROOM MANAGEMENT =====
    // ===== ROOM MANAGEMENT =====

    /**
     * Adds a room to the home.
     * 
     * @param room The room to add.
     */
    public void addRoom(Room room) {
        home.addRoom(room);
    }

    /**
     * Finds a room by its name.
     * 
     * @param roomName The name of the room.
     * @return The Room object.
     * @throws DeviceNotFoundException If the room is not found.
     */
    private Room findRoomByName(String roomName) throws DeviceNotFoundException {
        for (Room r : home.getRooms()) {
            if (r.getName().equalsIgnoreCase(roomName))
                return r;
        }
        throw new DeviceNotFoundException("Room not found: " + roomName);
    }

    // ===== DEVICE MANAGEMENT =====
    // ===== DEVICE MANAGEMENT =====

    /**
     * Adds a device to a specific room.
     * 
     * @param roomName The name of the room.
     * @param device   The device to add.
     * @throws DeviceNotFoundException If the room does not exist.
     */
    public void addDeviceToRoom(String roomName, SmartDevice device) throws DeviceNotFoundException {
        Room room = findRoomByName(roomName);
        room.addDevice(device);

        // keep device aware of room (SmartDevice must have setRoomName)
        device.setRoomName(room.getName());
        deviceCache.put(device.getId(), device); // Update cache

        System.out.println(device.getName() + " added to " + room.getName());
    }

    /**
     * Removes a device from a room.
     * 
     * @param roomName The name of the room.
     * @param device   The device to remove.
     * @throws DeviceNotFoundException If the room or device is not found.
     */
    public void removeDeviceFromRoom(String roomName, SmartDevice device) throws DeviceNotFoundException {
        Room room = findRoomByName(roomName);
        room.removeDevice(device);
        deviceCache.remove(device.getId()); // Update cache
    }

    // ===== GLOBAL ACTIONS =====
    // ===== GLOBAL ACTIONS =====

    /**
     * Turns off all devices in the home.
     */
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
    /**
     * Lists all devices and their status.
     */
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
    // ===== LOOKUP =====

    /**
     * Finds a device by its ID.
     * 
     * @param id The ID of the device.
     * @return The SmartDevice object, or null if not found.
     */
    public SmartDevice findDeviceById(String id) {
        // Use cache for O(1) lookup
        if (deviceCache.containsKey(id)) {
            return deviceCache.get(id);
        }
        // Fallback or just return null
        return null;
    }

    /**
     * Gets a list of devices matching a specific type.
     * 
     * @param type The type of device (e.g., "Light", "Thermostat").
     * @return A list of matching SmartDevices.
     */
    public List<SmartDevice> getDevicesByType(String type) {
        List<SmartDevice> result = new ArrayList<>();
        for (SmartDevice d : deviceCache.values()) {
            if (d.getType().equalsIgnoreCase(type)) {
                result.add(d);
            }
        }
        return result;
    }

    // ===== ENERGY =====
    // ===== ENERGY =====

    /**
     * Calculates the total energy consumption of all devices.
     * 
     * @return Total energy in Watts.
     */
    public double getTotalEnergyConsumption() {
        double total = 0.0;
        for (SmartDevice d : deviceCache.values()) {
            total += d.getEnergyConsumption();
        }
        return total;
    }

    // More “real” optimization: dim lights + eco for energy consumers + reduce
    // thermostat target if possible
    /**
     * Optimizes energy consumption by adjusting device settings.
     * Sets ECO mode for supported devices and dims lights.
     */
    public void optimizeEnergy() {
        for (SmartDevice d : deviceCache.values()) {

            // 1) If device supports EnergyConsumer => ECO
            if (d instanceof EnergyConsumer ec) {
                ec.setEnergyMode("ECO");
            }

            // 2) If it's a Light2 and ON => dim to 40%
            if (d instanceof Light2 light && light.isOn()) {
                light.setBrightness(Math.min(light.getBrightness(), 40));
            }
        }
        System.out.println("Energy optimization applied.");
    }

    /**
     * Checks schedulable devices and executes actions if time matches.
     * 
     * @param time The current simulation time.
     */
    public void checkSchedules(LocalTime time) {
        String currentTimeStr = time.toString().substring(0, 5); // "HH:mm"
        for (SmartDevice d : deviceCache.values()) {
            if (d instanceof Schedulable schedulable) {
                // Determine if schedule matches
                // Note: Schedulable interface is simple (String getSchedule()), parsing
                // required
                // For this demo, we assume schedule string format "Action at HH:mm"
                String schedule = schedulable.getSchedule();
                if (schedule != null && schedule.contains(" at " + currentTimeStr)) {
                    System.out.println("Executing schedule for " + d.getName());
                    if (schedule.toLowerCase().startsWith("on"))
                        d.turnOn();
                    else if (schedule.toLowerCase().startsWith("off"))
                        d.turnOff();
                }
            }
        }
    }
}
