package com.fincore.ui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Utility class for cross-platform Java Swing GUI styling,
 * High-DPI display scaling, and Windows native Look & Feel.
 */
public final class UiUtil {

    private static Image cachedAppIcon;

    private UiUtil() {
    }

    /**
     * Configures the system Look & Feel, high-DPI scaling, and font rendering.
     * On Windows, selects WindowsLookAndFeel for native title bars and controls.
     */
    public static void applySystemLookAndFeel() {
        try {
            System.setProperty("sun.java2d.uiScale.enabled", "true");
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Generates a sleek, high-resolution application icon for Windows and cross-platform taskbars.
     */
    public static synchronized Image getAppIcon() {
        if (cachedAppIcon != null) {
            return cachedAppIcon;
        }

        int size = 64;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Navy blue rounded shield background
        g2.setColor(new Color(24, 43, 73));
        g2.fillRoundRect(2, 2, size - 4, size - 4, 16, 16);

        // Gold accent border
        g2.setColor(new Color(255, 193, 7));
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawRoundRect(2, 2, size - 4, size - 4, 16, 16);

        // "FC" FinCore text emblem
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        FontMetrics fm = g2.getFontMetrics();
        String text = "FC";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent() - 1;
        g2.drawString(text, x, y);

        g2.dispose();
        cachedAppIcon = img;
        return cachedAppIcon;
    }

    /**
     * Applies the FinCore application icon to any JFrame or JDialog.
     */
    public static void setWindowIcon(Window window) {
        if (window != null) {
            window.setIconImage(getAppIcon());
        }
    }
}
