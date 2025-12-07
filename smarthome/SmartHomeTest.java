package smarthome;

import smarthome.core.Home;
import smarthome.core.Room;

public class SmartHomeTest {
    public static void main(String[] args) {

        //Home home = new Home("My Smart Home");
        Room living = new Room("Living Room");
        Room kitchen = new Room("Kitchen");

        System.out.println("Smart Home initialized successfully!");
    }

}

