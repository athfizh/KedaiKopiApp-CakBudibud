package com.kedaikopi.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.kedaikopi.config.DatabaseConfig;
import com.kedaikopi.model.User;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern Login Form - Entry point untuk aplikasi
 * User harus login sebelum mengakses aplikasi utama
 */
public class LoginForm extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(LoginForm.class);

    // UI Components
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private JLabel lblStatus;
    private JCheckBox chkShowPassword;

    // Authenticated user
    private User authenticatedUser;

    public LoginForm() {
        initComponents();
        setupUI();
        setupListeners();
    }

    private void initComponents() {
        // Set title
        setTitle("Login - Kedai Kopi Cak Budibud");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Layout dengan MigLayout
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[grow]"));

        // Main panel dengan dua kolom
        JPanel mainPanel = new JPanel(new MigLayout("fill", "[400!][600!]", "[grow]"));

        // Left Panel - Branding & Info
        JPanel leftPanel = createLeftPanel();

        // Right Panel - Login Form
        JPanel rightPanel = createRightPanel();

        mainPanel.add(leftPanel, "grow");
        mainPanel.add(rightPanel, "grow");

        add(mainPanel, "grow");

        // Set size and center
        setSize(1000, 600);
        setLocationRelativeTo(null);
    }

    /**
     * Left panel - Branding
     */
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 30", "[center]", "push[]15[]15[]15[]push"));
        panel.setBackground(new Color(78, 52, 46)); // Brown coffee color

        // Logo - High quality transparent image
        JLabel lblLogo = new JLabel();
        try {
            ImageIcon logoIcon = new ImageIcon(
                    getClass().getResource("/icons/coffee_logo.png"));
            // Scale to 80x80 for perfect display
            Image scaledImage = logoIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            // Fallback to emoji if image not found
            lblLogo.setText("☕");
            lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
            lblLogo.setForeground(new Color(255, 215, 0)); // Gold color
        }

        // App Title
        JLabel lblTitle = new JLabel("KEDAI KOPI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("CAK BUDIBUD");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        lblSubtitle.setForeground(new Color(255, 255, 255, 180));

        // Tagline
        JLabel lblTagline = new JLabel("<html><center>Sistem Manajemen<br>Kasir & Inventaris</center></html>");
        lblTagline.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblTagline.setForeground(new Color(255, 255, 255, 150));
        lblTagline.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(lblLogo, "wrap");
        panel.add(lblTitle, "wrap");
        panel.add(lblSubtitle, "wrap");
        panel.add(lblTagline, "wrap");
        // Version removed for cleaner look

        return panel;
    }

    /**
     * Right panel - Login Form
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 60", "[400!, center]", "[]20[]10[]20[]10[]15[]15[]10[]"));
        panel.setBackground(Color.WHITE);

        // Welcome text
        JLabel lblWelcome = new JLabel("Selamat Datang");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblWelcome.setForeground(new Color(51, 51, 51));

        JLabel lblInstruction = new JLabel("Silakan login untuk melanjutkan");
        lblInstruction.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInstruction.setForeground(new Color(120, 120, 120));

        // Username field
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUsername.setForeground(new Color(70, 70, 70));

        txtUsername = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        txtUsername.setFont(new Font("Inter", Font.PLAIN, 14));
        txtUsername.setOpaque(false);
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        // Password field
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPassword.setForeground(new Color(70, 70, 70));

        txtPassword = new JPasswordField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        txtPassword.setFont(new Font("Inter", Font.PLAIN, 14));
        txtPassword.setOpaque(false);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        // Show password checkbox
        chkShowPassword = new JCheckBox("Tampilkan password");
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setBackground(Color.WHITE);
        chkShowPassword.setFocusPainted(false);

        // Status label
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(Color.RED);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        // Login button
        btnLogin = new JButton("LOGIN");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(0, 153, 153)); // Teal color
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));

        // Exit button
        btnExit = new JButton("KELUAR");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnExit.setForeground(new Color(100, 100, 100));
        btnExit.setBackground(new Color(240, 240, 240));
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // Add subtle shadow for depth
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0, 0, 0, 10)),
                BorderFactory.createEmptyBorder(60, 50, 60, 50)));

        // Add components
        panel.add(lblWelcome, "wrap");
        panel.add(lblInstruction, "wrap");
        panel.add(lblUsername, "wrap, gaptop 5");
        panel.add(txtUsername, "wrap, growx");
        panel.add(lblPassword, "wrap, gaptop 5");
        panel.add(txtPassword, "wrap, growx");
        panel.add(chkShowPassword, "wrap");
        panel.add(lblStatus, "wrap, growx");
        panel.add(btnLogin, "wrap, growx");
        panel.add(btnExit, "wrap, growx, gaptop 5");
        // Hint removed for cleaner professional look

        return panel;
    }

    /**
     * Setup UI properties
     */
    private void setupUI() {
        // Apply FlatLaf theme
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            logger.error("Failed to set FlatLaf theme", e);
        }

        // Focus on username field
        txtUsername.requestFocusInWindow();

        // Test database connection on startup
        SwingUtilities.invokeLater(this::testDatabaseConnection);
    }

    /**
     * Setup event listeners
     */
    private void setupListeners() {
        // Login button
        btnLogin.addActionListener(e -> performLogin());

        // Exit button
        btnExit.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Apakah Anda yakin ingin keluar?",
                    "Konfirmasi Keluar",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        // Show/Hide password
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0); // Show password
            } else {
                txtPassword.setEchoChar('•'); // Hide password
            }
        });

        // Enter key on password field
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        // Enter key on username field
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocus();
                }
            }
        });

        // Hover effects
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(0, 133, 133));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(0, 153, 153));
            }
        });
    }

    /**
     * Test database connection
     */
    private void testDatabaseConnection() {
        try {
            boolean connected = DatabaseConfig.getInstance().testConnection();
            if (connected) {
                logger.info("Database connection successful");
            } else {
                showError("Gagal terhubung ke database!");
            }
        } catch (Exception e) {
            logger.error("Database connection error", e);
            showError("Error koneksi database: " + e.getMessage());
        }
    }

    /**
     * Perform login authentication
     */
    private void performLogin() {
        // Clear status
        lblStatus.setText(" ");

        // Validate input
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty()) {
            showError("Username tidak boleh kosong!");
            txtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Password tidak boleh kosong!");
            txtPassword.requestFocus();
            return;
        }

        // Disable button and show loading
        btnLogin.setEnabled(false);
        btnLogin.setText("LOADING...");
        lblStatus.setText("Memverifikasi...");
        lblStatus.setForeground(new Color(0, 153, 153));

        // Authenticate in background thread
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                // Simulate slight delay for UX
                Thread.sleep(500);
                return User.authenticate(username, password);
            }

            @Override
            protected void done() {
                try {
                    authenticatedUser = get();

                    if (authenticatedUser != null) {
                        onLoginSuccess();
                    } else {
                        onLoginFailed();
                    }

                } catch (Exception e) {
                    logger.error("Login error", e);
                    showError("Error: " + e.getMessage());
                    btnLogin.setEnabled(true);
                    btnLogin.setText("LOGIN");
                }
            }
        };

        worker.execute();
    }

    /**
     * Handle successful login
     */
    private void onLoginSuccess() {
        logger.info("Login successful for user: {} ({})", authenticatedUser.getUsername(), authenticatedUser.getRole());

        // Show success message
        lblStatus.setText("Login berhasil!");
        lblStatus.setForeground(new Color(0, 153, 0));

        // Brief delay before opening main app
        Timer timer = new Timer(800, e -> {
            // Close login form
            dispose();

            // Open main application
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame(authenticatedUser);
                mainFrame.setVisible(true);
            });
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Handle failed login
     */
    private void onLoginFailed() {
        showError("Username atau password salah!");
        btnLogin.setEnabled(true);
        btnLogin.setText("LOGIN");
        txtPassword.setText("");
        txtPassword.requestFocus();
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        lblStatus.setText(message);
        lblStatus.setForeground(Color.RED);
    }

    /**
     * Main method - Entry point
     */
    public static void main(String[] args) {
        // Set system properties
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Apply FlatLaf theme globally
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            // Use System.err for static context (logger not available yet)
            System.err.println("Failed to set FlatLaf theme: " + e.getMessage());
        }

        // Launch login form
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
}
