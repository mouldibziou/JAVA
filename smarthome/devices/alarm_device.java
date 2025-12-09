package smarthome.devices;

public class SmartAlarm extends SmartDevice implements Schedulable {

    private boolean isArmed;
    private boolean isTriggered;
    private String alarmMode; // HOME, AWAY, NIGHT, DISARMED
    private int pinCode;
    private static final double ENERGY_CONSUMPTION = 5.0; // watts

    public SmartAlarm(String name, int pinCode) {
        super(name, "SmartAlarm");
        setPinCode(pinCode);
        this.isArmed = false;
        this.isTriggered = false;
        this.alarmMode = "DISARMED";
    }

    // Pin code management
    public void setPinCode(int pinCode) {
        if (pinCode < 1000 || pinCode > 9999) {
            throw new IllegalArgumentException("PIN code must be 4 digits (1000-9999).");
        }
        this.pinCode = pinCode;
        System.out.println(getName() + " PIN code updated");
    }

    public boolean verifyPin(int enteredPin) {
        return this.pinCode == enteredPin;
    }

    // Alarm control
    public void arm(int pin, String mode) {
        if (!verifyPin(pin)) {
            System.out.println("❌ " + getName() + " - Invalid PIN. Cannot arm alarm.");
            return;
        }

        String upperMode = mode.toUpperCase();
        if (!upperMode.equals("HOME") && !upperMode.equals("AWAY") && !upperMode.equals("NIGHT")) {
            throw new IllegalArgumentException("Mode must be HOME, AWAY, or NIGHT.");
        }

        if (!isOn()) {
            turnOn();
        }

        this.isArmed = true;
        this.alarmMode = upperMode;
        this.isTriggered = false;
        System.out.println("🛡 " + getName() + " ARMED in " + upperMode + " mode");
    }

    public void disarm(int pin) {
        if (!verifyPin(pin)) {
            System.out.println("❌ " + getName() + " - Invalid PIN. Cannot disarm alarm.");
            return;
        }

        this.isArmed = false;
        this.isTriggered = false;
        this.alarmMode = "DISARMED";
        System.out.println("✓ " + getName() + " DISARMED successfully");
    }

    public void trigger() {
        if (!isOn()) {
            System.out.println(getName() + " is OFF and cannot be triggered.");
            return;
        }

        if (isArmed && !isTriggered) {
            this.isTriggered = true;
            System.out.println("🚨 ALARM TRIGGERED! " + getName() + " in " + getRoomName());
            System.out.println("🚨 Security breach detected! Please enter PIN to disarm.");
        }
    }

    public boolean isArmed() {
        return isArmed;
    }

    public boolean isTriggered() {
        return isTriggered;
    }

    public String getAlarmMode() {
        return alarmMode;
    }

    @Override
    public void turnOn() {
        setOn(true);
        System.out.println(getName() + " system activated (disarmed)");
    }

    @Override
    public void turnOff() {
        if (isArmed) {
            System.out.println("⚠ Cannot turn off " + getName() + " while armed. Disarm first.");
            return;
        }
        setOn(false);
        alarmMode = "DISARMED";
        isTriggered = false;
        System.out.println(getName() + " system deactivated");
    }

    @Override
    public String getStatus() {
        if (isOn()) {
            String status = isTriggered ? "🚨 TRIGGERED!" : (isArmed ? "Armed" : "Disarmed");
            return getName() + " [SmartAlarm] is ON - Status: " + status +
                    " - Mode: " + alarmMode +
                    " - Energy: " + String.format("%.2f", getEnergyConsumption()) + "W" +
                    " - Room: " + getRoomName();
        } else {
            return getName() + " [SmartAlarm] is OFF - Room: " + getRoomName();
        }
    }

    @Override
    public double getEnergyConsumption() {
        if (!isOn()) return 0.0;

        // Triggered alarm uses more power (sirens, lights, etc.)
        return isTriggered ? ENERGY_CONSUMPTION * 10 : ENERGY_CONSUMPTION;
    }

    @Override
    public void schedule(String timeExpression, Runnable task) {
        System.out.println(getName() + " scheduled action at " + timeExpression);
        // Could be used to arm/disarm at specific times
        task.run();
    }
}