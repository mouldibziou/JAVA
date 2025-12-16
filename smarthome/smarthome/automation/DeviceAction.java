package smarthome.automation;

import smarthome.core.CentralController;
import smarthome.devices.SmartDevice;
import smarthome.devices.Thermostat;
import java.util.List;

public class DeviceAction implements Action {
    private String targetDeviceType; // null for specific ID
    private String targetDeviceId; // null for all of type
    private String command; // "ON", "OFF", "SET_TEMP:25"

    public DeviceAction(String command, String target, boolean isType) {
        this.command = command;
        if (isType) {
            this.targetDeviceType = target;
        } else {
            this.targetDeviceId = target;
        }
    }

    @Override
    public void execute(CentralController controller) {
        if (targetDeviceId != null) {
            SmartDevice d = controller.findDeviceById(targetDeviceId);
            if (d != null)
                executeOnDevice(d);
        } else if (targetDeviceType != null) {
            List<SmartDevice> devices = controller.getDevicesByType(targetDeviceType);
            for (SmartDevice d : devices) {
                executeOnDevice(d);
            }
        }
    }

    private void executeOnDevice(SmartDevice d) {
        if (command.equalsIgnoreCase("ON")) {
            d.turnOn();
        } else if (command.equalsIgnoreCase("OFF")) {
            d.turnOff();
        } else if (command.startsWith("SET_TEMP:")) {
            if (d instanceof Thermostat) {
                try {
                    double t = Double.parseDouble(command.split(":")[1]);
                    ((Thermostat) d).setTargetTemperature(t);
                } catch (Exception e) {
                    System.err.println("Invalid temp format");
                }
            }
        }
    }
}
