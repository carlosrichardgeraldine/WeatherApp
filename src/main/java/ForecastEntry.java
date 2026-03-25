import java.time.Instant;

public record ForecastEntry(Instant time, double temperature, String description, int timezoneOffset) {
}