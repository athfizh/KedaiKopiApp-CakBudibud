package com.kedaikopi;

import com.formdev.flatlaf.FlatLightLaf;
import com.kedaikopi.ui.LoginForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Main Entry Point - Aplikasi Kedai Kopi
 * 
 * @author H A F I Z H
 * @version 1.0.0
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Set system properties for better rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Apply FlatLaf theme globally
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            logger.info("FlatLaf theme applied successfully");
        } catch (Exception e) {
            logger.error("Failed to set FlatLaf theme", e);

        }

        // Logo aplikasi di console
        printApplicationBanner();

        // Launch application in EDT
        SwingUtilities.invokeLater(() -> {
            try {
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
                logger.info("Application started successfully");
            } catch (Exception e) {
                logger.error("Failed to start application", e);
                JOptionPane.showMessageDialog(
                        null,
                        "Error: " + e.getMessage(),
                        "Application Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    /**
     * Print application banner to console
     */
    private static void printApplicationBanner() {
        System.out.println("===================================================");
        System.out.println("          KEDAI KOPI CAK BUDIBUD - v1.0.0          ");
        System.out.println("        Sistem Kasir & Inventaris Management       ");
        System.out.println("===================================================");
        System.out.println();
    }
}