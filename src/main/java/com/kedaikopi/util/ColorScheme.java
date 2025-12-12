package com.kedaikopi.util;

import java.awt.Color;

/**
 * Centralized color scheme for consistent UI theming
 * Coffee shop theme with brown, gold, and modern accents
 */
public class ColorScheme {

    // Primary Colors (Brown Shades - Coffee Theme)
    public static final Color PRIMARY_DARK = new Color(78, 52, 46); // #4E342E - Sidebar background
    public static final Color PRIMARY = new Color(121, 85, 72); // #795548 - Primary buttons
    public static final Color PRIMARY_LIGHT = new Color(161, 136, 127); // #A1887F - Hover states
    public static final Color PRIMARY_LIGHTER = new Color(215, 204, 200); // #D7CCC8 - Borders

    // Accent Colors
    public static final Color ACCENT_GOLD = new Color(255, 193, 7); // #FFC107 - Highlights, coffee
    public static final Color ACCENT_GREEN = new Color(76, 175, 80); // #4CAF50 - Success
    public static final Color ACCENT_RED = new Color(244, 67, 54); // #F44336 - Danger
    public static final Color ACCENT_BLUE = new Color(33, 150, 243); // #2196F3 - Info
    public static final Color ACCENT_ORANGE = new Color(255, 152, 0); // #FF9800 - Warning

    // Neutral Colors
    public static final Color BG_WHITE = Color.WHITE; // #FFFFFF
    public static final Color BG_LIGHT = new Color(250, 250, 250); // #FAFAFA - Light background
    public static final Color BG_GRAY = new Color(245, 245, 245); // #F5F5F5 - Alternate rows
    public static final Color TEXT_PRIMARY = new Color(33, 33, 33); // #212121 - Main text
    public static final Color TEXT_SECONDARY = new Color(117, 117, 117); // #757575 - Secondary text
    public static final Color TEXT_DISABLED = new Color(189, 189, 189); // #BDBDBD - Disabled text
    public static final Color BORDER_COLOR = new Color(224, 224, 224); // #E0E0E0 - Borders
    public static final Color DIVIDER_COLOR = new Color(238, 238, 238); // #EEEEEE - Dividers

    // Status Colors (for stock levels, etc.)
    public static final Color STATUS_NORMAL = ACCENT_GREEN;
    public static final Color STATUS_WARNING = ACCENT_ORANGE;
    public static final Color STATUS_DANGER = ACCENT_RED;
    public static final Color STATUS_INFO = ACCENT_BLUE;

    // Component-specific colors
    public static final Color BUTTON_PRIMARY = PRIMARY;
    public static final Color BUTTON_PRIMARY_HOVER = PRIMARY_DARK;
    public static final Color BUTTON_SUCCESS = ACCENT_GREEN;
    public static final Color BUTTON_SUCCESS_HOVER = new Color(67, 160, 71);
    public static final Color BUTTON_DANGER = ACCENT_RED;
    public static final Color BUTTON_DANGER_HOVER = new Color(229, 57, 53);
    public static final Color BUTTON_SECONDARY = new Color(158, 158, 158); // #9E9E9E
    public static final Color BUTTON_SECONDARY_HOVER = new Color(117, 117, 117);

    // Table colors
    public static final Color TABLE_HEADER_BG = PRIMARY_DARK;
    public static final Color TABLE_HEADER_FG = Color.WHITE;
    public static final Color TABLE_ROW_EVEN = Color.WHITE;
    public static final Color TABLE_ROW_ODD = BG_GRAY;
    public static final Color TABLE_SELECTION_BG = PRIMARY_LIGHTER;
    public static final Color TABLE_SELECTION_FG = TEXT_PRIMARY;
    public static final Color TABLE_GRID = BORDER_COLOR;

    // Card/Panel colors
    public static final Color CARD_BG = Color.WHITE;
    public static final Color CARD_BORDER = BORDER_COLOR;
    public static final Color CARD_SHADOW = new Color(0, 0, 0, 10);

    // Input field colors
    public static final Color INPUT_BG = Color.WHITE;
    public static final Color INPUT_BORDER = BORDER_COLOR;
    public static final Color INPUT_BORDER_FOCUS = PRIMARY;
    public static final Color INPUT_ERROR_BORDER = ACCENT_RED;

    // Shadow colors (with alpha)
    public static final Color SHADOW_LIGHT = new Color(0, 0, 0, 5);
    public static final Color SHADOW_MEDIUM = new Color(0, 0, 0, 15);
    public static final Color SHADOW_DARK = new Color(0, 0, 0, 30);

    /**
     * Get status color based on stock level
     */
    public static Color getStockStatusColor(int stock, int lowThreshold) {
        if (stock == 0) {
            return STATUS_DANGER;
        } else if (stock < lowThreshold) {
            return STATUS_WARNING;
        } else {
            return STATUS_NORMAL;
        }
    }

    /**
     * Lighten a color by a percentage (0-100)
     */
    public static Color lighten(Color color, int percentage) {
        int r = Math.min(255, color.getRed() + (255 - color.getRed()) * percentage / 100);
        int g = Math.min(255, color.getGreen() + (255 - color.getGreen()) * percentage / 100);
        int b = Math.min(255, color.getBlue() + (255 - color.getBlue()) * percentage / 100);
        return new Color(r, g, b, color.getAlpha());
    }

    /**
     * Darken a color by a percentage (0-100)
     */
    public static Color darken(Color color, int percentage) {
        int r = Math.max(0, color.getRed() - color.getRed() * percentage / 100);
        int g = Math.max(0, color.getGreen() - color.getGreen() * percentage / 100);
        int b = Math.max(0, color.getBlue() - color.getBlue() * percentage / 100);
        return new Color(r, g, b, color.getAlpha());
    }

    /**
     * Add alpha transparency to a color
     */
    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    // Chart Colors
    public static final Color CHART_BG = Color.WHITE;
    public static final Color CHART_GRID = new Color(240, 240, 240);
    public static final Color CHART_LABEL = TEXT_PRIMARY;

    // Chart Series Colors (for multiple data series)
    public static final Color[] CHART_COLORS = {
            new Color(66, 133, 244), // Blue
            new Color(52, 168, 83), // Green
            new Color(251, 188, 5), // Yellow/Gold
            new Color(234, 67, 53), // Red
            new Color(156, 39, 176), // Purple
            new Color(255, 109, 0), // Orange
            new Color(0, 172, 193), // Cyan
            new Color(158, 158, 158) // Gray
    };
}
