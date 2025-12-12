package com.kedaikopi.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Centralized icon management - Creates custom icons using Graphics2D
 * Solves emoji display issues by drawing icons programmatically
 */
public class IconManager {

    // Icon sizes
    public static final int SMALL = 16;
    public static final int MEDIUM = 24;
    public static final int LARGE = 32;

    // Colors
    private static final Color ICON_PRIMARY = new Color(78, 52, 46);
    private static final Color ICON_ACCENT = new Color(255, 193, 7);
    private static final Color ICON_SUCCESS = new Color(76, 175, 80);
    private static final Color ICON_DANGER = new Color(244, 67, 54);
    private static final Color ICON_INFO = new Color(33, 150, 243);
    private static final Color ICON_WHITE = Color.WHITE;

    /**
     * Creates a coffee cup icon with improved quality
     */
    public static ImageIcon createCoffeeIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // MAXIMUM HD+ quality rendering hints
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g.setColor(color);
        int margin = size / 8;

        // Cup body - more refined shape
        int cupWidth = size - (margin * 2);
        int cupHeight = (int) (cupWidth * 0.75);
        int arcSize = size / 6;
        g.fillRoundRect(margin, margin + size / 5, cupWidth, cupHeight, arcSize, arcSize);

        // Handle - smoother curve
        g.setStroke(new BasicStroke(size / 10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(size - margin - size / 6, margin + size / 5, size / 3, cupHeight / 2, -90, 180);

        // Steam (3 wavy lines) - more elegant
        g.setStroke(new BasicStroke(size / 14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 3; i++) {
            int x = margin + (i + 1) * cupWidth / 4;
            int steamHeight = margin - 2;
            g.drawLine(x, margin + size / 6, x + 2, steamHeight);
            g.drawLine(x + 2, steamHeight, x, steamHeight - 3);
        }

        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates a dashboard/grid icon with improved quality
     */
    public static ImageIcon createDashboardIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        // HD+ rendering hints
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g.setColor(color);
        int margin = size / 6;
        int gap = size / 12;
        int boxSize = (size - margin * 2 - gap) / 2;
        int arc = size / 8;

        // 2x2 grid with proper spacing
        g.fillRoundRect(margin, margin, boxSize, boxSize, arc, arc);
        g.fillRoundRect(margin + boxSize + gap, margin, boxSize, boxSize, arc, arc);
        g.fillRoundRect(margin, margin + boxSize + gap, boxSize, boxSize, arc, arc);
        g.fillRoundRect(margin + boxSize + gap, margin + boxSize + gap, boxSize, boxSize, arc, arc);

        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates a dollar/money icon for Kasir with improved quality
     */
    public static ImageIcon createMoneyIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g.setColor(color);
        int margin = size / 6;

        // Circle background (coin)
        int circleSize = size - margin * 2;
        g.fillOval(margin, margin, circleSize, circleSize);

        // Dollar sign
        g.setColor(new Color(255, 255, 255, 230));
        g.setFont(new Font("Arial", Font.BOLD, (int) (size * 0.6)));
        FontMetrics fm = g.getFontMetrics();
        String text = "$";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g.drawString(text, (size - textWidth) / 2, (size + textHeight) / 2 - size / 12);

        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates a box/package icon for Inventaris with improved quality
     */
    public static ImageIcon createBoxIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        // HD+ rendering hints
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g.setColor(color);
        int margin = size / 6;

        // Box main body - isometric view
        int[] xPoints = { size / 2, size - margin, size - margin, size / 2, margin, margin };
        int[] yPoints = { margin, size / 3, size - margin, size - margin / 2, size - margin, size / 3 };
        g.fillPolygon(xPoints, yPoints, 6);

        // Top face for depth
        g.setColor(ColorScheme.lighten(color, 20));
        int[] xPoints2 = { size / 2, size - margin, size / 2, margin };
        int[] yPoints2 = { margin, size / 3, size / 3 + margin, size / 3 };
        g.fillPolygon(xPoints2, yPoints2, 4);

        // Right face for more depth
        g.setColor(ColorScheme.darken(color, 15));
        int[] xPoints3 = { size / 2, size - margin, size - margin, size / 2 };
        int[] yPoints3 = { size / 3 + margin, size / 3, size - margin, size - margin / 2 };
        g.fillPolygon(xPoints3, yPoints3, 4);

        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates a tag icon for Kategori
     */
    public static ImageIcon createTagIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);
        int margin = size / 6;

        // Tag shape
        int[] xPoints = { margin, size / 2, size - margin, size - margin, size / 2, margin };
        int[] yPoints = { margin, margin, size / 3, size - margin / 2, size - margin, size / 2 };
        g.fillPolygon(xPoints, yPoints, 6);

        // Hole
        g.setColor(Color.WHITE);
        g.fillOval(margin * 2, margin * 2, size / 6, size / 6);

        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates a user/person icon with improved quality
     */
    public static ImageIcon createUserIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g.setColor(color);
        int margin = size / 6;

        // Head - more proportional
        int headSize = (int) (size * 0.35);
        int headX = size / 2 - headSize / 2;
        int headY = margin;
        g.fillOval(headX, headY, headSize, headSize);

        // Body - better proportions
        int bodyWidth = (int) (size * 0.65);
        int bodyHeight = (int) (size * 0.5);
        int bodyX = size / 2 - bodyWidth / 2;
        int bodyY = headY + headSize - margin / 2;
        g.fillRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, size / 5, size / 5);

        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates a logout/exit icon
     */
    public static ImageIcon createLogoutIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);
        int margin = size / 6;

