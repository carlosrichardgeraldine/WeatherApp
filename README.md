# 🌤️ Weather Information App

A JavaFX desktop application that displays real-time weather data and forecasts using the OpenWeatherMap API.

---

## Features

- **Current weather** — temperature, conditions, humidity, and wind speed
- **Hourly forecast** — next 8 time slots displayed in a scrollable row
- **Daily forecast** — multi-day forecast in a list view
- **Search history** — sidebar showing past searches with timestamps
- **Unit selection** — toggle between Celsius/Fahrenheit and m/s / km/h
- **Dynamic backgrounds** — gradient changes based on weather condition (rain, clouds, clear) and local time (day/night)
- **Auto-refresh** — weather data refreshes automatically every 2 minutes
- **Manual refresh** — click the animated "refresh" link to update on demand
- **Fade animations** — smooth transitions when loading new weather data

---

## Requirements

- Java 25
- JavaFX 26
- An [OpenWeatherMap API key](https://openweathermap.org/api) (free tier works)

---

## Project Structure

```
WeatherApp/
├── src/
│   ├── WeatherApp.java          # Main JavaFX application & UI
│   ├── WeatherService.java      # API calls to OpenWeatherMap
│   ├── CurrentWeather.java      # Model for current weather data
│   └── ForecastEntry.java       # Model for forecast entries
└── resources/
    └── style.css                # Stylesheet for the UI
```

---

## Setup

1. **Clone the repository**
   ```bash
   git https://github.com/carlosrichardgeraldine/WeatherApp.git
   cd WeatherApp
   ```

2. **Add your API key**  
   In `WeatherService.java`, set your OpenWeatherMap API key:
   ```java
   private static final String API_KEY = "your_api_key_here";
   ```

3. **Build and run**  
   Using your IDE (IntelliJ, Eclipse) or via the command line with the JavaFX SDK on the module path:
   ```bash
   mvn clean javafx:run
   ```

---

## Usage

1. Launch the app — it opens maximized.
2. Type a city name in the **City** field.
3. Select your preferred **temperature unit** (Celsius / Fahrenheit) and **wind unit** (m/s / km/h).
4. Click **Get Weather**.
5. Current conditions, hourly forecast, and daily forecast are displayed.
6. The sidebar tracks your search history with timestamps.
7. The background gradient updates automatically to reflect the weather and local time at the searched city.

---

## API Details

Weather data is fetched from the [OpenWeatherMap API](https://openweathermap.org/api):

| Endpoint | Used for |
|----------|----------|
| `/data/2.5/weather` | Current temperature, condition, humidity, wind, icon |
| `/data/2.5/forecast` | Hourly and daily forecast entries, timezone offset |

Weather icons are loaded from `https://openweathermap.org/img/wn/{iconCode}@2x.png`.

---

## Configuration

| Setting | Default | Options |
|---------|---------|---------|
| Temperature unit | Celsius | Celsius, Fahrenheit |
| Wind speed unit | m/s | m/s, km/h |
| Auto-refresh interval | 2 minutes | (hardcoded in `startAutoRefresh`) |

---

## Known Limitations

- No offline/cached data — requires an active internet connection.
- Auto-refresh only runs while the app is open.
- Search history is not persisted between sessions.

---

## License

GNU General Public License. See `LICENSE` for details.