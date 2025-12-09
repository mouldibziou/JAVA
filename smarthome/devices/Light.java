package smarthome.devices;


/**
 * Smart Light with brightness control
 */
public class Light extends SmartDevice implements Controllable, EnergyConsumer {
    private int brightness; // 0-100
    private String color;
    private String energyMode;

    public Light(String id, String name) {
        super(id, name, "Light");
        this.brightness = 0;
        this.color = "White";
        this.energyMode = "NORMAL";
    }

    @Override
    public void turnOn() {
        isOn = true;
        if (brightness == 0) {
            brightness = 100;
        }
        System.out.println(name + " turned ON with brightness " + brightness + "%");
    }

    @Override
    public void turnOff() {
        isOn = false;
        System.out.println(name + " turned OFF");
    }

    @Override
    public String getStatus() {
        return super.getStatus() + String.format(" | Brightness: %d%% | Color: %s", brightness, color);
    }

    @Override
    public void executeCommand(String command) {
        if (command.startsWith("brightness:")) {
            int value = Integer.parseInt(command.split(":")[1]);
            setBrightness(value);
        } else if (command.startsWith("color:")) {
            setColor(command.split(":")[1]);
        }
    }

    @Override
    public boolean isResponding() {
        return true;
    }

    @Override
    public double getEnergyConsumption() {
        if (!isOn) return 0;
        double baseConsumption = 10; // 10W base
        return baseConsumption * (brightness / 100.0) *
                (energyMode.equals("ECO") ? 0.7 : 1.0);
    }

    @Override
    public void setEnergyMode(String mode) {
        this.energyMode = mode;
        System.out.println(name + " energy mode set to " + mode);
    }

    public void setBrightness(int brightness) {
        if (brightness < 0 || brightness > 100) {
            System.out.println("Invalid brightness. Must be 0-100");
            return;
        }
        this.brightness = brightness;
        if (brightness > 0 && !isOn) {
            turnOn();
        }
        System.out.println(name + " brightness set to " + brightness + "%");
    }

    public void setColor(String color) {
        this.color = color;
        System.out.println(name + " color set to " + color);
    }

    public int getBrightness() { return brightness; }
    public String getColor() { return color; }
}

'''khedmti(tannoubi)'
        package smarthome.devices;

public class Light extends SmartDevice {

    private int brightness; // 0-100
    private static final double ENERGY_PER_BRIGHTNESS = 0.5; // watts per brightness unit

    public Light(String name) {
        super(name, "Light");
        this.brightness = 0;
    }

    public Light(String name, int initialBrightness) {
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