package smarthome.automation;

import smarthome.core.CentralController;
import smarthome.devices.SmartDevice;
import smarthome.devices.Thermostat;
import java.util.List;

public class ThresholdCondition implements Condition {
    private String deviceType;
    private String metric; // "Temperature"
    private double threshold;
    private String operator; // ">", "<", ">=", "<=", "=="

    public ThresholdCondition(String deviceType, String metric, String operator, double threshold) {
        this.deviceType = deviceType;
        this.metric = metric;
        this.operator = operator;
        this.threshold = threshold;
    }

    @Override
    public boolean evaluate(CentralController controller) {
        List<SmartDevice> devices = controller.getDevicesByType(deviceType);
        for (SmartDevice device : devices) {
            if (device instanceof Thermostat && metric.equalsIgnoreCase("Temperature")) {
                Thermostat t = (Thermostat) device;
                double val = t.getCurrentTemperature();
                if (compare(val, threshold, operator))
                    return true;
            }
            // Add other device types/metrics here
        }
        return false;
    }

    private boolean compare(double val, double threshold, String operator) {
        switch (operator) {
            case ">":
                return val > threshold;
            case "<":
                return val < threshold;
            case ">=":
                return val >= threshold;
            case "<=":
                return val <= threshold;
            case "==":
                return Math.abs(val - threshold) < 0.001;
            default:
                return false;
        }
    }
}
