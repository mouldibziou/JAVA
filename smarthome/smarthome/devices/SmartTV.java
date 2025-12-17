package smarthome.devices;

public class SmartTV extends SmartDevice {

    private int volume; // 0-100
    private int channel;
    private String currentApp; // Netflix, YouTube, etc.
    private boolean isMuted;
    private static final double BASE_ENERGY_CONSUMPTION = 80.0; // watts

    public SmartTV(String name) {
        super(name, "SmartTV");
        this.volume = 30;
        this.channel = 1;
        this.currentApp = "None";
        this.isMuted = false;
    }

    // Volume control
    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be between 0 and 100.");
        }
        this.volume = volume;
        this.isMuted = false;
        System.out.println(getName() + " volume set to " + volume);
    }

    public void volumeUp() {
        if (volume < 100) {
            volume++;
            isMuted = false;
            System.out.println(getName() + " volume: " + volume);
        }
    }

    public void volumeDown() {
        if (volume > 0) {
            volume--;
            System.out.println(getName() + " volume: " + volume);
        }
    }

    public void mute() {
        isMuted = true;
        System.out.println(getName() + " muted");
    }

    public void unmute() {
        isMuted = false;
        System.out.println(getName() + " unmuted");
    }

    public boolean isMuted() {
        return isMuted;
    }

    // Channel control
    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) throws smarthome.exceptions.InvalidDeviceOperationException {
        if (!isOn()) {
            throw new smarthome.exceptions.InvalidDeviceOperationException("Cannot set channel while TV is OFF.");
        }
        if (channel < 1 || channel > 999) {
            throw new IllegalArgumentException("Channel must be between 1 and 999.");
        }
        this.channel = channel;
        System.out.println(getName() + " changed to channel " + channel);
    }

    public void channelUp() {
        try {
            if (channel < 999) {
                setChannel(channel + 1);
            }
        } catch (smarthome.exceptions.InvalidDeviceOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void channelDown() {
        try {
            if (channel > 1) {
                setChannel(channel - 1);
            }
        } catch (smarthome.exceptions.InvalidDeviceOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // App control
    public String getCurrentApp() {
        return currentApp;
    }

    public void launchApp(String appName) {
        if (appName == null || appName.trim().isEmpty()) {
            throw new IllegalArgumentException("App name cannot be empty.");
        }

        if (!isOn()) {
            turnOn();
        }

        this.currentApp = appName;
        System.out.println(getName() + " launched app: " + appName);
    }

    public void closeApp() {
        this.currentApp = "None";
        System.out.println(getName() + " closed current app");
    }

    @Override
    public void turnOn() {
        setOn(true);
        System.out.println(getName() + " turned ON - Channel " + channel);
    }

    @Override
    public void turnOff() {
        setOn(false);
        currentApp = "None";
        System.out.println(getName() + " turned OFF");
    }

    @Override
    public String getStatus() {
        if (isOn()) {
            String muteStatus = isMuted ? " (MUTED)" : "";
            return getName() + " [SmartTV] is ON - Channel: " + channel +
                    " - Volume: " + volume + muteStatus +
                    " - App: " + currentApp +
                    " - Energy: " + String.format("%.2f", getEnergyConsumption()) + "W" +
                    " - Room: " + getRoomName();
        } else {
            return getName() + " [SmartTV] is OFF - Room: " + getRoomName();
        }
    }

    @Override
    public double getEnergyConsumption() {
        if (!isOn())
            return 0.0;

        // Additional energy for streaming apps
        double appEnergy = currentApp.equals("None") ? 0.0 : 20.0;
        return BASE_ENERGY_CONSUMPTION + appEnergy;
    }

    @Override
    public void setEnergyMode(String mode) {
        if (mode == null)
            return;
        System.out.println(getName() + " energy mode set to " + mode + " (No specific logic for TV yet)");
    }
}