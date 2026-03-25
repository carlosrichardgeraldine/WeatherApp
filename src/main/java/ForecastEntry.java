import java.time.Instant;

public class ForecastEntry {
    private final Instant time;
    private final double temperature;
    private final String description;
    private final int timezoneOffset;

    public ForecastEntry(Instant time, double temperature, String description, int timezoneOffset) {
        this.time = time;
        this.temperature = temperature;
        this.description = description;
        this.timezoneOffset = timezoneOffset;
    }

    public Instant getTime() {
        return time;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }

    public int getTimezoneOffset() {
        return timezoneOffset;
    }
}