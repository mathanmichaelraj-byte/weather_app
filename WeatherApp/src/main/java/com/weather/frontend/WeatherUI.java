package com.weather.frontend;

import com.weather.backend.WeatherService;
import com.weather.backend.WeatherService.Weather;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class WeatherUI {

    private JFrame frame;
    private JTextField cityField;
    private JButton button;
    private JLabel resultLabel;
    private JLabel iconLabel;
    private JLabel temperatureLabel;
    private JLabel descriptionLabel;
    private JPanel weatherPanel;

    public WeatherUI() {
        // Set system look and feel for native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("Weather App");
        frame.setSize(450, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setResizable(false);

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 248, 255)); // Light blue background

        // Top panel for input with improved styling
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setBackground(new Color(240, 248, 255));

        JLabel cityLabel = new JLabel("Enter City:");
        cityLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        topPanel.add(cityLabel);

        cityField = new JTextField(15);
        cityField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cityField.setPreferredSize(new Dimension(200, 30));
        cityField.addActionListener(e -> fetchWeather()); // Allow Enter key
        topPanel.add(cityField);

        button = new JButton("Get Weather");
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(120, 30));
        button.setBackground(new Color(70, 130, 180)); // Steel blue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        topPanel.add(button);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel for weather display
        weatherPanel = new JPanel(new BorderLayout(10, 10));
        weatherPanel.setBackground(Color.WHITE);
        weatherPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Icon on the left
        iconLabel = new JLabel("", JLabel.CENTER);
        iconLabel.setPreferredSize(new Dimension(100, 100));
        weatherPanel.add(iconLabel, BorderLayout.WEST);

        // Weather info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        resultLabel = new JLabel("", JLabel.LEFT);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        resultLabel.setForeground(new Color(50, 50, 50));
        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        temperatureLabel = new JLabel("", JLabel.LEFT);
        temperatureLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        temperatureLabel.setForeground(new Color(70, 130, 180));
        temperatureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        descriptionLabel = new JLabel("", JLabel.LEFT);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descriptionLabel.setForeground(new Color(100, 100, 100));
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(resultLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(temperatureLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(descriptionLabel);

        weatherPanel.add(infoPanel, BorderLayout.CENTER);

        // Initial prompt
        JLabel promptLabel = new JLabel(
                "<html><div style='text-align: center;'>" +
                        "<b>Welcome to Weather App</b><br><br>" +
                        "Enter a city name above and click<br>" +
                        "'Get Weather' to see current conditions" +
                        "</div></html>",
                JLabel.CENTER
        );
        promptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        promptLabel.setForeground(new Color(120, 120, 120));
        weatherPanel.add(promptLabel, BorderLayout.CENTER);

        mainPanel.add(weatherPanel, BorderLayout.CENTER);

        // Button action
        button.addActionListener(e -> fetchWeather());

        frame.add(mainPanel);
        frame.setVisible(true);

        // Focus on text field for immediate typing
        cityField.requestFocusInWindow();
    }

    private void fetchWeather() {
        String city = cityField.getText().trim();

        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Please enter a city name",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Show loading state
        button.setEnabled(false);
        button.setText("Loading...");
        weatherPanel.removeAll();
        JLabel loadingLabel = new JLabel("Fetching weather data...", JLabel.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        weatherPanel.add(loadingLabel, BorderLayout.CENTER);
        weatherPanel.revalidate();
        weatherPanel.repaint();

        // Fetch weather in background thread
        SwingWorker<Weather, Void> worker = new SwingWorker<Weather, Void>() {
            @Override
            protected Weather doInBackground() throws Exception {
                return WeatherService.fetchWeather(city);
            }

            @Override
            protected void done() {
                try {
                    Weather w = get();
                    displayWeather(w);
                } catch (Exception ex) {
                    displayError(ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    button.setEnabled(true);
                    button.setText("Get Weather");
                }
            }
        };
        worker.execute();
    }

    private void displayWeather(Weather w) {
        weatherPanel.removeAll();
        weatherPanel.setLayout(new BorderLayout(10, 10));

        // Update labels
        resultLabel.setText(w.city);
        temperatureLabel.setText(w.temperature + " °C");
        descriptionLabel.setText(
                w.description.substring(0, 1).toUpperCase() +
                        w.description.substring(1)
        );

        // Load and display icon
        try {
            String iconUrl = "https://openweathermap.org/img/wn/" + w.icon + "@2x.png";
            ImageIcon icon = new ImageIcon(new URL(iconUrl));
            iconLabel.setIcon(icon);
        } catch (Exception e) {
            iconLabel.setIcon(null);
        }

        // Add components back
        weatherPanel.add(iconLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        temperatureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(resultLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(temperatureLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(descriptionLabel);

        weatherPanel.add(infoPanel, BorderLayout.CENTER);
        weatherPanel.revalidate();
        weatherPanel.repaint();
    }

    private void displayError(String message) {
        weatherPanel.removeAll();
        JLabel errorLabel = new JLabel(
                "<html><div style='text-align: center;'>" +
                        "<b>Error</b><br><br>" +
                        "Unable to fetch weather data.<br>" +
                        "Please check the city name and try again." +
                        "</div></html>",
                JLabel.CENTER
        );
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorLabel.setForeground(new Color(180, 50, 50));
        weatherPanel.add(errorLabel, BorderLayout.CENTER);
        weatherPanel.revalidate();
        weatherPanel.repaint();

        JOptionPane.showMessageDialog(
                frame,
                "Could not retrieve weather for \"" + cityField.getText() + "\"",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}