        // Door frame
        g.setStroke(new BasicStroke(size / 12f));
        g.drawRoundRect(margin, margin, size / 2, size - margin * 2, size / 10, size / 10);

        // Arrow pointing right (exit)
        int arrowX = size - margin * 2;
        int arrowY = size / 2;
        g.setStroke(new BasicStroke(size / 10f));
        g.drawLine(size / 2, arrowY, arrowX, arrowY);

        // Arrow head
        int[] xPoints = { arrowX, arrowX - size / 6, arrowX - size / 6 };
        int[] yPoints = { arrowY, arrowY - size / 8, arrowY + size / 8 };
        g.fillPolygon(xPoints, yPoints, 3);

        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates a plus/add icon
     */
    public static ImageIcon createAddIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);
        g.setStroke(new BasicStroke(size / 8f));

        int margin = size / 4;
        // Horizontal line
        g.drawLine(margin, size / 2, size - margin, size / 2);
        // Vertical line
        g.drawLine(size / 2, margin, size / 2, size - margin);

        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates an edit/pencil icon
     */
    public static ImageIcon createEditIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);
        int margin = size / 6;

        // Pencil body
        int[] xPoints = { size / 2, size - margin, size - margin, size / 2 };
        int[] yPoints = { margin, size / 3, size - margin, size / 2 };
        g.fillPolygon(xPoints, yPoints, 4);

        // Pencil tip
        int[] xPoints2 = { margin, size / 2, size / 2 };
        int[] yPoints2 = { size - margin, size / 2, size / 2 + margin };
        g.fillPolygon(xPoints2, yPoints2, 3);

        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates a delete/trash icon
     */
    public static ImageIcon createDeleteIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);
        int margin = size / 5;

        // Lid
        g.fillRoundRect(margin, margin, size - margin * 2, size / 8, size / 12, size / 12);

        // Can body
        int bodyTop = margin + size / 6;
        int bodyWidth = size - margin * 2;
        int bodyHeight = size - margin - bodyTop;
        g.fillRoundRect(margin, bodyTop, bodyWidth, bodyHeight, size / 12, size / 12);

        // Lines
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(size / 16f));
        for (int i = 1; i <= 3; i++) {
            int x = margin + i * bodyWidth / 4;
            g.drawLine(x, bodyTop + margin / 2, x, size - margin * 2);
        }

        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates a refresh/reload icon
     */
    public static ImageIcon createRefreshIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);
        g.setStroke(new BasicStroke(size / 10f));

        int margin = size / 6;
        // Circular arrow
        g.drawArc(margin, margin, size - margin * 2, size - margin * 2, 45, 270);

        // Arrowhead
        int arrowSize = size / 6;
        int[] xPoints = { size / 2 + arrowSize, size / 2, size / 2 + arrowSize / 2 };
        int[] yPoints = { margin, margin + arrowSize / 2, margin + arrowSize };
        g.fillPolygon(xPoints, yPoints, 3);

        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates a search/magnifying glass icon
     */
    public static ImageIcon createSearchIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);
        g.setStroke(new BasicStroke(size / 10f));

        int margin = size / 6;
        int circleSize = size / 2;

        // Circle
        g.drawOval(margin, margin, circleSize, circleSize);

        // Handle
        int handleX1 = margin + circleSize - margin / 2;
        int handleY1 = margin + circleSize - margin / 2;
        int handleX2 = size - margin;
        int handleY2 = size - margin;
        g.drawLine(handleX1, handleY1, handleX2, handleY2);

        g.dispose();
        return new ImageIcon(image);
    }

    // Convenience methods with default colors
    public static ImageIcon getCoffeeIcon(int size) {
        return createCoffeeIcon(size, ICON_ACCENT);
    }

    public static ImageIcon getDashboardIcon(int size) {
        return createDashboardIcon(size, ICON_WHITE);
    }

    public static ImageIcon getMoneyIcon(int size) {
        return createMoneyIcon(size, ICON_WHITE);
    }

    public static ImageIcon getBoxIcon(int size) {
        return createBoxIcon(size, ICON_WHITE);
    }

    public static ImageIcon getTagIcon(int size) {
        return createTagIcon(size, ICON_WHITE);
    }

    public static ImageIcon getUserIcon(int size) {
        return createUserIcon(size, ICON_WHITE);
    }

    public static ImageIcon getLogoutIcon(int size) {
        return createLogoutIcon(size, ICON_WHITE);
    }

    public static ImageIcon getAddIcon(int size) {
        return createAddIcon(size, ICON_SUCCESS);
    }

    public static ImageIcon getEditIcon(int size) {
        return createEditIcon(size, ICON_INFO);
    }

    public static ImageIcon getDeleteIcon(int size) {
        return createDeleteIcon(size, ICON_DANGER);
    }

    public static ImageIcon getRefreshIcon(int size) {
        return createRefreshIcon(size, ICON_PRIMARY);
    }

    public static ImageIcon getSearchIcon(int size) {
        return createSearchIcon(size, ICON_PRIMARY);
    }
}
