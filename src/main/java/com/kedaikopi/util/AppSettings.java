package com.kedaikopi.util;

import com.kedaikopi.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Application Settings Utility
 * Read/write app settings from database
 */
public class AppSettings {

    private static final Logger logger = LoggerFactory.getLogger(AppSettings.class);

    /**
     * Get setting value by key
     */
    public static String getSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM tbl_app_settings WHERE setting_key = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("setting_value");
            }

        } catch (SQLException e) {
            logger.error("Error getting setting {}: {}", key, e.getMessage());
        }

        return defaultValue;
    }

    /**
     * Set setting value
     */
    public static boolean setSetting(String key, String value) {
        String sql = "INSERT INTO tbl_app_settings (setting_key, setting_value) " +
                "VALUES (?, ?) " +
                "ON CONFLICT (setting_key) DO UPDATE SET " +
                "setting_value = EXCLUDED.setting_value, " +
                "updated_at = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            stmt.setString(2, value);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error setting {}: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Get integer setting
     */
    public static int getIntSetting(String key, int defaultValue) {
        try {
            String value = getSetting(key, String.valueOf(defaultValue));
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get boolean setting
     */
    public static boolean getBooleanSetting(String key, boolean defaultValue) {
        String value = getSetting(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
}
