package smarthome;

import smarthome.core.CentralController;
import smarthome.core.Home;
import smarthome.core.Room;
import smarthome.devices.Light2;
import smarthome.devices.MotionSensor;
import smarthome.devices.SmartDevice;
import smarthome.devices.Thermostat;
import smarthome.automation.*;
import smarthome.exceptions.DeviceNotFoundException;
import java.time.LocalTime;

public class AutomationTest {
    public static void main(String[] args) {
        System.out.println("===== AUTOMATION ENGINE TEST =====");

        // 1. Setup
        Home home = new Home(1, "Test Home", 5, "TestCity");
        CentralController controller = new CentralController(home);
        AutomationEngine engine = new AutomationEngine(controller);

        Room livingRoom = new Room("Living Room");
        controller.addRoom(livingRoom);

        Light2 mainLight = new Light2("Main Light"); // ID will be random
        MotionSensor motion = new MotionSensor("Main Motion Sensor", 5);

        try {
            controller.addDeviceToRoom("Living Room", mainLight);
            controller.addDeviceToRoom("Living Room", motion);
        } catch (DeviceNotFoundException e) {
            e.printStackTrace();
        }

        // 2. Define Rules

        // Rule 1: Motion -> Turn Light On
        Action turnOnLight = new DeviceAction("ON", mainLight.getId(), false);
        Condition motionDetected = new SensorCondition("MotionSensor", true);
        AutomationRule motionRule = new AutomationRule("Motion Lights", motionDetected, turnOnLight);
        engine.addRule(motionRule);

        // Rule 2: Time Check (Simulation)
        // We'll create a rule matching the NEXT minute current time for test
        LocalTime now = LocalTime.now();
        LocalTime triggerTime = now; // Immediate trigger for test
        // Actually, TimeCondition checks Minute precision.
        Condition timeCheck = new TimeCondition(triggerTime);
        Action turnOffLight = new DeviceAction("OFF", mainLight.getId(), false);
        AutomationRule timeRule = new AutomationRule("Scheduled Off", timeCheck, turnOffLight);
        // We won't add it yet to avoid conflict.

        // 3. Execution Test
        System.out.println("\n--- Test 1: Motion Detection ---");
        System.out.println("Initial Light Status: " + mainLight.getStatus());

        // Simulate Motion
        motion.turnOn(); // Sensor must be ON to work
        motion.detectMotion();

        // Run Engine
        System.out.println("Evaluating Rules...");
        engine.evaluateRules();

        System.out.println("Final Light Status: " + mainLight.getStatus());
        if (mainLight.isOn())
            System.out.println("SUCCESS: Light turned on by motion.");
        else
            System.out.println("FAILURE: Light did not turn on.");

        System.out.println("\n--- Test 2: Threshold (Thermostat) ---");
        Thermostat thermo = new Thermostat("Smart Thermostat");
        try {
            controller.addDeviceToRoom("Living Room", thermo);
        } catch (DeviceNotFoundException e) {
        }

        Action heatOn = new DeviceAction("ON", thermo.getId(), false);
        // Condition: If Temp < 18, Turn ON
        // Let's set current temp to 15.
        thermo.setCurrentTemperature(15.0);
        Condition tempLow = new ThresholdCondition("Thermostat", "Temperature", "<", 18.0);
        AutomationRule heatRule = new AutomationRule("Auto Heat", tempLow, heatOn);
        engine.addRule(heatRule);

        System.out.println("Thermostat Status Before: " + thermo.getStatus());
        engine.evaluateRules();
        System.out.println("Thermostat Status After: " + thermo.getStatus());

        if (thermo.isOn())
            System.out.println("SUCCESS: Thermostat turned on.");
        else
            System.out.println("FAILURE: Thermostat failed to turn on.");

    }
}
