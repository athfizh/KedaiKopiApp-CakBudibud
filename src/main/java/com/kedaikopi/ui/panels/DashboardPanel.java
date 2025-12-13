package com.kedaikopi.ui.panels;

import com.kedaikopi.model.Shift;
import com.kedaikopi.model.User;

import com.kedaikopi.util.ColorScheme;
import com.kedaikopi.util.ChartFactory;
import com.kedaikopi.ui.components.UIComponents;
import com.kedaikopi.ui.dialogs.StockStatusDialog;
import com.kedaikopi.ui.dialogs.MonthlyTransactionDialog;
import com.kedaikopi.ui.dialogs.TransactionLogDialog;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Dashboard Panel - Statistics and overview with charts
 * Different views for different roles
 */
public class DashboardPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(DashboardPanel.class);
    private User currentUser;

    // Stat cards
    private JLabel lblTodaySales, lblTodayTransactions, lblLowStockCount, lblTotalMenuItems, lblTotalStock;

    // New dashboard cards
    private JLabel lblOnlineKasir; // Kasir: Currently online kasir count

    // Auto-refresh timer
    private javax.swing.Timer autoRefreshTimer;

    // Tables
    private JTable tableBestSelling;
    private JTable tableLowStock;

    // Charts
    private ChartPanel salesTrendChart;
    private ChartPanel categoryChart;
    private ChartPanel topProductsChart;

    // Number formatter
    private NumberFormat currencyFormat;

    public DashboardPanel(User user) {
        this.currentUser = user;
        this.currencyFormat = NumberFormat
                .getCurrencyInstance(new Locale.Builder().setLanguage("id").setRegion("ID").build());

        initComponents();
        loadData();
    }

    private void initComponents() {
        // Use BorderLayout to contain the scroll pane
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BG_LIGHT);

        // Content panel - flexible vertical stacking for Kasir/Stocker
        // Equal column widths for centered, evenly-spaced cards
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(
                new MigLayout("fill, insets 10, width 100%", "[grow][grow][grow]", "[][][][grow]"));
        contentPanel.setBackground(ColorScheme.BG_LIGHT);

        // Add profile/shift info card FIRST for Kasir and Stocker (swap with title)
        if (!"Owner".equals(currentUser.getRole())) {
            JPanel shiftInfoCard = createShiftInfoCard();
            contentPanel.add(shiftInfoCard, "span 3, growx, h 70!, wrap 15");
        }

        // Title - AFTER shift card for Kasir/Stocker, or first for Owner
        JLabel title = UIComponents.createLabel("Dashboard", UIComponents.LabelType.HEADING);
        if ("Owner".equals(currentUser.getRole())) {
            contentPanel.add(title, "span 3, wrap");
        } else {
            // For Kasir/Stocker: add larger gap (30px) after title before stat cards
            contentPanel.add(title, "span 3, wrap 30");
        }

        // Create different dashboards based on role
        if ("Owner".equals(currentUser.getRole())) {
            createOwnerDashboard(contentPanel);
        } else if ("Kasir".equals(currentUser.getRole())) {
            createKasirDashboard(contentPanel);
        } else if ("Stocker".equals(currentUser.getRole())) {
            createStockerDashboard(contentPanel);
        }

        // Wrap content in scroll pane with smooth scrolling
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Smooth scrolling configuration
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);

        add(scrollPane, BorderLayout.CENTER);

        // Initial load
        loadData();

        // Start auto-refresh timer (5 minutes) for Owner dashboard only
        if ("Owner".equals(currentUser.getRole())) {
            startAutoRefresh();
        }
    }

    /**
     * Create shift info card for Kasir/Stocker
     * Shows username, role, assigned shift, and real-time status
     */
    private JPanel createShiftInfoCard() {
        JPanel card = new JPanel(new MigLayout("fill, insets 10 15 10 15", "[]push[]", ""));
        card.setBackground(new Color(25, 118, 210)); // Blue background
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(21, 101, 192), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // User info panel (Left)
        JPanel userInfo = new JPanel(new MigLayout("insets 0", "[]", "[]0[]"));
        userInfo.setOpaque(false);

        JLabel lblName = new JLabel(currentUser.getNamaLengkap() + " (" + currentUser.getRole() + ")");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setForeground(Color.WHITE);
        userInfo.add(lblName, "wrap");

        // Shift info
        String shiftText = "Shift: ";
        final Shift[] assignedShift = { null }; // Wrapper for lambda access

        if (currentUser.getAssignedShiftId() != null) {
            assignedShift[0] = currentUser.getAssignedShift();
            if (assignedShift[0] != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                shiftText += assignedShift[0].getShiftName() + " (" +
                        timeFormat.format(assignedShift[0].getStartTime()) + " - " +
                        timeFormat.format(assignedShift[0].getEndTime()) + ")";
            } else {
                shiftText += "Tidak ada shift";
            }
        } else {
            shiftText += "Belum ditentukan";
        }

        JLabel lblShift = new JLabel(shiftText);
        lblShift.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblShift.setForeground(new Color(255, 255, 255, 230));
        userInfo.add(lblShift);

        card.add(userInfo);

        // Status Label (Right)
        JLabel lblStatus = new JLabel("Checking status...");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus.setForeground(Color.WHITE);
        card.add(lblStatus);

        // Timer to update status every second
        javax.swing.Timer statusTimer = new javax.swing.Timer(1000, e -> {
            if (assignedShift[0] == null) {
                lblStatus.setText("Status: -");
                return;
            }

            Calendar now = Calendar.getInstance();
            Calendar shiftStart = Calendar.getInstance();
            shiftStart.setTime(assignedShift[0].getStartTime());

            // Set shift start date to today for comparison
            shiftStart.set(Calendar.YEAR, now.get(Calendar.YEAR));
            shiftStart.set(Calendar.MONTH, now.get(Calendar.MONTH));
            shiftStart.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

            // Calculate difference in minutes
            long diffMillis = now.getTimeInMillis() - shiftStart.getTimeInMillis();
            long diffMinutes = diffMillis / (60 * 1000);

            if (diffMinutes < 0) {
                // Before shift start
                lblStatus.setText("Belum Waktunya (Mulai dalam " + Math.abs(diffMinutes) + " mnt)");
                lblStatus.setForeground(new Color(255, 235, 59)); // Yellow
            } else if (diffMinutes <= 15) {
                // Within 15 tolerance
                lblStatus.setText("Tepat Waktu");
                lblStatus.setForeground(new Color(76, 255, 76)); // Green
            } else {
                // Late
                lblStatus.setText("Terlambat " + diffMinutes + " menit");
                lblStatus.setForeground(new Color(255, 82, 82)); // Red
            }

            // Check shift end warning (e.g. 15 mins before end)
            Calendar shiftEnd = Calendar.getInstance();
            shiftEnd.setTime(assignedShift[0].getEndTime());
            shiftEnd.set(Calendar.YEAR, now.get(Calendar.YEAR));
            shiftEnd.set(Calendar.MONTH, now.get(Calendar.MONTH));
            shiftEnd.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

            long minsUntilEnd = (shiftEnd.getTimeInMillis() - now.getTimeInMillis()) / (60 * 1000);
            if (minsUntilEnd > 0 && minsUntilEnd <= 15) {
                lblStatus.setText(lblStatus.getText() + " | Sesi berakhir dlm " + minsUntilEnd + " mnt");
            }
        });
        statusTimer.start();

        // Ensure timer stops when panel is removed (add HierarchyListener or similar if
        // needed,
        // but for now this is attached to the main dashboard which lives long)

        return card;
    }

    /**
     * Dashboard for Owner - Full statistics with charts
     */
    private void createOwnerDashboard(JPanel panel) {
        // Statistics Cards Row
        JPanel cardSales = createStatCardPanel("Penjualan Hari Ini", "Rp 0",
                ColorScheme.ACCENT_GREEN, null);
        JPanel cardTransactions = createStatCardPanel("Total Transaksi Hari Ini", "0",
                ColorScheme.ACCENT_BLUE, null);

        lblTodaySales = (JLabel) findValueLabel(cardSales);
        lblTodayTransactions = (JLabel) findValueLabel(cardTransactions);

        // Make cardTransactions clickable
        makeCardClickable(cardTransactions);

        panel.add(cardSales, "h 120!");
        panel.add(cardTransactions, "h 120!");

        // Action Buttons Panel - takes remaining space in first row
        JPanel buttonPanel = createActionButtonsPanel();
        panel.add(buttonPanel, "h 120!, wrap");

        // Charts Row - Compact layout with fixed heights
        try (Connection conn = com.kedaikopi.config.DatabaseConfig.getInstance().getConnection()) {

            // Sales Trend Chart - full width
            salesTrendChart = ChartFactory.createSalesTrendChart(conn);
            JPanel salesChartPanel = createCompactChartPanel("Tren Penjualan 7 Hari Terakhir", salesTrendChart);
            panel.add(salesChartPanel, "span 3, growx, h 240!, wrap");

            // Bottom row: Category + Top products (very compact 200px)
            categoryChart = ChartFactory.createCategoryDistributionChart(conn);
            JPanel categoryChartPanel = createCompactChartPanel("Distribusi Penjualan per Kategori", categoryChart);
            panel.add(categoryChartPanel, "growx, h 200!");

            topProductsChart = ChartFactory.createTopProductsChart(conn);
            JPanel topProductsPanel = createCompactChartPanel("Top 10 Menu Terlaris (30 Hari)", topProductsChart);
            panel.add(topProductsPanel, "span 2, growx, h 200!, wrap");

        } catch (SQLException e) {
            logger.error("Error creating dashboard charts", e);
            JLabel errorLabel = new JLabel("Error memuat grafik");
            errorLabel.setForeground(ColorScheme.ACCENT_RED);
            panel.add(errorLabel, "span 3, wrap");
        }
    }

    /**
     * Dashboard for Kasir - Sales summary
     */
    private void createKasirDashboard(JPanel panel) {
        // Three cards: Sales, Transactions, and Online Kasir
        JPanel cardSales = createStatCardPanel("Penjualan Hari Ini", "Rp 0",
                ColorScheme.ACCENT_GREEN, null);
        JPanel cardTransactions = createStatCardPanel("Transaksi Anda", "0",
                ColorScheme.ACCENT_BLUE, null);
        JPanel cardOnlineKasir = createStatCardPanel("Kasir Online", "0",
                new Color(156, 39, 176), null); // Purple

        lblTodaySales = (JLabel) findValueLabel(cardSales);
        lblTodayTransactions = (JLabel) findValueLabel(cardTransactions);
        lblOnlineKasir = (JLabel) findValueLabel(cardOnlineKasir);

        // Make Transaksi Anda card clickable to show transaction details
        makeCardClickable(cardTransactions);

        panel.add(cardSales, "grow, gapright 15");
        panel.add(cardTransactions, "grow, gapright 15");
        panel.add(cardOnlineKasir, "grow, wrap 20");

        // Best selling items - compact table
        JPanel bestSellingPanel = createCompactBestSellingPanel();
        panel.add(bestSellingPanel, "span 3, growx, h 250::380, wrap 10");
    }

    /**
     * Dashboard for Stocker - Stock focus
     */
    private void createStockerDashboard(JPanel panel) {
        // Three cards: Low Stock, Total Items, and Total Stock
        JPanel cardLowStock = createStatCardPanel("Stok Menipis", "0",
                ColorScheme.ACCENT_ORANGE, null);
        JPanel cardTotalItems = createStatCardPanel("Total Item Menu", "0",
                ColorScheme.ACCENT_BLUE, null);
        JPanel cardTotalStock = createStatCardPanel("Total Stok Tersedia", "0 unit",
                new Color(103, 58, 183), null); // Purple-ish color

        lblLowStockCount = (JLabel) findValueLabel(cardLowStock);
        lblTotalMenuItems = (JLabel) findValueLabel(cardTotalItems);
        lblTotalStock = (JLabel) findValueLabel(cardTotalStock);

        panel.add(cardLowStock, "grow, gapright 15");
        panel.add(cardTotalItems, "grow, gapright 15");
        panel.add(cardTotalStock, "grow, wrap 5");

        // Low stock alert table - compact
        JPanel lowStockPanel = createCompactLowStockPanel();
        panel.add(lowStockPanel, "span 3, growx, h 250::380, wrap 10"); // Max height 380px, gap bottom 10px (match
                                                                        // insets)
    }

    /**
     * Create COMPACT best selling panel with category tabs for single-screen
     * dashboard
     */
    private JPanel createCompactBestSellingPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 8", "[grow]", "[]6[grow]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));

        // Header
        JLabel lblTitle = new JLabel("Menu Terlaris");
        lblTitle.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD, 11f));
        lblTitle.setForeground(ColorScheme.ACCENT_BLUE);
        panel.add(lblTitle, "wrap");

        // Create tabbed pane for category filtering
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIComponents.FONT_BODY.deriveFont(10f));
        tabbedPane.setBackground(Color.WHITE);

        // Create "Semua" (All categories) tab
        JPanel allCategoriesPanel = createBestSellingTablePanel(null);
        tabbedPane.addTab("Semua Kategori", allCategoriesPanel);

        // Load categories and create tabs
        try (Connection conn = com.kedaikopi.config.DatabaseConfig.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt
                        .executeQuery("SELECT id_kategori, nama_kategori FROM tbl_kategori ORDER BY nama_kategori")) {

            while (rs.next()) {
                int categoryId = rs.getInt("id_kategori");
                String categoryName = rs.getString("nama_kategori");
                JPanel categoryPanel = createBestSellingTablePanel(categoryId);
                tabbedPane.addTab(categoryName, categoryPanel);
            }
        } catch (Exception e) {
            logger.error("Error loading categories for tabs", e);
        }

        panel.add(tabbedPane, "grow");

        return panel;
    }

    /**
     * Create a table panel for best selling items with optional category filter
     * 
     * @param categoryId Category ID to filter by, or null for all categories
     */
    private JPanel createBestSellingTablePanel(Integer categoryId) {
        JPanel tablePanel = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[grow]"));
        tablePanel.setBackground(Color.WHITE);

        // Table
        String[] columns = { "#", "Menu", "Kategori", "Terjual" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = UIComponents.createStyledTable(model);
        table.setRowHeight(20);
        table.getColumnModel().getColumn(0).setPreferredWidth(25);
        table.getColumnModel().getColumn(3).setPreferredWidth(55);

        // Store the first table as the main reference
        if (categoryId == null && tableBestSelling == null) {
            tableBestSelling = table;
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        tablePanel.add(scrollPane, "grow");

        // Load data for this specific category
        loadBestSellingItemsForCategory(table, categoryId);

        return tablePanel;
    }

    /**
     * Create COMPACT low stock panel for single-screen dashboard
     */
    private JPanel createCompactLowStockPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 8", "[grow]", "[]6[grow]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));

        // Header
        JLabel lblTitle = new JLabel("Stok Menipis");
        lblTitle.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD, 11f));
        lblTitle.setForeground(ColorScheme.ACCENT_ORANGE);
        panel.add(lblTitle, "wrap");

        // Table
        String[] columns = { "Menu", "Kategori", "Stok", "Status" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableLowStock = UIComponents.createStyledTable(model);
        tableLowStock.setRowHeight(20);
        tableLowStock.getColumnModel().getColumn(2).setPreferredWidth(45);
        tableLowStock.getColumnModel().getColumn(3).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(tableLowStock);
        scrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));
        // Removed setPreferredSize to allow growing to fill dashboard blank space
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Always show scrollbar
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        panel.add(scrollPane, "grow");

        return panel;
    }

    /**
     * Create compact chart panel (no duplicate title - chart has its own)
     */
    private JPanel createCompactChartPanel(String title, JPanel chartPanel) {
        JPanel panel = new JPanel(new MigLayout("fill, insets 12", "[grow]", "[grow]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Chart only - title is already in the chart itself
        if (chartPanel != null) {
            chartPanel.setBackground(Color.WHITE);
            chartPanel.setBorder(null);
            panel.add(chartPanel, "grow");
        }

        return panel;
    }

    /**
     * Create statistic card
     */
    private JPanel createStatCardPanel(String title, String value, Color color, Icon icon) {
        return UIComponents.createStatCard(title, value, color, icon);
    }

    /**
     * Create action buttons panel (replaces stock status card)
     */
    private JPanel createActionButtonsPanel() {
        // Check if owner for conditional buttons
        boolean isOwner = "Owner".equals(currentUser.getRole());

        // Vertical stacking - adjust layout based on role
        String layoutConstraint = isOwner ? "[grow][10][grow][10][grow][10][grow][10][grow]" : // 5 buttons for Owner
                "[grow][10][grow][10][grow]"; // 3 buttons for others

        JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[grow]", layoutConstraint));
        panel.setOpaque(false);

        // Button 1: Stock Status Report
        JButton btnStockStatus = new JButton("Cek Status Stok");
        btnStockStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnStockStatus.setBackground(new Color(255, 152, 0));
        btnStockStatus.setForeground(Color.WHITE);
        btnStockStatus.setFocusPainted(false);
        btnStockStatus.setBorderPainted(false);
        btnStockStatus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStockStatus.addActionListener(e -> openStockStatusDialog());
        panel.add(btnStockStatus, "grow, h 45!");

        // Button 2: Monthly Transactions
        JButton btnTransactions = new JButton("Data Transaksi");
        btnTransactions.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTransactions.setBackground(new Color(33, 150, 243));
        btnTransactions.setForeground(Color.WHITE);
        btnTransactions.setFocusPainted(false);
        btnTransactions.setBorderPainted(false);
        btnTransactions.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTransactions.addActionListener(e -> openTransactionDialog());
        panel.add(btnTransactions, "grow, h 45!");

        // Button 3: Activity Log (OWNER ONLY)
        if (isOwner) {
            JButton btnActivityLog = new JButton("Log/Riwayat Tim");
            btnActivityLog.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnActivityLog.setBackground(new Color(156, 39, 176)); // Purple
            btnActivityLog.setForeground(Color.WHITE);
            btnActivityLog.setFocusPainted(false);
            btnActivityLog.setBorderPainted(false);
            btnActivityLog.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnActivityLog.addActionListener(e -> openActivityLogDialog());
            panel.add(btnActivityLog, "grow, h 45!");

            // Button 4: Shift Management (OWNER ONLY)
            JButton btnShift = new JButton("Manajemen Shift");
            btnShift.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnShift.setBackground(new Color(244, 67, 54)); // Red
            btnShift.setForeground(Color.WHITE);
            btnShift.setFocusPainted(false);
            btnShift.setBorderPainted(false);
            btnShift.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnShift.addActionListener(e -> openShiftManagementDialog());
            panel.add(btnShift, "grow, h 45!");
        }

        // Button 5: Refresh Dashboard
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setBackground(new Color(76, 175, 80)); // Green
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setToolTipText("Refresh dashboard data (auto-refresh setiap 5 menit)");
        btnRefresh.addActionListener(e -> {
            btnRefresh.setEnabled(false);
            btnRefresh.setText("Refreshing...");
            refreshDashboardData();
            // Re-enable after 2 seconds
            javax.swing.Timer enableTimer = new javax.swing.Timer(2000, evt -> {
                btnRefresh.setEnabled(true);
                btnRefresh.setText("Refresh");
            });
            enableTimer.setRepeats(false);
            enableTimer.start();
        });
        panel.add(btnRefresh, "grow, h 45!");

        return panel;
    }

    /**
     * Open Stock Status Report Dialog
     */
    private void openStockStatusDialog() {
        SwingUtilities.invokeLater(() -> {
            try {
                StockStatusDialog dialog = new StockStatusDialog((JFrame) SwingUtilities.getWindowAncestor(this));
                dialog.setVisible(true);
            } catch (Exception e) {
                logger.error("Error opening stock status dialog", e);
                JOptionPane.showMessageDialog(this,
                        "Error membuka dialog: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Open Activity Log Dialog (Owner only)
     */
    private void openActivityLogDialog() {
        try {
            com.kedaikopi.ui.dialogs.ActivityLogDialog dialog = new com.kedaikopi.ui.dialogs.ActivityLogDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), currentUser);
            dialog.setVisible(true);
        } catch (Exception e) {
            logger.error("Error opening activity log dialog", e);
            JOptionPane.showMessageDialog(this,
                    "Error membuka dialog: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Open Shift Management Dialog (Owner only)
     */
    private void openShiftManagementDialog() {
        try {
            com.kedaikopi.ui.dialogs.ShiftManagementDialog dialog = new com.kedaikopi.ui.dialogs.ShiftManagementDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), currentUser);
            dialog.setVisible(true);
        } catch (Exception e) {
            logger.error("Error opening shift management dialog", e);
            JOptionPane.showMessageDialog(this,
                    "Error membuka dialog: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Open Monthly Transaction Report Dialog
     */
    private void openTransactionDialog() {
        try {
            MonthlyTransactionDialog dialog = new MonthlyTransactionDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
        } catch (Exception e) {
            logger.error("Error opening transaction dialog", e);
            JOptionPane.showMessageDialog(this,
                    "Error membuka dialog: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Make a card panel clickable with hover effects
     */
    private void makeCardClickable(JPanel card) {
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Store original border
        final javax.swing.border.Border originalBorder = card.getBorder();

        // Add mouse listener for hover and click effects
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Add highlighted border on hover
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ColorScheme.ACCENT_BLUE, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                // Restore original border
                card.setBorder(originalBorder);
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openTransactionLogDialog();
            }
        });

        // Make all child components also trigger the click
        for (Component comp : card.getComponents()) {
            comp.setCursor(new Cursor(Cursor.HAND_CURSOR));
            comp.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    openTransactionLogDialog();
                }
            });
        }
    }

    /**
     * Open Transaction Log Dialog
     */
    private void openTransactionLogDialog() {
        try {
            TransactionLogDialog dialog;

            // For Kasir, show only their transactions from today
            if ("Kasir".equals(currentUser.getRole())) {
                dialog = new TransactionLogDialog(
                        (JFrame) SwingUtilities.getWindowAncestor(this),
                        currentUser); // Pass current user to filter
            } else {
                // For Owner, show all transactions
                dialog = new TransactionLogDialog(
                        (JFrame) SwingUtilities.getWindowAncestor(this),
                        null); // null = no filter
            }

            dialog.setVisible(true);
        } catch (Exception e) {
            logger.error("Error opening transaction log dialog", e);
            JOptionPane.showMessageDialog(this,
                    "Error membuka dialog log transaksi: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Find value label in card (helper method)
     */
    private Component findValueLabel(JPanel card) {
        for (Component comp : card.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getFont().getSize() == 22) { // Value label has size 22 (updated from createStatCard)
                    return label;
                }
            }
        }
        return null;
    }

    /**
     * Load all dashboard data
     */
    public void loadData() {
        loadStatistics();
        loadBestSellingItems();
        loadLowStockItems();
    }

    /**
     * Load statistics for cards
     */
    private void loadStatistics() {
        try (Connection conn = com.kedaikopi.config.DatabaseConfig.getInstance().getConnection()) {

            // Total sales today (including tax)
            if (lblTodaySales != null) {
                // Use grand_total which stores final total (subtotal + tax)
                String salesSql = "SELECT COALESCE(SUM(th.grand_total), 0) as total " +
                        "FROM tbl_transaksi_header th " +
                        "WHERE DATE(th.tanggal) = CURRENT_DATE";

                // For Kasir, filter by their user ID to show only their sales
                if ("Kasir".equals(currentUser.getRole())) {
                    salesSql += " AND th.id_user = " + currentUser.getIdUser();
                }

                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(salesSql)) {
                    if (rs.next()) {
                        double total = rs.getDouble("total");
                        lblTodaySales.setText(currencyFormat.format(total));
                    }
                }
            }

            // Total transactions today
            if (lblTodayTransactions != null) {
                String transSql = "SELECT COUNT(*) as count " +
                        "FROM tbl_transaksi_header " +
                        "WHERE DATE(tanggal) = CURRENT_DATE";

                // For kasir, filter by their user id
                if ("Kasir".equals(currentUser.getRole())) {
                    transSql += " AND id_user = " + currentUser.getIdUser();
                }

                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(transSql)) {
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        lblTodayTransactions.setText(String.valueOf(count));
                    }
                }
            }

            // Low stock count
            if (lblLowStockCount != null) {
                String stockSql = "SELECT COUNT(*) as count FROM tbl_menu " +
                        "WHERE stok < 10 AND is_active = TRUE";
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(stockSql)) {
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        lblLowStockCount.setText(String.valueOf(count));
                    }
                }
            }

            // Total menu items
            if (lblTotalMenuItems != null) {
                String menuSql = "SELECT COUNT(*) as count FROM tbl_menu WHERE is_active = TRUE";
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(menuSql)) {
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        lblTotalMenuItems.setText(String.valueOf(count));
                    }
                }
            }

            // NEW: Online Kasir count (for Kasir dashboard)
            if (lblOnlineKasir != null) {
                try {
                    String onlineSql = "SELECT COUNT(*) as count FROM v_active_kasir";
                    try (Statement stmt = conn.createStatement();
                            ResultSet rs = stmt.executeQuery(onlineSql)) {
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            lblOnlineKasir.setText(String.valueOf(count));
                        }
                    }
                } catch (SQLException e) {
                    // View might not exist yet, set to 0
                    lblOnlineKasir.setText("0");
                    logger.warn("v_active_kasir view not found, session tracking not enabled", e);
                }
            }

            // Total stock (for stocker dashboard)
            if (lblTotalStock != null) {
                String stockSql = "SELECT COALESCE(SUM(stok), 0) as total FROM tbl_menu WHERE is_active = TRUE";
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(stockSql)) {
                    if (rs.next()) {
                        int totalStock = rs.getInt("total");
                        lblTotalStock.setText(totalStock + " unit");
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Error loading statistics", e);
        }
    }

    /**
     * Load best selling items
     */
    private void loadBestSellingItems() {
        if (tableBestSelling == null)
            return;

        loadBestSellingItemsForCategory(tableBestSelling, null);
    }

    /**
     * Load best selling items with optional category filter
     * 
     * @param table      Table to load data into
     * @param categoryId Category ID to filter by, or null for all categories
     */
    private void loadBestSellingItemsForCategory(JTable table, Integer categoryId) {
        if (table == null)
            return;

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        String sql = "SELECT m.nama_menu, k.nama_kategori, " +
                "COALESCE(SUM(td.qty), 0) as total_terjual, " +
                "COALESCE(SUM(td.subtotal), 0) as total_revenue " +
                "FROM tbl_menu m " +
                "JOIN tbl_kategori k ON m.id_kategori = k.id_kategori " +
                "LEFT JOIN tbl_transaksi_detail td ON m.id_menu = td.id_menu " +
                "WHERE m.is_active = TRUE ";

        // Add category filter if specified
        if (categoryId != null) {
            sql += "AND m.id_kategori = " + categoryId + " ";
        }

        sql += "GROUP BY m.id_menu, m.nama_menu, k.nama_kategori " +
                "ORDER BY total_terjual DESC"; // Show all menu items sorted by sales

        try (Connection conn = com.kedaikopi.config.DatabaseConfig.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            int rank = 1;
            while (rs.next()) {
                String namaMenu = rs.getString("nama_menu");
                String kategori = rs.getString("nama_kategori");
                int terjual = rs.getInt("total_terjual");
                double revenue = rs.getDouble("total_revenue");

                model.addRow(new Object[] {
                        rank++,
                        namaMenu,
                        kategori,
                        terjual + " pcs",
                        currencyFormat.format(revenue)
                });
            }

            if (model.getRowCount() == 0) {
                model.addRow(new Object[] { "", "Belum ada data transaksi", "", "", "" });
            }

        } catch (SQLException e) {
            logger.error("Error loading best selling items", e);
        }
    }

    /**
     * Load low stock items
     */
    private void loadLowStockItems() {
        if (tableLowStock == null)
            return;

        DefaultTableModel model = (DefaultTableModel) tableLowStock.getModel();
        model.setRowCount(0);

        String sql = "SELECT m.nama_menu, k.nama_kategori, m.stok " +
                "FROM tbl_menu m " +
                "JOIN tbl_kategori k ON m.id_kategori = k.id_kategori " +
                "WHERE m.stok < 10 AND m.is_active = TRUE " +
                "ORDER BY m.stok ASC";

        try (Connection conn = com.kedaikopi.config.DatabaseConfig.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String namaMenu = rs.getString("nama_menu");
                String kategori = rs.getString("nama_kategori");
                int stok = rs.getInt("stok");

                String status;
                if (stok == 0) {
                    status = "HABIS";
                } else if (stok < 5) {
                    status = "KRITIS";
                } else {
                    status = "RENDAH";
                }

                model.addRow(new Object[] {
                        namaMenu,
                        kategori,
                        stok,
                        status
                });
            }

            if (model.getRowCount() == 0) {
                model.addRow(new Object[] { "Semua stok aman", "", "", "" });
            }

        } catch (SQLException e) {
            logger.error("Error loading low stock items", e);
        }
    }

    /**
     * Start auto-refresh timer - refreshes dashboard every 5 minutes
     */
    private void startAutoRefresh() {
        // Auto-refresh every 5 minutes (300,000 ms)
        autoRefreshTimer = new javax.swing.Timer(300000, e -> {
            SwingUtilities.invokeLater(() -> {
                loadData();
            });
        });
        autoRefreshTimer.start();
    }

    /**
     * Refresh dashboard data - reloads all statistics and charts
     */
    public void refreshDashboardData() {
        SwingUtilities.invokeLater(() -> {
            try {
                loadData();

                // Reload charts if Owner dashboard
                if ("Owner".equals(currentUser.getRole())) {
                    refreshChartsOnly();
                }

                logger.info("Dashboard refreshed successfully");
            } catch (Exception ex) {
                logger.error("Error refreshing dashboard", ex);
            }
        });
    }

    /**
     * Refresh only the charts (without reloading card statistics)
     */
    private void refreshChartsOnly() {
        try (Connection conn = com.kedaikopi.config.DatabaseConfig.getInstance().getConnection()) {
            // Refresh sales trend chart
            if (salesTrendChart != null) {
                ChartPanel newSalesTrendChart = ChartFactory.createSalesTrendChart(conn);
                salesTrendChart.setChart(newSalesTrendChart.getChart());
            }

            // Refresh category chart
            if (categoryChart != null) {
                ChartPanel newCategoryChart = ChartFactory.createCategoryDistributionChart(conn);
                categoryChart.setChart(newCategoryChart.getChart());
            }

            // Refresh top products chart
            if (topProductsChart != null) {
                ChartPanel newTopProductsChart = ChartFactory.createTopProductsChart(conn);
                topProductsChart.setChart(newTopProductsChart.getChart());
            }

        } catch (SQLException ex) {
            logger.error("Error refreshing charts", ex);
        }
    }

    /**
     * Stop auto-refresh timer when panel is disposed
     */
    public void stopAutoRefresh() {
        if (autoRefreshTimer != null && autoRefreshTimer.isRunning()) {
            autoRefreshTimer.stop();
            logger.info("Dashboard auto-refresh stopped");
        }
    }
}
