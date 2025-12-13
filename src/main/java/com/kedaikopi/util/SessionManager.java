package com.kedaikopi.util;

import com.kedaikopi.model.User;
import com.kedaikopi.model.UserActivityLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Session Manager - Auto logout after inactivity
 * Singleton pattern
 */
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static SessionManager instance;

    private Timer inactivityTimer;
    private Timer warningTimer;
    private User currentUser;
    private Window mainWindow;
    private int timeoutMinutes = 30; // Default 30 minutes
    private int warningMinutes = 25; // Show warning at 25 minutes
    private JDialog warningDialog;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Start monitoring user activity
     */
    public void startMonitoring(User user, Window window) {
        this.currentUser = user;
        this.mainWindow = window;

        // Load timeout setting from database (if exists)
        loadTimeoutSetting();

        // Setup inactivity listener
        setupInactivityListener();

        // Start timers
        startTimers();

        logger.info("Session monitoring started for user: {}", user.getUsername());
    }

    /**
     * Stop monitoring (on logout)
     */
    public void stopMonitoring() {
        if (inactivityTimer != null) {
            inactivityTimer.stop();
        }
        if (warningTimer != null) {
            warningTimer.stop();
        }
        if (warningDialog != null) {
            warningDialog.dispose();
        }
        logger.info("Session monitoring stopped");
    }

    /**
     * Load timeout setting from database
     */
    private void loadTimeoutSetting() {
        try {
            String timeoutStr = AppSettings.getSetting("session_timeout_minutes", "30");
            timeoutMinutes = Integer.parseInt(timeoutStr);
            warningMinutes = timeoutMinutes - 5; // Warning 5 min before timeout

            logger.info("Session timeout configured: {} minutes", timeoutMinutes);
        } catch (Exception e) {
            logger.warn("Could not load timeout setting, using default: {} min", timeoutMinutes);
        }
    }

    /**
     * Setup AWT event listener for activity detection
     */
    private void setupInactivityListener() {
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                // Detect mouse and keyboard activity
                if (event instanceof MouseEvent || event instanceof KeyEvent) {
                    resetTimers();
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    /**
     * Start inactivity timers
     */
    private void startTimers() {
        // Warning timer (shows dialog before timeout)
        int warningDelay = warningMinutes * 60 * 1000; // Convert to milliseconds
        warningTimer = new Timer(warningDelay, e -> showWarningDialog());
        warningTimer.setRepeats(false);
        warningTimer.start();

        // Timeout timer (force logout)
        int timeoutDelay = timeoutMinutes * 60 * 1000;
        inactivityTimer = new Timer(timeoutDelay, e -> performAutoLogout());
        inactivityTimer.setRepeats(false);
        inactivityTimer.start();
    }

    /**
     * Reset timers on user activity
     */
    private void resetTimers() {
        if (warningDialog != null && warningDialog.isVisible()) {
            warningDialog.dispose();
        }

        if (warningTimer != null) {
            warningTimer.restart();
        }
        if (inactivityTimer != null) {
            inactivityTimer.restart();
        }
    }

    /**
     * Show warning dialog before auto-logout
     */
    private void showWarningDialog() {
        if (warningDialog != null && warningDialog.isVisible()) {
            return; // Already showing
        }

        SwingUtilities.invokeLater(() -> {
            warningDialog = new JDialog((Frame) mainWindow, "Peringatan Sesi", true);
            warningDialog.setLayout(new BorderLayout(10, 10));
            warningDialog.setSize(400, 180);
            warningDialog.setLocationRelativeTo(mainWindow);

            // Message panel
            JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
            messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
            messagePanel.setBackground(Color.WHITE);

            JLabel iconLabel = new JLabel("⚠");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            iconLabel.setForeground(new Color(255, 152, 0));

            JLabel messageLabel = new JLabel(
                    "<html><center><b>Anda akan logout otomatis dalam 5 menit!</b><br>" +
                            "Tidak ada aktivitas terdeteksi.<br>" +
                            "Klik 'Tetap Login' untuk melanjutkan sesi.</center></html>");
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            messagePanel.add(iconLabel, BorderLayout.WEST);
            messagePanel.add(messageLabel, BorderLayout.CENTER);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            buttonPanel.setBackground(Color.WHITE);

            JButton btnStayLoggedIn = new JButton("Tetap Login");
            btnStayLoggedIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnStayLoggedIn.setBackground(new Color(76, 175, 80));
            btnStayLoggedIn.setForeground(Color.WHITE);
            btnStayLoggedIn.setFocusPainted(false);
            btnStayLoggedIn.setBorderPainted(false);
            btnStayLoggedIn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnStayLoggedIn.addActionListener(e -> {
                resetTimers();
                warningDialog.dispose();
                logger.info("User chose to stay logged in");
            });

            JButton btnLogoutNow = new JButton("Logout Sekarang");
            btnLogoutNow.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btnLogoutNow.setBackground(new Color(240, 240, 240));
            btnLogoutNow.setForeground(Color.DARK_GRAY);
            btnLogoutNow.setFocusPainted(false);
            btnLogoutNow.setBorderPainted(false);
            btnLogoutNow.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnLogoutNow.addActionListener(e -> {
                warningDialog.dispose();
                performAutoLogout();
            });

            buttonPanel.add(btnStayLoggedIn);
            buttonPanel.add(btnLogoutNow);

            warningDialog.add(messagePanel, BorderLayout.CENTER);
            warningDialog.add(buttonPanel, BorderLayout.SOUTH);
            warningDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            // Auto-close warning dialog if main window is not visible
            Timer checkTimer = new Timer(1000, e -> {
                if (!mainWindow.isVisible()) {
                    warningDialog.dispose();
                    ((Timer) e.getSource()).stop();
                }
            });
            checkTimer.start();

            warningDialog.setVisible(true);
        });
    }

    /**
     * Perform auto-logout
     */
    private void performAutoLogout() {
        SwingUtilities.invokeLater(() -> {
            logger.info("Auto-logout triggered for user: {}", currentUser.getUsername());

            // Log logout event
            UserActivityLog.logLogout(currentUser.getIdUser(), "Session timeout");

            // Close warning dialog if open
            if (warningDialog != null && warningDialog.isVisible()) {
                warningDialog.dispose();
            }

            // Show timeout message
            JOptionPane.showMessageDialog(
                    mainWindow,
                    "Sesi Anda telah berakhir karena tidak aktif.\nSilakan login kembali.",
                    "Sesi Berakhir",
                    JOptionPane.INFORMATION_MESSAGE);

            // Close main window
            mainWindow.dispose();

            // Open login form
            SwingUtilities.invokeLater(() -> {
                com.kedaikopi.ui.LoginForm loginForm = new com.kedaikopi.ui.LoginForm();
                loginForm.setVisible(true);
            });
        });
    }

    /**
     * Update timeout setting
     */
    public void setTimeoutMinutes(int minutes) {
        this.timeoutMinutes = minutes;
        this.warningMinutes = minutes - 5;

        // Restart timers with new settings
        if (inactivityTimer != null && inactivityTimer.isRunning()) {
            stopMonitoring();
            startTimers();
        }

        logger.info("Session timeout updated to {} minutes", minutes);
    }

    public int getTimeoutMinutes() {
        return timeoutMinutes;
    }

    /**
     * Check if monitoring is active
     */
    public boolean isMonitoring() {
        // We consider it monitoring if the inactivity timer is running
        return inactivityTimer != null && inactivityTimer.isRunning();
    }
}
