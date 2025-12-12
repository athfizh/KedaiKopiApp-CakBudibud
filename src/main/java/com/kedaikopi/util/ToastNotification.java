package com.kedaikopi.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Toast Notification System - Enhanced Version
 * Features: Proper queue management, beautiful gradients, smooth animations
 */
public class ToastNotification {

    private static final int TOAST_WIDTH = 350;
    private static final int TOAST_HEIGHT = 80;
    private static final int ANIMATION_DURATION = 300; // ms
    private static final int DISPLAY_DURATION = 3500; // ms
    private static final int MARGIN = 20;
    private static final int SPACING = 12;

    // Toast queue to prevent overlaps
    private static final List<JWindow> activeToasts = new ArrayList<>();

    public enum ToastType {
        SUCCESS(new Color(46, 204, 113), new Color(39, 174, 96), "✓"),
        ERROR(new Color(231, 76, 60), new Color(192, 57, 43), "✗"),
        WARNING(new Color(241, 196, 15), new Color(243, 156, 18), "⚠"),
        INFO(new Color(52, 152, 219), new Color(41, 128, 185), "ℹ");

        private final Color primaryColor;
        private final Color secondaryColor;
        private final String icon;

        ToastType(Color primaryColor, Color secondaryColor, String icon) {
            this.primaryColor = primaryColor;
            this.secondaryColor = secondaryColor;
            this.icon = icon;
        }

        public Color getPrimaryColor() {
            return primaryColor;
        }

        public Color getSecondaryColor() {
            return secondaryColor;
        }

        public String getIcon() {
            return icon;
        }
    }

    /**
     * Show success toast notification
     */
    public static void showSuccess(Component parent, String message) {
        showToast(parent, message, ToastType.SUCCESS);
    }

    /**
     * Show error toast notification
     */
    public static void showError(Component parent, String message) {
        showToast(parent, message, ToastType.ERROR);
    }

    /**
     * Show warning toast notification
     */
    public static void showWarning(Component parent, String message) {
        showToast(parent, message, ToastType.WARNING);
    }

    /**
     * Show info toast notification
     */
    public static void showInfo(Component parent, String message) {
        showToast(parent, message, ToastType.INFO);
    }

