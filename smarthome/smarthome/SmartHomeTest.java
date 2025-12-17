package smarthome;

import smarthome.core.CentralController;
import smarthome.core.Home;
import smarthome.core.Room;
import smarthome.devices.Light2;
import smarthome.devices.MotionSensor;
import smarthome.devices.SmartDevice;
import smarthome.devices.Thermostat;
import smarthome.exceptions.DeviceNotFoundException;

public class SmartHomeTest {

    public static void main(String[] args) {

        System.out.println("===== SMART HOME TEST START =====");

        Home home = new Home(1, "Moi", 5, "Tunis");
        CentralController controller = new CentralController(home);

        // Add rooms
        controller.addRoom(new Room("Living Room"));
        controller.addRoom(new Room("Kitchen"));

        // Create devices
        SmartDevice light = new Light2("Main Light", 70);
        SmartDevice thermostat = new Thermostat("Heater");
        SmartDevice motionSensor = new MotionSensor("Entrance Sensor", 5);

        // Add devices
        try {
            controller.addDeviceToRoom("Living Room", light);
            controller.addDeviceToRoom("Living Room", thermostat);
            controller.addDeviceToRoom("Kitchen", motionSensor);
        } catch (DeviceNotFoundException e) {
            System.out.println(e.getMessage());
        }

        // List devices
        controller.listAllDevices();

        // Energy
        System.out.println("\nTotal energy consumption: " +
                controller.getTotalEnergyConsumption() + " W");

        // Optimize energy
        System.out.println("\n===== ENERGY OPTIMIZATION =====");
        controller.optimizeEnergy();

        controller.listAllDevices();

        // Turn off all
        System.out.println("\n===== TURN OFF ALL =====");
        controller.turnOffAllDevices();
        controller.listAllDevices();

        System.out.println("\n===== SMART HOME TEST END =====");

        // Start Dashboard Server
        try {
            System.out.println("\n===== STARTING DASHBOARD SERVER =====");
            System.out.println("Turning devices ON for dashboard demo...");
            controller.turnOnAllDevices();

            // Adjust path depending on where execution happens.
            // Assuming execution from workspace root 'SmartHomeProject2025' or 'smarthome'
            // We'll check if smarthome/web-content exists, else assume web-content
            String webPath = "smarthome/web-content";
            if (!new java.io.File(webPath).exists()) {
                webPath = "web-content";
            }

            smarthome.web.DashboardServer server = new smarthome.web.DashboardServer(controller, 8085, webPath);
            server.start();

            System.out.println("Dashboard running at http://localhost:8085");
            System.out.println("Press Ctrl+C to stop.");
        } catch (java.io.IOException e) {
            System.err.println("Failed to start dashboard server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
