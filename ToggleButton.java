package Tetris.main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;

public class ToggleButton {
    private Rectangle bounds;
    private String text;
    private boolean isToggled;
    private Color defaultColor = new Color(40, 40, 40);
    private Color toggledColor = new Color(0, 150, 200);
    private Color textColor = Color.WHITE;
    private Font font;

    public ToggleButton(int x, int y, int width, int height, String text, Font font) {
        bounds = new Rectangle(x, y, width, height);
        this.text = text;
        this.font = font;
        this.isToggled = false;
    }

    public void draw(Graphics2D g2) {
        // Draw button background
        g2.setColor(isToggled ? toggledColor : defaultColor);
        g2.fill(bounds);

        // Draw button border
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(bounds);

        // Draw button text
        g2.setColor(textColor);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
        int textY = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, textX, textY);
    }

    public boolean contains(int x, int y) {
        return bounds.contains(x, y);
    }

    public void toggle() {
        isToggled = !isToggled;
    }

    public boolean isToggled() {
        return isToggled;
    }
}