    /**
     * Show toast notification with proper queue management
     */
    private static void showToast(Component parent, String message, ToastType type) {
        SwingUtilities.invokeLater(() -> {
            try {
                Window window = SwingUtilities.getWindowAncestor(parent);
                if (window == null) {
                    return;
                }

                // Create toast window
                JWindow toast = createToastWindow(window, message, type);

                // Add to active toasts list
                synchronized (activeToasts) {
                    activeToasts.add(toast);
                }

                // Calculate position based on existing toasts
                repositionAllToasts(window);

                // Show toast
                toast.setVisible(true);

                // Slide in animation
                animateSlideIn(toast, window, () -> {
                    // Auto-dismiss after display duration
                    Timer dismissTimer = new Timer(DISPLAY_DURATION, ev -> {
                        dismissToast(toast, window);
                    });
                    dismissTimer.setRepeats(false);
                    dismissTimer.start();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Reposition all active toasts to prevent overlap (smooth animation)
     */
    private static void repositionAllToasts(Window window) {
        synchronized (activeToasts) {
            int yOffset = MARGIN;
            for (JWindow toast : activeToasts) {
                int xPosition = window.getX() + window.getWidth() - TOAST_WIDTH - MARGIN;
                int yPosition = window.getY() + yOffset;

                // Smooth reposition with 60fps timing
                Timer repositionTimer = new Timer(16, null);
                repositionTimer.addActionListener(new ActionListener() {
                    int currentY = toast.getY();
                    final int targetY = yPosition;
                    int frame = 0;
                    final int totalFrames = 15; // ~240ms duration

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (Math.abs(currentY - targetY) < 2 || frame >= totalFrames) {
                            toast.setLocation(toast.getX(), targetY);
                            repositionTimer.stop();
                        } else {
                            // Ease-out for smooth deceleration
                            frame++;
                            float progress = (float) frame / totalFrames;
                            float eased = 1 - (1 - progress) * (1 - progress);

                            int startY = toast.getY();
                            if (frame == 1)
                                startY = currentY; // Capture start on first frame

                            currentY = (int) (startY + (targetY - startY) * eased);
                            toast.setLocation(toast.getX(), currentY);
                        }
                    }
                });
                repositionTimer.start();

                yOffset += TOAST_HEIGHT + SPACING;
            }
        }
    }

    /**
     * Slide in animation with smooth easing
     */
    private static void animateSlideIn(JWindow toast, Window window, Runnable onComplete) {
        int targetX = window.getX() + window.getWidth() - TOAST_WIDTH - MARGIN;
        toast.setLocation(window.getX() + window.getWidth() + 50, toast.getY()); // Start off-screen

        // 60fps = ~16ms per frame for smooth animation
        Timer slideInTimer = new Timer(16, null);
        slideInTimer.addActionListener(new ActionListener() {
            int currentX = toast.getX();
            int frame = 0;
            final int totalFrames = 20; // ~320ms total duration

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentX <= targetX || frame >= totalFrames) {
                    toast.setLocation(targetX, toast.getY());
                    slideInTimer.stop();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } else {
                    // Ease-out quad for smooth deceleration
                    frame++;
                    float progress = (float) frame / totalFrames;
                    float eased = 1 - (1 - progress) * (1 - progress);

                    int startX = window.getX() + window.getWidth() + 50;
                    currentX = (int) (startX - (startX - targetX) * eased);
                    toast.setLocation(currentX, toast.getY());
                }
            }
        });
        slideInTimer.start();
    }

    /**
     * Create beautifully designed toast window
     */
    private static JWindow createToastWindow(Window owner, String message, ToastType type) {
        JWindow toast = new JWindow(owner);
        toast.setAlwaysOnTop(true);

        // Create custom panel with gradient and rounded corners
        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 20, 20);

                // Gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(255, 255, 255),
                        0, getHeight(), new Color(250, 250, 250));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 20, 20);

                // Colored left accent bar with gradient
                GradientPaint accentGradient = new GradientPaint(
                        0, 0, type.getPrimaryColor(),
                        0, getHeight(), type.getSecondaryColor());
                g2.setPaint(accentGradient);
                g2.fillRoundRect(0, 0, 6, getHeight() - 8, 20, 20);

                // Inner border
                g2.setColor(new Color(0, 0, 0, 10));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 9, getHeight() - 9, 20, 20);

                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        panel.setOpaque(false);

        // Icon with circular background
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Circle background
                GradientPaint circleGradient = new GradientPaint(
                        0, 0, type.getPrimaryColor(),
                        0, getHeight(), type.getSecondaryColor());
                g2.setPaint(circleGradient);
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);

                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(45, 45));
        iconPanel.setOpaque(false);
        iconPanel.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel(type.getIcon());
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        iconLabel.setForeground(Color.WHITE);
        iconPanel.add(iconLabel);

        panel.add(iconPanel, BorderLayout.WEST);

        // Message with better typography and proper width
        JLabel messageLabel = new JLabel("<html><body style='width: 260px'>" + message + "</body></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(45, 45, 45));
        panel.add(messageLabel, BorderLayout.CENTER);

        // Close button
        JLabel closeLabel = new JLabel("×");
        closeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        closeLabel.setForeground(new Color(150, 150, 150));
        closeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dismissToast(toast, owner);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeLabel.setForeground(new Color(100, 100, 100));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeLabel.setForeground(new Color(150, 150, 150));
            }
        });
        panel.add(closeLabel, BorderLayout.EAST);

        toast.add(panel);
        toast.setSize(TOAST_WIDTH, TOAST_HEIGHT);
        toast.setBackground(new Color(0, 0, 0, 0));

        return toast;
    }

    /**
     * Dismiss toast with fade out animation
     */
    private static void dismissToast(JWindow toast, Window window) {
        Timer fadeTimer = new Timer(10, null);
        fadeTimer.addActionListener(new ActionListener() {
            float opacity = 1.0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.08f;
                if (opacity <= 0) {
                    fadeTimer.stop();
                    toast.dispose();

                    // Remove from active toasts and reposition remaining
                    synchronized (activeToasts) {
                        activeToasts.remove(toast);
                        repositionAllToasts(window);
                    }
                } else {
                    toast.setOpacity(Math.max(0, opacity));
                }
            }
        });
        fadeTimer.start();
    }
}
