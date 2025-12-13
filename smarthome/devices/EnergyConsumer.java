package smarthome.devices;


public interface EnergyConsumer {
    double getEnergyConsumption(); // Returns energy in watts
    void setEnergyMode(String mode); // e.g., "ECO", "NORMAL", "HIGH"
}