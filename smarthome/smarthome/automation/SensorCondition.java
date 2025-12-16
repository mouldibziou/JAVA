package smarthome.automation;

import smarthome.core.CentralController;
import smarthome.devices.MotionSensor;
import smarthome.devices.SmartDevice;
import java.util.List;

public class SensorCondition implements Condition {
    private String sensorType; // e.g., "MotionSensor"
    private boolean expectedState; // true for detected, false for clear

    public SensorCondition(String sensorType, boolean expectedState) {
        this.sensorType = sensorType;
        this.expectedState = expectedState;
    }

    @Override
    public boolean evaluate(CentralController controller) {
        List<SmartDevice> devices = controller.getDevicesByType(sensorType);
        for (SmartDevice device : devices) {
            if (device instanceof MotionSensor) {
                MotionSensor sensor = (MotionSensor) device;
                if (sensor.isMotionDetected() == expectedState) {
                    return true; // If ANY sensor matches
                }
            }
        }
        return false;
    }
}
