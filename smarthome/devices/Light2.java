package smarthome.devices;





public abstract class Light2 extends SmartDevice {

    private int brightness; // 0-100
    private static final double ENERGY_PER_BRIGHTNESS = 0.5; // watts per brightness unit

    public Light2(String name) {
        super(name, "Light");
        this.brightness = 0;
    }

    public Light2(String name, int initialBrightness) {
        super(name, "Light");
        setBrightness(initialBrightness);
    }

    // Brightness management
    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        if (brightness < 0 || brightness > 100) {
            throw new IllegalArgumentException("Brightness must be between 0 and 100.");
        }
        this.brightness = brightness;

        if (brightness > 0 && !isOn()) {
            setOn(true);
            System.out.println(getName() + " turned ON with brightness " + brightness + "%");
        } else if (brightness == 0 && isOn()) {
            setOn(false);
            System.out.println(getName() + " turned OFF (brightness set to 0)");
        } else if (isOn()) {
            System.out.println(getName() + " brightness adjusted to " + brightness + "%");
        }
    }

    public void dim(int amount) {
        int newBrightness = Math.max(0, brightness - amount);
        setBrightness(newBrightness);
    }

    public void brighten(int amount) {
        int newBrightness = Math.min(100, brightness + amount);
        setBrightness(newBrightness);
    }

    @Override
    public void turnOn() {
        if (brightness == 0) {
            setBrightness(50); // Default to 50% brightness
        } else {
            setOn(true);
            System.out.println(getName() + " turned ON at " + brightness + "% brightness");
        }
    }

    @Override
    public void turnOff() {
        setOn(false);
        brightness = 0;
        System.out.println(getName() + " turned OFF");
    }

    @Override
    public String getStatus() {
        if (isOn()) {
            return getName() + " [Light] is ON - Brightness: " + brightness + "% - " +
                    "Energy: " + String.format("%.2f", getEnergyConsumption()) + "W - " +
                    "Room: " + getRoomName();
        } else {
            return getName() + " [Light] is OFF - Room: " + getRoomName();
        }
    }

    @Override
    public double getEnergyConsumption() {
        return isOn() ? brightness * ENERGY_PER_BRIGHTNESS : 0.0;
    }
}