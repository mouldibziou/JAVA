package smarthome.automation;

import smarthome.core.CentralController;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeCondition implements Condition {
    private LocalTime targetTime;

    public TimeCondition(LocalTime targetTime) {
        this.targetTime = targetTime;
    }

    public TimeCondition(String timeStr) {
        // Parse HH:mm
        this.targetTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public boolean evaluate(CentralController controller) {
        // In a real system, we'd check if current time matches.
        // For simulation, we check if current time is within this minute
        LocalTime now = LocalTime.now();
        return now.getHour() == targetTime.getHour() && now.getMinute() == targetTime.getMinute();
    }
}
