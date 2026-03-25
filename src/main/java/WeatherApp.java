import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.concurrent.Task;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Objects;

public class WeatherApp extends Application {

    private int lastForecastTimezoneOffset = 0;

    private final WeatherService weatherService = new WeatherService();

    private TextField cityField;
    private ComboBox<String> tempUnitBox;
    private ComboBox<String> windUnitBox;

    private Label cityTimeLabel;
    private Label tempLabel;
    private Label conditionLabel;
    private Label humidityInfo;
    private Label windInfo;
    private Label refreshLabel;

    private String lastSearchedCity = null;

    private ImageView iconView;

    private ListView<String> forecastList;

    private BorderPane root;
    private VBox centerPane;
    private VBox historyPane;

    private final ObservableList<String> historyItems = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setPadding(new Insets(16));

        HBox topPane = buildTopPane();
        centerPane = buildCenterPane();
        historyPane = buildHistoryPane();

        root.setTop(topPane);
        root.setCenter(centerPane);
        root.setRight(historyPane);

        Scene scene = new Scene(root, 900, 500);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

        updateBackground(null);

        stage.setTitle("Weather Information App");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        startAutoRefresh();

        Platform.runLater(() -> {
            updateBackground(null);
            applyTextColor("white");
        });

        playFadeIn(topPane);
        playFadeIn(centerPane);
        playFadeIn(historyPane);
    }

    private HBox buildTopPane() {
        cityField = new TextField();
        cityField.setPromptText("Enter city name");

        tempUnitBox = new ComboBox<>();
        tempUnitBox.getItems().addAll("Celsius", "Fahrenheit");
        tempUnitBox.getSelectionModel().select("Celsius");

        windUnitBox = new ComboBox<>();
        windUnitBox.getItems().addAll("m/s", "km/h");
        windUnitBox.getSelectionModel().select("m/s");

        Button searchBtn = new Button("Get Weather");
        searchBtn.setOnAction(_ -> onSearch());

        HBox box = new HBox(10,
                new Label("City:"), cityField,
                new Label("Temp:"), tempUnitBox,
                new Label("Wind:"), windUnitBox,
                searchBtn
        );

        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12));

        return box;
    }

    private void manualRefresh() {
        if (lastSearchedCity != null) {
            onSearch();  // triggers a normal refresh
        }
    }

    private void startAutoRefresh() {
        Thread autoRefreshThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(120_000); // 2 minutes
                } catch (InterruptedException ignored) {}

                if (lastSearchedCity != null) {
                    Platform.runLater(this::manualRefresh);
                }
            }
        });

        autoRefreshThread.setDaemon(true);
        autoRefreshThread.start();
    }

    private VBox buildCenterPane() {

        // CITY NAME (big)
        cityTimeLabel = new Label("-");
        cityTimeLabel.getStyleClass().add("city-time");

        // LAST UPDATED LABEL
        Label lastUpdatedLabel = new Label("Last updated: --:--");
        lastUpdatedLabel.getStyleClass().add("last-updated");

        // REFRESH LABEL
        refreshLabel = new Label("refresh");
        refreshLabel.getStyleClass().add("refresh-link");
        refreshLabel.setOpacity(0.5);

        // Pulse animation
        FadeTransition pulse = new FadeTransition(Duration.millis(1200), refreshLabel);
        pulse.setFromValue(0.5);
        pulse.setToValue(0.8);
        pulse.setCycleCount(FadeTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Hover effect
        refreshLabel.setOnMouseEntered(_ -> refreshLabel.setOpacity(0.7));
        refreshLabel.setOnMouseExited(_ -> refreshLabel.setOpacity(0.5));
        refreshLabel.setOnMouseClicked(_ -> manualRefresh());

        // SECOND ROW: Last updated + refresh
        HBox updateRow = new HBox(10, lastUpdatedLabel, refreshLabel);
        updateRow.setAlignment(Pos.CENTER_LEFT);

        // FIRST ROW: City name
        HBox cityRow = new HBox(cityTimeLabel);
        cityRow.setAlignment(Pos.CENTER_LEFT);

        // Combined header
        VBox cityHeader = new VBox(4, cityRow, updateRow);
        cityHeader.setAlignment(Pos.CENTER_LEFT);
        cityHeader.setMaxWidth(Double.MAX_VALUE);

        // HERO SECTION
        tempLabel = new Label("-");
        tempLabel.getStyleClass().add("hero-temp");

        conditionLabel = new Label("-");
        conditionLabel.getStyleClass().add("hero-condition");

        Label highLowLabel = new Label("— / —");
        highLowLabel.getStyleClass().add("hero-highlow");

        humidityInfo = new Label("Humidity —%");
        windInfo = new Label("Wind —");

        HBox infoRow = new HBox(20, humidityInfo, windInfo);
        infoRow.setAlignment(Pos.CENTER);
        infoRow.getStyleClass().add("hero-info");

        iconView = new ImageView();
        iconView.setFitWidth(120);
        iconView.setFitHeight(120);

        VBox heroBox = new VBox(6, iconView, tempLabel, conditionLabel, highLowLabel, infoRow);
        heroBox.setAlignment(Pos.CENTER);

        // HOURLY FORECAST
        HBox hourlyRow = new HBox(24);
        hourlyRow.setAlignment(Pos.CENTER);
        hourlyRow.setMaxWidth(Double.MAX_VALUE);

        ScrollPane hourlyScroll = new ScrollPane(hourlyRow);
        hourlyScroll.setFitToWidth(true);
        hourlyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hourlyScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hourlyScroll.getStyleClass().add("hourly-scroll");

        // DAILY FORECAST
        forecastList = new ListView<>();
        forecastList.getStyleClass().add("daily-list");

        // FINAL LAYOUT
        centerPane = new VBox(16, cityHeader, heroBox, hourlyScroll, forecastList);
        VBox.setVgrow(hourlyScroll, Priority.ALWAYS);

        centerPane.setPadding(new Insets(20));

        return centerPane;
    }

    private VBox buildHistoryPane() {
        ListView<String> historyList = new ListView<>();
        historyList.setItems(historyItems);

        historyPane = new VBox(10, new Label("Search History"), historyList);
        historyPane.setPadding(new Insets(20));

        historyPane.prefWidthProperty().bind(root.widthProperty().multiply(0.20));

        historyList.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(historyList, Priority.ALWAYS);

        return historyPane;
    }

    private void onSearch() {
        String city = cityField.getText().trim();
        lastSearchedCity = city;
        if (city.isEmpty()) {
            showError("Please enter a city name.");
            return;
        }

        String units = tempUnitBox.getValue().equals("Celsius") ? "metric" : "imperial";

        Task<Void> task = new Task<>() {
            private CurrentWeather current;
            private List<ForecastEntry> forecast;

            @Override
            protected Void call() throws Exception {
                current = weatherService.getCurrentWeather(city, units);
                forecast = weatherService.getForecast(city, units);
                return null;
            }

            @Override
            protected void succeeded() {
                updateUIWithWeather(city, current, forecast);
            }

            @Override
            protected void failed() {
                showError("Failed to fetch weather: " + getException().getMessage());
            }
        };

        new Thread(task).start();
    }

    private void updateUIWithWeather(String city, CurrentWeather current, List<ForecastEntry> forecast) {
        Platform.runLater(() -> {

            tempLabel.setText(String.format("%.0f°", current.getTemperature()));
            conditionLabel.setText(current.getDescription());

            int offsetSeconds = forecast.getFirst().getTimezoneOffset();
            ZoneId cityZone = ZoneId.ofOffset("UTC", ZoneOffset.ofTotalSeconds(offsetSeconds));
            lastForecastTimezoneOffset = offsetSeconds;

            String localTime = LocalTime.now(cityZone).format(DateTimeFormatter.ofPattern("HH:mm"));
            cityTimeLabel.setText(city + " – " + localTime);

            double maxTemp = forecast.stream()
                    .mapToDouble(ForecastEntry::getTemperature)
                    .max()
                    .orElse(current.getTemperature());

            double minTemp = forecast.stream()
                    .mapToDouble(ForecastEntry::getTemperature)
                    .min()
                    .orElse(current.getTemperature());

            String highLow = String.format("%.0f° / %.0f°", maxTemp, minTemp);
            ((Label)((VBox)centerPane.getChildren().get(1)).getChildren().get(3)).setText(highLow);

            humidityInfo.setText("Humidity " + current.getHumidity() + "%");

            double windSpeed = current.getWindSpeed();
            if (windUnitBox.getValue().equals("km/h")) windSpeed *= 3.6;
            windInfo.setText(String.format("Wind %.1f %s", windSpeed, windUnitBox.getValue()));

            loadIcon(current.getIconCode());

            HBox hourlyRow = (HBox)((ScrollPane)centerPane.getChildren().get(2)).getContent();
            hourlyRow.getChildren().clear();

            DateTimeFormatter hourFmt = DateTimeFormatter.ofPattern("HH:mm");

            for (ForecastEntry fe : forecast.subList(0, Math.min(8, forecast.size()))) {
                VBox hourBox = new VBox(
                        new Label(hourFmt.format(fe.getTime().atZone(cityZone))),
                        new Label(String.format("%.0f°", fe.getTemperature()))
                );
                hourBox.setAlignment(Pos.CENTER);
                hourBox.getStyleClass().add("hour-item");
                hourlyRow.getChildren().add(hourBox);
            }

            forecastList.getItems().clear();
            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE dd");

            for (ForecastEntry fe : forecast) {
                String line = String.format("%s   %.0f°   %s",
                        dayFmt.format(fe.getTime().atZone(cityZone)),
                        fe.getTemperature(),
                        fe.getDescription());
                forecastList.getItems().add(line);
            }

            forecastList.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(forecastList, Priority.NEVER);

            String historyEntry = String.format("%s - %s (%s)",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                    city,
                    current.getDescription());
            historyItems.addFirst(historyEntry);

            playFadeIn(centerPane);
            playFadeIn(historyPane);

            updateBackground(current.getDescription());
        });
    }

    private void loadIcon(String iconCode) {
        String url = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        iconView.setImage(new Image(url, true));
    }

    private boolean isDark(Color c) {
        double brightness = (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
        return brightness < 0.5;
    }

    private void forceTextColor(Parent parent, String color) {
        parent.lookupAll("*").forEach(node -> {
            if (node instanceof Labeled labeled) {
                labeled.setStyle("-fx-text-fill: " + color + ";");
            }
            if (node instanceof TextInputControl input) {
                input.setStyle("-fx-text-fill: " + color + ";");
            }
        });
    }

    private void applyTextColor(String color) {
        forceTextColor(root, color);
    }

    private String extractTopColor(String gradient) {
        int start = gradient.indexOf("#");
        int end = gradient.indexOf(",", start);
        return gradient.substring(start, end).trim();
    }

    private void updateBackground(String condition) {

        ZoneId cityZone;
        if (lastForecastTimezoneOffset != 0) {
            cityZone = ZoneId.ofOffset("UTC", ZoneOffset.ofTotalSeconds(lastForecastTimezoneOffset));
        } else {
            cityZone = ZoneId.systemDefault();
        }

        int localHour = LocalTime.now(cityZone).getHour();
        boolean isNight = (localHour < 6 || localHour >= 18);

        String gradient;

        String cond = (condition == null ? "" : condition.toLowerCase());

        if (cond.contains("rain") || cond.contains("drizzle")) {
            gradient = isNight
                    ? "linear-gradient(to bottom, #1f1c2c, #928dab)"
                    : "linear-gradient(to bottom, #4b79a1, #283e51)";
        }

        else if (cond.contains("cloud")) {
            gradient = isNight
                    ? "linear-gradient(to bottom, #232526, #414345)"
                    : "linear-gradient(to bottom, #8e9eab, #eef2f3)";
        }

        else if (cond.contains("clear")) {
            gradient = isNight
                    ? "linear-gradient(to bottom, #0f2027, #203a43, #2c5364)"
                    : "linear-gradient(to bottom, #2980b9, #6dd5fa)";
        }

        else {
            gradient = isNight
                    ? "linear-gradient(to bottom, #2d3436, #000000)"
                    : "linear-gradient(to bottom, #74b9ff, #a29bfe)";
        }

        root.setStyle("-fx-background-color: " + gradient + ";");

        Color c = Color.web(extractTopColor(gradient));
        String textColor = isDark(c) ? "white" : "black";
        applyTextColor(textColor);
    }

    private void playFadeIn(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(350), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void showError(String msg) {
        Platform.runLater(() ->
                new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait()
        );
    }

}
