package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Selection {
    private static JLabel dateLabel;
    private static final String API_URL = "http://worldtimeapi.org/api/timezone/UTC";
    private static final int TIMEOUT_MS = 3000; // 3 second timeout
    private static boolean useLocalFallback = false;

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Game Selection");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 350);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(12, 35, 63));

        // Load custom font
        Font customFont = loadCustomFont("/Main/resources/fonts/PixeloidSans-Bold.ttf", 28f);
        Font buttonFont = loadCustomFont("/Main/resources/fonts/PixeloidSans.ttf", 20f);
        Font dateFont = loadCustomFont("/Main/resources/fonts/PixeloidSans.ttf", 16f);

        // Create a label with the custom font
        JLabel label = new JLabel("Choose a Game to Play", SwingConstants.CENTER);
        label.setFont(customFont != null ? customFont : new Font("SansSerif", Font.BOLD, 28));
        label.setForeground(Color.cyan);
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Create date label
        dateLabel = new JLabel("Loading time...", SwingConstants.CENTER);
        dateLabel.setFont(dateFont != null ? dateFont : new Font("SansSerif", Font.PLAIN, 16));
        dateLabel.setForeground(Color.WHITE);
        
        // Panel for title and date
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(12, 35, 63));
        headerPanel.add(label, BorderLayout.CENTER);
        headerPanel.add(dateLabel, BorderLayout.SOUTH);
        
        frame.add(headerPanel, BorderLayout.NORTH);

        // Create buttons for Chess and Tetris
        JButton chessButton = new JButton("Play Chess");
        JButton tetrisButton = new JButton("Play Tetris");

        // Style buttons with the custom font
        styleButton(chessButton, buttonFont);
        styleButton(tetrisButton, buttonFont);

        // Add action listeners to the buttons
        chessButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                try {
                    Chess.main.Main.main(new String[0]);
                } catch (Exception ex) {
                    showError(frame, "Error launching Chess: " + ex.getMessage());
                }
            }
        });

        tetrisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                try {
                    Tetris.main.Main.main(new String[0]);
                } catch (Exception ex) {
                    showError(frame, "Error launching Tetris: " + ex.getMessage());
                }
            }
        });

        // Create a panel to hold the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(12, 35, 63));
        buttonPanel.setLayout(new GridLayout(1, 2, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        buttonPanel.add(chessButton);
        buttonPanel.add(tetrisButton);

        frame.add(buttonPanel, BorderLayout.CENTER);

        // Start the time update thread
        updateTime();

        // Display the frame
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void updateTime() {
        Thread timeThread = new Thread(() -> {
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss z");
            while (true) {
                try {
                    if (!useLocalFallback) {
                        String apiTime = fetchTimeFromAPI();
                        if (apiTime != null) {
                            SwingUtilities.invokeLater(() -> dateLabel.setText(apiTime));
                        } else {
                            useLocalFallback = true;
                        }
                    }
                    
                    if (useLocalFallback) {
                        // Fallback to local time if API fails
                        String localTime = outputFormat.format(new Date());
                        SwingUtilities.invokeLater(() -> dateLabel.setText(localTime + " (Local)"));
                    }
                    
                    Thread.sleep(1000); // Update every second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();
    }

    private static String fetchTimeFromAPI() {
        HttpURLConnection conn = null;
        try {
            @SuppressWarnings("deprecation")
            URL url = new URL(API_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            
            if (conn.getResponseCode() != 200) {
                return null;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse datetime from JSON response
            String jsonResponse = response.toString();
            int startIndex = jsonResponse.indexOf("\"datetime\":\"");
            if (startIndex == -1) return null;
            
            startIndex += 12; // Length of "datetime":"
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            if (endIndex == -1) return null;
            
            String dateTime = jsonResponse.substring(startIndex, endIndex);
            
            // Parse and format the time
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss z");
            Date date = inputFormat.parse(dateTime);
            
            return outputFormat.format(date);

        } catch (Exception e) {
            System.err.println("API Error: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static Font loadCustomFont(String resourcePath, float size) {
        try (InputStream fontStream = Selection.class.getResourceAsStream(resourcePath)) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(size);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
            return font;
        } catch (Exception e) {
            System.err.println("Error loading font: " + e.getMessage());
            return null;
        }
    }

    private static void styleButton(JButton button, Font font) {
        button.setFont(font != null ? font : new Font("SansSerif", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(13, 29, 64));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.cyan, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
    }

    private static void showError(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}