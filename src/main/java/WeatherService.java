import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WeatherService {

    private static final String API_KEY = "dfa8ac5e7bd643c5a444cf3bbbc4c344";
    private static final String CURRENT_URL =
            "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=%s";
    private static final String FORECAST_URL =
            "https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=%s";

    public CurrentWeather getCurrentWeather(String city, String units) throws IOException {
        String url = String.format(CURRENT_URL, URLEncoder.encode(city, StandardCharsets.UTF_8), API_KEY, units);
        String json = readUrl(url);

        JSONObject obj = new JSONObject(json);

        return parseCurrentWeather(obj);
    }

    public List<ForecastEntry> getForecast(String city, String units) throws IOException {
        String url = String.format(FORECAST_URL, URLEncoder.encode(city, StandardCharsets.UTF_8), API_KEY, units);
        String json = readUrl(url);

        JSONObject obj = new JSONObject(json);
        int timezoneOffset = obj.getJSONObject("city").getInt("timezone");

        return parseForecast(obj, timezoneOffset);
    }

    private String readUrl(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private CurrentWeather parseCurrentWeather(JSONObject obj) {
        JSONObject main = obj.getJSONObject("main");
        JSONObject wind = obj.getJSONObject("wind");
        JSONObject weather0 = obj.getJSONArray("weather").getJSONObject(0);

        double temp = main.getDouble("temp");
        int humidity = main.getInt("humidity");
        double windSpeed = wind.getDouble("speed");
        String description = weather0.getString("description");
        String icon = weather0.getString("icon");

        return new CurrentWeather(temp, humidity, windSpeed, description, icon);
    }

    private List<ForecastEntry> parseForecast(JSONObject obj, int timezoneOffset) {
        JSONArray list = obj.getJSONArray("list");
        List<ForecastEntry> result = new ArrayList<>();

        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.getJSONObject(i);

            long dt = item.getLong("dt");
            JSONObject main = item.getJSONObject("main");
            JSONObject weather0 = item.getJSONArray("weather").getJSONObject(0);

            double temp = main.getDouble("temp");
            String description = weather0.getString("description");

            result.add(new ForecastEntry(
                    Instant.ofEpochSecond(dt),
                    temp,
                    description,
                    timezoneOffset
            ));
        }

        return result;
    }
}
