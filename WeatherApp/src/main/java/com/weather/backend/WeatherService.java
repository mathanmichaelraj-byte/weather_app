package com.weather.backend;

import io.github.cdimascio.dotenv.Dotenv;
import theleo.jstruct.Struct;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {

    @Struct
    public static class Weather {
        public String city;
        public double temperature;
        public String description;  // e.g., "Clear", "Clouds"
        public String icon;         // e.g., "01d", "02n"
    }

    public static Weather fetchWeather(String city) throws Exception {

        // Load API key from .env
        Dotenv dotenv = Dotenv.load();
        String _apiKey = dotenv.get("OPENWEATHER_API_KEY");

        // Build URL
        String _baseUrl = "https://api.openweathermap.org/data/2.5/weather";
        String urlStr = _baseUrl + "?q=" + city + "&appid=" + _apiKey + "&units=metric";

        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        // Read response
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            json.append(line);
        }
        br.close();

        // Parse JSON response
        JSONObject obj = new JSONObject(json.toString());
        JSONObject main = obj.getJSONObject("main");
        JSONObject weather0 = obj.getJSONArray("weather").getJSONObject(0);

        Weather w = new Weather();
        w.city = city;
        w.temperature = main.getDouble("temp");
        w.description = weather0.getString("main"); // e.g., "Clouds", "Clear"
        w.icon = weather0.getString("icon");        // e.g., "01d"

        return w;
    }
}
