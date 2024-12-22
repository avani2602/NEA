package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;

public class Selection {

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Game Selection");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(12, 35, 63));

        // Load custom font
        Font customFont = loadCustomFont("/Main/resources/fonts/PixeloidSans-Bold.ttf", 28f);
        Font buttonFont = loadCustomFont("/Main/resources/fonts/PixeloidSans.ttf", 20f);

        // Create a label with the custom font
        JLabel label = new JLabel("Choose a Game to Play", SwingConstants.CENTER);
        label.setFont(customFont != null ? customFont : new Font("SansSerif", Font.BOLD, 28));
        label.setForeground(Color.cyan);
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        frame.add(label, BorderLayout.NORTH);

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
                frame.dispose(); // Close the selection screen
                try {
                    // Call the Chess Main class
                    Chess.main.Main.main(new String[0]);
                } catch (Exception ex) {
                    showError(frame, "Error launching Chess: " + ex.getMessage());
                }
            }
        });

        tetrisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Close the selection screen
                try {
                    // Call the Tetris Main class
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

        // Display the frame
        frame.setLocationRelativeTo(null); // Center the frame on the screen
        frame.setVisible(true);
    }

    // Helper method to load a custom font
    private static Font loadCustomFont(String resourcePath, float size) {
        try (InputStream fontStream = Selection.class.getResourceAsStream(resourcePath)) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(size);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font); // Register the font with the system
            return font;
        } catch (Exception e) {
            System.err.println("Error loading font: " + e.getMessage());
            return null;
        }
    }

    // Helper method to style buttons
    private static void styleButton(JButton button, Font font) {
        button.setFont(font != null ? font : new Font("SansSerif", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(13, 29, 64)); // Darker shade of blue
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.cyan, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true); // Ensures the background color is visible
        button.setContentAreaFilled(true); // Ensures the content area is filled with the background color
    }

    // Helper method to display error messages
    private static void showError(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
