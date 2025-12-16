package smarthome.automation;

import smarthome.core.CentralController;
import smarthome.devices.SmartDevice;
import java.util.List;

public class GroupStateCondition implements Condition {
    private String deviceType;
    private boolean requiredState; // true for ON, false for OFF
    private boolean allMustMatch; // true for "ALL", false for "ANY"

    public GroupStateCondition(String deviceType, boolean requiredState, boolean allMustMatch) {
        this.deviceType = deviceType;
        this.requiredState = requiredState;
        this.allMustMatch = allMustMatch;
    }

    @Override
    public boolean evaluate(CentralController controller) {
        List<SmartDevice> devices = controller.getDevicesByType(deviceType);
        if (devices.isEmpty())
            return false;

        if (allMustMatch) {
            // Check if ALL match
            for (SmartDevice d : devices) {
                if (d.isOn() != requiredState)
                    return false;
            }
            return true;
        } else {
            // Check if ANY match
            for (SmartDevice d : devices) {
                if (d.isOn() == requiredState)
                    return true;
            }
            return false;
        }
    }
}
