package com.kedaikopi.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.kedaikopi.model.User;
import com.kedaikopi.util.ColorScheme;
import com.kedaikopi.ui.panels.DashboardPanel;
import com.kedaikopi.ui.panels.KasirPanel;
import com.kedaikopi.ui.panels.InventarisPanel;
import com.kedaikopi.ui.panels.KategoriPanel;
import com.kedaikopi.ui.panels.UserManagementPanel;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.Image;

/**
 * Main Application Frame - Modern UI with sidebar navigation
 */
public class MainFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);

    private User currentUser;
    private JPanel contentPanel;
    private JPanel currentPanel; // Track current active panel

    // Sidebar buttons
    private JButton btnDashboard;
    private JButton btnKasir;
    private JButton btnInventaris;
    private JButton btnKategori;
    private JButton btnUser;
    private JButton btnLogout;

    // Track login time for session display
    private java.util.Date loginTime;

    public MainFrame(User user) {
        this.currentUser = user;
        this.loginTime = new java.util.Date(); // Record login time
        initComponents();
        setupUI();
        setupListeners();

        logger.info("Main application opened by: {} ({})", user.getUsername(), user.getRole());
    }

    private void initComponents() {
        // Disable default window controls (no minimize/maximize/close buttons)
        setUndecorated(true);

        setTitle("Kedai Kopi Cak Budibud - " + currentUser.getNamaLengkap() + " (" + currentUser.getRole() + ")");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        // Custom title bar (replace default window controls)
        JPanel titleBar = createCustomTitleBar();
        add(titleBar, BorderLayout.NORTH);

        // Sidebar
        JPanel sidebar = createSidebar();

        // Content area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ColorScheme.BG_LIGHT);

        // Add components
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Set size and maximize
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);

        // Show dashboard by default
        showDashboard();
    }

    /**
     * Create custom title bar (display only - no buttons)
     * Logout button already available in sidebar
     */
    private JPanel createCustomTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(78, 52, 46)); // Brown color matching sidebar
        titleBar.setPreferredSize(new Dimension(0, 40));
        titleBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(58, 32, 26)));

        // App name and user info (no buttons - logout available in sidebar)
        JLabel lblAppName = new JLabel(
                "  Kedai Kopi Cak Budibud - " + currentUser.getNamaLengkap() + " (" + currentUser.getRole() + ")");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAppName.setForeground(Color.WHITE);
        titleBar.add(lblAppName, BorderLayout.CENTER);

        // Make title bar draggable (since we removed default title bar)
        makeTitleBarDraggable(titleBar);

        return titleBar;
    }

    /**
     * Make custom title bar draggable so user can move window
     */
    private void makeTitleBarDraggable(JPanel titleBar) {
        final Point[] offset = { null };

        titleBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                offset[0] = e.getPoint();
            }
        });

        titleBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (offset[0] != null) {
                    Point location = getLocation();
                    setLocation(
                            location.x + e.getX() - offset[0].x,
                            location.y + e.getY() - offset[0].y);
                }
            }
        });
    }

    private JPanel createSidebar() {
        // Idea 6: Sidebar with subtle coffee bean pattern
        JPanel sidebar = new JPanel(
                new MigLayout("fillx, insets 0", "[200!]", "[]5[]10[]10[]10[]10[]10[]push[]5[]5[]20")) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Subtle coffee bean pattern
                g2d.setColor(new Color(58, 32, 26, 15)); // Very subtle
                for (int y = 0; y < getHeight(); y += 40) {
                    for (int x = 0; x < getWidth(); x += 40) {
                        g2d.fillOval(x + 5, y + 5, 8, 12); // Small bean shape
                    }
                }
                g2d.dispose();
            }
        };
        sidebar.setBackground(ColorScheme.PRIMARY_DARK);

        // Logo/Header - coffee logo only (no text)
        JPanel logoPanel = new JPanel(new MigLayout("fillx, insets 20", "[center]", "[]"));
        logoPanel.setBackground(ColorScheme.PRIMARY_DARK);

        // Coffee logo icon (bigger for prominence)
        try {
            ImageIcon coffeeIcon = new ImageIcon(getClass().getResource("/icons/coffee_logo.png"));
            Image scaledImage = coffeeIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel lblIcon = new JLabel(new ImageIcon(scaledImage));
            lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
            logoPanel.add(lblIcon, "wrap");
        } catch (Exception e) {
            logger.warn("Could not load coffee logo icon", e);
        }

        sidebar.add(logoPanel, "h 100!, growx, wrap");

        // Menu buttons - text only
        btnDashboard = createMenuButton("Dashboard", null, "dashboard");
        btnKasir = createMenuButton("Kasir", null, "kasir");
        btnInventaris = createMenuButton("Inventaris", null, "inventaris");
        btnKategori = createMenuButton("Kategori", null, "kategori");
        btnUser = createMenuButton("User", null, "user");
        btnLogout = createMenuButton("Logout", null, "logout");
        btnLogout.setBackground(ColorScheme.BUTTON_DANGER);

        // No user info panel (removed as per user request)

        // Add components to sidebar based on role
        sidebar.add(new JSeparator(), "wrap, growx, gaptop 10, gapbottom 10");

        String role = currentUser.getRole();

        // Owner sees everything
        if ("Owner".equals(role)) {
            sidebar.add(btnDashboard, "wrap, growx");
            sidebar.add(btnKasir, "wrap, growx");
            sidebar.add(btnInventaris, "wrap, growx");
            sidebar.add(btnKategori, "wrap, growx");
            sidebar.add(btnUser, "wrap, growx");
        }
        // Kasir sees Dashboard and Kasir
        else if ("Kasir".equals(role)) {
            sidebar.add(btnDashboard, "wrap, growx");
            sidebar.add(btnKasir, "wrap, growx");
        }
        // Stocker sees Dashboard and Inventaris
        else if ("Stocker".equals(role)) {
            sidebar.add(btnDashboard, "wrap, growx");
            sidebar.add(btnInventaris, "wrap, growx");
        }

        // Session Timer Widget (enhanced)
        sidebar.add(createSessionTimer(), "wrap, growx, gaptop 5");

        sidebar.add(btnLogout, "wrap, growx");

        // Idea 9: Coffee Quote at bottom
        sidebar.add(createCoffeeQuote(), "dock south, h 60!");

        return sidebar;
    }

    private JButton createMenuButton(String text, Icon icon, String action) {
        JButton btn = new JButton(text, icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(98, 72, 66));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btn.setActionCommand(action);
        btn.setIconTextGap(10);

        // Idea 7: Micro-animations - hover effect with subtle lift
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!"logout".equals(action)) {
                    btn.setBackground(new Color(118, 92, 86));
                    // Add shadow effect (lift illusion)
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0, 0, 0, 20)), // Shadow
                            BorderFactory.createEmptyBorder(12, 20, 9, 20) // Shift up
                    ));
                } else {
                    btn.setBackground(ColorScheme.BUTTON_DANGER_HOVER);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0, 0, 0, 30)),
                            BorderFactory.createEmptyBorder(12, 20, 9, 20)));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!"logout".equals(action)) {
                    btn.setBackground(new Color(98, 72, 66));
                } else {
                    btn.setBackground(ColorScheme.BUTTON_DANGER);
                }
                // Reset to normal border
                btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
            }
        });

        return btn;
    }

    /**
     * Create user avatar with initials (Option 3)
     */
    private JPanel createUserAvatar() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 10", "[center]", "[]"));
        panel.setBackground(ColorScheme.PRIMARY_DARK);

        // Get user initials (e.g., "Bon Kasir" -> "BK")
        String initials = getInitials(currentUser.getNamaLengkap());

        // Create circular avatar
        JLabel lblAvatar = new JLabel(initials) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw circle background
                g2d.setColor(new Color(255, 193, 7)); // Yellow/gold
                g2d.fillOval(0, 0, 50, 50);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAvatar.setForeground(new Color(78, 52, 46)); // Dark brown
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setVerticalAlignment(SwingConstants.CENTER);
        lblAvatar.setPreferredSize(new Dimension(50, 50));

        panel.add(lblAvatar);
        return panel;
    }

    /**
     * Get initials from full name
     */
    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        // Take first letter of first two words
        return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
    }

    /**
     * Enhanced session timer widget - shows real-time clock
     */
    private JPanel createSessionTimer() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 8", "[center]", "[]3[]3[]"));
        panel.setBackground(new Color(68, 42, 36));
        panel.setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7, 50), 1));

        // Title without icon
        JLabel lblTitle = new JLabel("Session");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(200, 200, 200));

        // Login time info
        JLabel lblLoginTime = new JLabel("Login: " + formatLoginTime());
        lblLoginTime.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblLoginTime.setForeground(new Color(180, 180, 180));

        // Real-time clock (HH:MM:SS format)
        JLabel lblClock = new JLabel(getCurrentTime());
        lblClock.setFont(new Font("Courier New", Font.BOLD, 12));
        lblClock.setForeground(new Color(255, 193, 7));

        panel.add(lblTitle, "wrap");
        panel.add(lblLoginTime, "wrap");
        panel.add(lblClock);

        // Update clock every second (1000ms) for real-time display
        Timer timer = new Timer(1000, e -> lblClock.setText(getCurrentTime()));
        timer.start();

        return panel;
    }

    /**
     * Format login time
     */
    private String formatLoginTime() {
        return new java.text.SimpleDateFormat("HH:mm").format(loginTime);
    }

    /**
     * Get current time in HH:MM:SS format
     */
    private String getCurrentTime() {
        return new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
    }

    /**
     * Idea 8: Create user avatar with role-specific badge
     */
    private JPanel createUserAvatarWithBadge() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 10", "[center][center]", "[]"));
        panel.setBackground(ColorScheme.PRIMARY_DARK);

        // Get user initials
        String initials = getInitials(currentUser.getNamaLengkap());

        // Create circular avatar
        JLabel lblAvatar = new JLabel(initials) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw circle background
                g2d.setColor(new Color(255, 193, 7)); // Yellow/gold
                g2d.fillOval(0, 0, 50, 50);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAvatar.setForeground(new Color(78, 52, 46));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setVerticalAlignment(SwingConstants.CENTER);
        lblAvatar.setPreferredSize(new Dimension(50, 50));

        // Role badge (Idea 8)
        JLabel roleBadge = new JLabel(getRoleBadge());
        roleBadge.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        panel.add(lblAvatar);
        panel.add(roleBadge, "gapleft 5");

        return panel;
    }

    /**
     * Get role-specific badge icon
     */
    private String getRoleBadge() {
        switch (currentUser.getRole()) {
            case "Owner":
                return "👑"; // Crown
            case "Kasir":
                return "💰"; // Money bag
            case "Stocker":
                return "📦"; // Box
            default:
                return "";
        }
    }

    /**
     * Enhanced coffee quote panel - larger text, daily rotation
     */
    private JPanel createCoffeeQuote() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 12", "[center]", "[]"));
        panel.setBackground(new Color(58, 32, 26));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 193, 7, 30)));

        String[] quotes = {
                "\"Life begins after coffee\"",
                "\"But first, coffee\"",
                "\"Coffee: because adulting is hard\"",
                "\"Espresso yourself!\"",
                "\"May your coffee be strong\""
        };

        // Daily rotation - use date as seed for consistent daily quote
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int dayOfYear = cal.get(java.util.Calendar.DAY_OF_YEAR);
        String quote = quotes[dayOfYear % quotes.length];

        // LARGER TEXT for better visibility
        JLabel lblQuote = new JLabel("<html><center>" + quote + "</center></html>");
        lblQuote.setFont(new Font("Segoe UI", Font.ITALIC, 11)); // Increased from 9 to 11
        lblQuote.setForeground(new Color(180, 180, 180));

        panel.add(lblQuote);
        return panel;
    }

    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 15", "[center]", "[]5[]"));
        panel.setBackground(new Color(58, 32, 26));

        JLabel lblName = new JLabel(currentUser.getNamaLengkap());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel(currentUser.getRole());
        lblRole.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblRole.setForeground(new Color(200, 200, 200));

        panel.add(lblName, "wrap");
        panel.add(lblRole, "wrap");

        return panel;
    }

    private void setupUI() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            logger.error("Failed to set FlatLaf theme", e);
        }
    }

    private void setupListeners() {
        // Window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleExit();
            }
        });

        // Menu button listeners with active indicator
        btnDashboard.addActionListener(e -> {
            showDashboard();
            setActiveButton(btnDashboard);
        });
        btnKasir.addActionListener(e -> {
            showKasir();
            setActiveButton(btnKasir);
        });
        btnInventaris.addActionListener(e -> {
            showInventaris();
            setActiveButton(btnInventaris);
        });
        btnKategori.addActionListener(e -> {
            showKategori();
            setActiveButton(btnKategori);
        });
        btnUser.addActionListener(e -> {
            showUserManagement();
            setActiveButton(btnUser);
        });
        btnLogout.addActionListener(e -> handleLogout());
    }

    /**
     * Set active button with yellow left border (Option 5)
     */
    private void setActiveButton(JButton selectedButton) {
        // Reset all buttons to default state
        for (JButton btn : new JButton[] { btnDashboard, btnKasir, btnInventaris, btnKategori, btnUser }) {
            if (btn != null) {
                btn.setBackground(new Color(98, 72, 66));
                btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
            }
        }

        // Highlight selected button with yellow left border
        if (selectedButton != null) {
            selectedButton.setBackground(new Color(118, 92, 86)); // Lighter shade
            selectedButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(255, 193, 7)), // Yellow left
                    BorderFactory.createEmptyBorder(12, 17, 12, 20) // Adjust padding (20-3=17)
            ));
        }
    }

    private void switchPanel(JPanel newPanel) {
        contentPanel.removeAll();

        // No scroll needed - dashboard fits perfectly
        JScrollPane scrollPane = new JScrollPane(newPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
        currentPanel = newPanel;
    }

    private void showDashboard() {
        DashboardPanel dashboard = new DashboardPanel(currentUser);
        switchPanel(dashboard);
    }

    /**
     * Public method to refresh/show dashboard - can be called from panels
     */
    public void refreshDashboard() {
        SwingUtilities.invokeLater(() -> {
            showDashboard();
            logger.info("Dashboard refreshed");
        });
    }

    private void showKasir() {
        KasirPanel kasirPanel = new KasirPanel(currentUser);
        switchPanel(kasirPanel);
    }

    private void showInventaris() {
        InventarisPanel inventarisPanel = new InventarisPanel(currentUser);
        switchPanel(inventarisPanel);
    }

    private void showKategori() {
        KategoriPanel kategoriPanel = new KategoriPanel(currentUser);
        switchPanel(kategoriPanel);
    }

    private void showUserManagement() {
        UserManagementPanel userPanel = new UserManagementPanel(currentUser);
        switchPanel(userPanel);
    }

    private void showComingSoon(String moduleName) {
        JPanel panel = new JPanel(new MigLayout("fill, insets 40", "[center]", "[]20[]"));
        panel.setBackground(ColorScheme.BG_LIGHT);

        JLabel lblTitle = new JLabel(moduleName);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(ColorScheme.PRIMARY_DARK);

        JLabel lblMessage = new JLabel("Modul ini sedang dalam pembangunan");
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblMessage.setForeground(ColorScheme.TEXT_SECONDARY);

        panel.add(lblTitle, "wrap");
        panel.add(lblMessage, "wrap");

        switchPanel(panel);
    }

    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin logout?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            logger.info("User logged out: {}", currentUser.getUsername());
            dispose();

            // Open login form again
            SwingUtilities.invokeLater(() -> {
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
            });
        }
    }

    private void handleExit() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin keluar dari aplikasi?",
                "Konfirmasi Keluar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            logger.info("Application closed by user: {}", currentUser.getUsername());
            System.exit(0);
        }
    }
}
