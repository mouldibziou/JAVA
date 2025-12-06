//Add: Custom exceptions (e.g., DeviceNotFoundException).

package smarthome.core;

import smarthome.devices.SmartDevice;

import java.util.ArrayList;
import java.util.List;

public class Home{
    // Home object can be used multiple times to add/remove rooms, change ownership of a home based on its id.
    // The attribute 'size' sets the size of a home depending on the number of devices it has.
    // numOfHomes attribute is static, it returns the total number of homes registered in the system.

    private String owner;
    private int homeId;
    private int numOfRooms;
    private String address;
    private String size; // Can use ENUM later
    static int numOfHomes = 0;
    private List<Room> rooms; // Composition concept.

    //think about rooms.
    public Home(int homeId, String owner, int numOfRooms, String address) {
        if (homeId <= 0){
            throw new IllegalArgumentException("Home ID must be positive.");
        }
        if(owner == null || owner.isEmpty()){
            throw new IllegalArgumentException("Owner name cannot be empty.");
        }
        if(numOfRooms <= 0){
            throw new IllegalArgumentException("Number of rooms must be positive.");
        }
        if(address == null || address.isEmpty()){
            throw new IllegalArgumentException("Address cannot be empty.");
        }

        this.homeId = homeId;
        this.owner = owner;
        this.numOfRooms = numOfRooms;
        this.address = address;
        this.rooms = new ArrayList<>();

        numOfHomes++;
    }

    //Rooms management section, room constructor will be instantiated within the SmartHomeTest class.
    //Then passed as an argument.
    public void addRoom(Room room){
        if (room == null) throw new IllegalArgumentException("Room cannot be null.");

        if (rooms.size() >= numOfRooms) throw new IllegalArgumentException("Cannot add another room, maximum rooms reached. \n Change the number of rooms for this house first.");

        for (Room r : rooms){
            if (r.getName().equalsIgnoreCase(room.getName())){
                throw new IllegalArgumentException(
                        "A room with the name '" + room.getName() + "' already exists."
                );
            }
        } // this loop checks for internal validity, not allowing multiple rooms with same name.
        rooms.add(room);
    }
    public void removeRoom(Room room){
        rooms.remove(room);
    }
    public List<Room> getRooms(){
        return rooms;
    }
    // Search method that allows locating a device by ID or type, using method overloading.
    public String deviceLocation(String id) {
        for (Room r : rooms) {
            SmartDevice found = r.findDeviceById(id);
            if (found != null) {
                return "Device with ID " + id + " found in room: " + r.getName();
            }
        }
        return "Device with ID " + id + " not found in this home.";
    }
    public String deviceLocationByType(String type) {
        for (Room r : rooms) {
            SmartDevice found = r.findDeviceByType(type);
            if (found != null) {
                return "Device of type " + type + " found in room: " + r.getName();
            }
        }
        return "No device of type " + type + " found in this home.";
    }

    // getters and setters for every attribute
    public String getOwner(){
        return owner;
    }
    public void changeOwnership(String newOwner){
        System.out.println("The ownership has been changed successfully from: " + this.owner);
        this.owner = newOwner;
        System.out.println(". To Mr/Mrs. " + this.owner);
    }
    public int getNumOfRooms() {
        return numOfRooms;
    }
    public void setNumOfRooms(int numOfRooms){
        this.numOfRooms = numOfRooms;
    }
    public String getAddress(){
        return address;
    }
    public void setAddress(String address) {
        if (address == null || address.isEmpty())
            throw new IllegalArgumentException("Address cannot be empty.");
        this.address = address;
    }
    public static int getNumOfHomes() {
        return numOfHomes;
    }
    public int getHomeId(){
        return homeId;
    }
}