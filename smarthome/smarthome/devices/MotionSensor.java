package smarthome.devices;

public class MotionSensor extends SmartDevice {

    private boolean motionDetected;
    private int sensitivity; // 1-10
    private long lastMotionTime;
    private static final double ENERGY_CONSUMPTION = 2.0;

    public MotionSensor(String name) {
        super(name, "MotionSensor");
        this.motionDetected = false;
        this.sensitivity = 5;
        this.lastMotionTime = 0;
    }

    public MotionSensor(String name, int sensitivity) {
        super(name, "MotionSensor");
        setSensitivity(sensitivity);
        this.motionDetected = false;
        this.lastMotionTime = 0;
    }

    public boolean isMotionDetected() {
        return motionDetected;
    }

    public void detectMotion() {
        if (!isOn()) {
            System.out.println(getName() + " is OFF and cannot detect motion.");
            return;
        }
        this.motionDetected = true;
        this.lastMotionTime = System.currentTimeMillis();
        System.out.println("⚠ " + getName() + " DETECTED MOTION in " + getRoomName());
    }

    public void clearMotion() {
        this.motionDetected = false;
        System.out.println(getName() + " motion cleared");
    }

    public long getLastMotionTime() {
        return lastMotionTime;
    }

    public String getTimeSinceLastMotion() {
        if (lastMotionTime == 0) return "No motion detected yet";

        long seconds = (System.currentTimeMillis() - lastMotionTime) / 1000;
        if (seconds < 60) return seconds + " seconds ago";

        long minutes = seconds / 60;
        return minutes + " minutes ago";
    }

    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        if (sensitivity < 1 || sensitivity > 10) {
            throw new IllegalArgumentException("Sensitivity must be between 1 and 10.");
        }
        this.sensitivity = sensitivity;
        System.out.println(getName() + " sensitivity set to " + sensitivity);
    }

    @Override
    public void turnOn() {
        setOn(true);
        System.out.println(getName() + " turned ON - Monitoring for motion");
    }

    @Override
    public void turnOff() {
        setOn(false);
        motionDetected = false;
        System.out.println(getName() + " turned OFF - Motion monitoring stopped");
    }

    @Override
    public String getStatus() {
        if (isOn()) {
            String motionStatus = motionDetected ? "MOTION DETECTED!" : "No motion";
            return getName() + " [MotionSensor] is ON - Status: " + motionStatus +
                    " - Sensitivity: " + sensitivity + "/10" +
                    " - Last motion: " + getTimeSinceLastMotion() +
                    " - Energy: " + String.format("%.2f", getEnergyConsumption()) + "W" +
                    " - Room: " + getRoomName();
        } else {
            return getName() + " [MotionSensor] is OFF - Room: " + getRoomName();
        }
    }

    @Override
    public double getEnergyConsumption() {
        return isOn() ? ENERGY_CONSUMPTION : 0.0;
    }
    @Override
public void setEnergyMode(String mode) {
    if (mode == null) return;

    switch (mode.toUpperCase()) {
        case "ECO" -> setSensitivity(3);
        case "NORMAL" -> setSensitivity(5);
        case "HIGH" -> setSensitivity(8);
        default -> System.out.println("Unknown energy mode: " + mode);
    }
}

}
