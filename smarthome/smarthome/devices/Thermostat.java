package smarthome.devices;


/**
 * Smart Thermostat with temperature control
 */
public class Thermostat extends SmartDevice implements Schedulable {
    private double currentTemperature;
    private double targetTemperature;
    private String mode; // "HEAT", "COOL", "AUTO"
    private String schedule;
    private String energyMode;

    public Thermostat(String name) {
        super(name, "Thermostat");
        this.currentTemperature = 20.0;
        this.targetTemperature = 22.0;
        this.mode = "AUTO";
        this.schedule = "None";
        this.energyMode = "NORMAL";
    }

    @Override
    public void turnOn() {
        setOn(true);
        System.out.println(getName() + " turned ON | Mode: " + mode + " | Target: " + targetTemperature + "°C");
    }

    @Override
    public void turnOff() {
        setOn(false);
        System.out.println(getName() + " turned OFF");
    }

    @Override
    public String getStatus() {
        String baseStatus = "[" + getId().substring(0, 8) + "] " + getName() +
                " (" + getType() + ") - " + (isOn() ? "ON" : "OFF");
        return baseStatus + String.format(" | Current: %.1f°C | Target: %.1f°C | Mode: %s",
                currentTemperature, targetTemperature, mode);
    }

    @Override
    public void executeCommand(String command) {
        if (command.startsWith("temp:")) {
            double temp = Double.parseDouble(command.split(":")[1]);
            setTargetTemperature(temp);
        } else if (command.startsWith("mode:")) {
            setMode(command.split(":")[1]);
        } else {
            super.executeCommand(command);
        }
    }

    @Override
    public boolean isResponding() {
        return true;
    }

    @Override
    public double getEnergyConsumption() {
        if (!isOn()) return 0;
        double baseConsumption = 1500; // 1500W
        double tempDiff = Math.abs(targetTemperature - currentTemperature);
        return baseConsumption * (1 + tempDiff * 0.1) *
                (energyMode.equals("ECO") ? 0.8 : 1.0);
    }

    @Override
    public void setEnergyMode(String mode) {
        this.energyMode = mode;
        System.out.println(getName() + " energy mode set to " + mode);
    }

    @Override
    public void scheduleAction(String action, String time) {
        this.schedule = action + " at " + time;
        System.out.println(getName() + " scheduled: " + schedule);
    }

    @Override
    public String getSchedule() {
        return schedule;
    }

    public void setTargetTemperature(double temp) {
        if (temp < 10 || temp > 35) {
            System.out.println("Invalid temperature. Must be 10-35°C");
            return;
        }
        this.targetTemperature = temp;
        System.out.println(getName() + " target temperature set to " + temp + "°C");
    }

    public void setMode(String mode) {
        this.mode = mode;
        System.out.println(getName() + " mode set to " + mode);
    }

    public void setCurrentTemperature(double temp) {
        this.currentTemperature = temp;
    }

    public double getCurrentTemperature() { return currentTemperature; }
    public double getTargetTemperature() { return targetTemperature; }
    public String getMode() { return mode; }
}