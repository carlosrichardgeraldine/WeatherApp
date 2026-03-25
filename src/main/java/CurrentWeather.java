import java.time.Instant;

public class CurrentWeather {
    private final double temperature;
    private final int humidity;
    private final double windSpeed;
    private final String description;
    private final String iconCode;

    public CurrentWeather(double temperature, int humidity, double windSpeed,
                          String description, String iconCode) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.description = description;
        this.iconCode = iconCode;
    }

    public double getTemperature() { return temperature; }
    public int getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
    public String getDescription() { return description; }
    public String getIconCode() { return iconCode; }

}
