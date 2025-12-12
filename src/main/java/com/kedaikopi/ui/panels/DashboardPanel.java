package com.kedaikopi.ui.panels;

import com.kedaikopi.model.User;

import com.kedaikopi.util.ColorScheme;
import com.kedaikopi.util.ChartFactory;
import com.kedaikopi.ui.components.UIComponents;
import com.kedaikopi.ui.dialogs.StockStatusDialog;
import com.kedaikopi.ui.dialogs.MonthlyTransactionDialog;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
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
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        initComponents();
        loadData();
    }

    private void initComponents() {
        // Use BorderLayout to contain the scroll pane
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BG_LIGHT);

        // Content panel - wider first card (40%), smaller charts for full visibility
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(
                new MigLayout("fill, insets 20, width 100%", "[40%][30%][30%]", "[]15[120!]15[240!]15[200!]"));
        contentPanel.setBackground(ColorScheme.BG_LIGHT);

        // Title - centered above all content
        JLabel title = UIComponents.createLabel("Dashboard", UIComponents.LabelType.HEADING);
        contentPanel.add(title, "span 3, wrap");

        // Wrap content in scroll pane with smooth scrollingadmijn
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Smooth scrolling configuration
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);

        add(scrollPane, BorderLayout.CENTER);

        // Role-based content added to contentPanel
        if ("Owner".equals(currentUser.getRole())) {
            createOwnerDashboard(contentPanel);
        } else if ("Kasir".equals(currentUser.getRole())) {
            createKasirDashboard(contentPanel);
        } else if ("Stocker".equals(currentUser.getRole())) {
            createStockerDashboard(contentPanel);
        }

        // Start auto-refresh timer (5 minutes) for Owner dashboard only
        if ("Owner".equals(currentUser.getRole())) {
            startAutoRefresh();
        }
    }

    /**
     * Dashboard for Owner - Full statistics with charts
     */
    private void createOwnerDashboard(JPanel panel) {
        // Statistics Cards Row
        JPanel cardSales = createStatCardPanel("Penjualan Hari Ini", "Rp 0",
                ColorScheme.ACCENT_GREEN, null);
        JPanel cardTransactions = createStatCardPanel("Total Transaksi", "0",
                ColorScheme.ACCENT_BLUE, null);

        lblTodaySales = (JLabel) findValueLabel(cardSales);
        lblTodayTransactions = (JLabel) findValueLabel(cardTransactions);

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

        panel.add(cardSales, "grow");
        panel.add(cardTransactions, "grow");
        panel.add(cardOnlineKasir, "grow, wrap");

        // Best selling items - compact table
        JPanel bestSellingPanel = createCompactBestSellingPanel();
        panel.add(bestSellingPanel, "span 3, grow, wrap");
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

        panel.add(cardLowStock, "grow");
        panel.add(cardTotalItems, "grow");
        panel.add(cardTotalStock, "grow, wrap");

        // Low stock alert table - compact
        JPanel lowStockPanel = createCompactLowStockPanel();
        panel.add(lowStockPanel, "span 3, grow, wrap");
    }

    /**
     * Create best selling items panel
     */
    private JPanel createBestSellingPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[]10[grow]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1));

        // Header - remove emoji
        JLabel lblTitle = UIComponents.createLabel("Menu Terlaris", UIComponents.LabelType.HEADING);
        panel.add(lblTitle, "wrap");

        // Table
        String[] columns = { "Rank", "Nama Menu", "Kategori", "Terjual", "Revenue" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableBestSelling = UIComponents.createStyledTable(model);
        tableBestSelling.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableBestSelling.getColumnModel().getColumn(3).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(tableBestSelling);
        scrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));
        panel.add(scrollPane, "grow");

        return panel;
    }

    /**
     * Create COMPACT best selling panel for single-screen dashboard
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

        // Table
        String[] columns = { "#", "Menu", "Kategori", "Terjual" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableBestSelling = UIComponents.createStyledTable(model);
        tableBestSelling.setRowHeight(20);
        tableBestSelling.getColumnModel().getColumn(0).setPreferredWidth(25);
        tableBestSelling.getColumnModel().getColumn(3).setPreferredWidth(55);

        JScrollPane scrollPane = new JScrollPane(tableBestSelling);
        scrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));
        scrollPane.setPreferredSize(new Dimension(0, 280)); // Increased from 170 to 280
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Always show scrollbar
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        panel.add(scrollPane, "grow");

        return panel;
    }

    /**
     * Create low stock alert panel
     */
    private JPanel createLowStockPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[]10[grow]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1));

        // Header - remove emoji
        JLabel lblTitle = UIComponents.createLabel("Stok Menipis", UIComponents.LabelType.HEADING);
        lblTitle.setForeground(ColorScheme.ACCENT_ORANGE);
        panel.add(lblTitle, "wrap");

        // Table
        String[] columns = { "Nama Menu", "Kategori", "Stok", "Status" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableLowStock = UIComponents.createStyledTable(model);
        tableLowStock.getColumnModel().getColumn(2).setPreferredWidth(60);
        tableLowStock.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(tableLowStock);
        scrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));
        panel.add(scrollPane, "grow");

        return panel;
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
        scrollPane.setPreferredSize(new Dimension(0, 280)); // Increased from 170 to 280 for more visible rows
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Always show scrollbar
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        panel.add(scrollPane, "grow");

        return panel;
    }

    /**
     * Create chart panel wrapper with consistent styling
     */
    private JPanel createChartPanel(String title, ChartPanel chartPanel) {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[]10[grow]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1));

        // Title
        JLabel lblTitle = UIComponents.createLabel(title, UIComponents.LabelType.HEADING);
        lblTitle.setForeground(ColorScheme.TEXT_PRIMARY);
        panel.add(lblTitle, "wrap");

        // Chart
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(null);
        panel.add(chartPanel, "grow");

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
     * Create stock status card with JTable (brown header like category table)
     */
    private JPanel createStockStatusCard() {
        JPanel card = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[]10[grow]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Title - simple gray text like "Total Transaksi"
        JLabel lblTitle = new JLabel("Status Stok");
        lblTitle.setFont(UIComponents.FONT_SMALL);
        lblTitle.setForeground(ColorScheme.TEXT_SECONDARY);
        card.add(lblTitle, "wrap");

        // Create table model with 3 columns
        String[] columns = { "HABIS", "KRITIS", "RENDAH" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Load and group stock status data
        try (Connection conn = com.kedaikopi.config.DatabaseConfig.getInstance().getConnection()) {
            String sql = "SELECT m.nama_menu, m.stok " +
                    "FROM tbl_menu m " +
                    "WHERE m.stok < 10 AND m.is_active = TRUE " +
                    "ORDER BY m.stok ASC, m.nama_menu ASC";

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                // Group items by status
                java.util.List<String> habis = new java.util.ArrayList<>();
                java.util.List<String> kritis = new java.util.ArrayList<>();
                java.util.List<String> rendah = new java.util.ArrayList<>();

                while (rs.next()) {
                    String namaMenu = rs.getString("nama_menu");
                    int stok = rs.getInt("stok");
                    String item = namaMenu + " (" + stok + ")";

                    if (stok == 0) {
                        habis.add(item);
                    } else if (stok < 5) {
                        kritis.add(item);
                    } else {
                        rendah.add(item);
                    }
                }

                // Add rows to table (each row has items from all 3 columns)
                int maxRows = Math.max(Math.max(habis.size(), kritis.size()), rendah.size());
                if (maxRows == 0)
                    maxRows = 1; // At least one row for "no data"

                for (int i = 0; i < maxRows; i++) {
                    String colHabis = i < habis.size() ? habis.get(i) : "";
                    String colKritis = i < kritis.size() ? kritis.get(i) : "";
                    String colRendah = i < rendah.size() ? rendah.get(i) : "";

                    model.addRow(new Object[] { colHabis, colKritis, colRendah });
                }

                // If all empty, show message
                if (habis.isEmpty() && kritis.isEmpty() && rendah.isEmpty()) {
                    model.setRowCount(0);
                    model.addRow(new Object[] { "Semua stok aman ✓", "", "" });
                }

            }
        } catch (SQLException e) {
            logger.error("Error loading stock status", e);
            model.addRow(new Object[] { "Error memuat data", "", "" });
        }

        // Create and style table
        JTable table = new JTable(model);
        table.setFont(UIComponents.FONT_SMALL);
        table.setRowHeight(22);
        table.setShowGrid(true);
        table.setGridColor(ColorScheme.BORDER_COLOR);
        table.setBackground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Style header like category table (brown header, white text)
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(78, 52, 46)); // Brown like category table
        header.setForeground(Color.WHITE);
        header.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD));
        header.setReorderingAllowed(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(scrollPane, "grow");

        return card;
    }

    /**
     * Create action buttons panel (replaces stock status card)
     */
    private JPanel createActionButtonsPanel() {
        // Vertical stacking for better fit in narrow column
        JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[grow][10][grow][10][grow]"));
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

        // Button 3: Refresh Dashboard (NEW!)
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
     * Create status column for table layout
     */
    private JPanel createStatusColumn(String title, java.util.List<String> items, Color color) {
        JPanel column = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[]5[]"));
        column.setBackground(Color.WHITE);

        // Column header
        JLabel lblHeader = new JLabel(title);
        lblHeader.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD, 11f));
        lblHeader.setForeground(color);
        column.add(lblHeader, "wrap");

        // Separator line
        JSeparator separator = new JSeparator();
        separator.setForeground(color);
        separator.setBackground(color);
        column.add(separator, "growx, wrap, gapbottom 5");

        // Items
        if (items.isEmpty()) {
            JLabel lblEmpty = new JLabel("-");
            lblEmpty.setFont(UIComponents.FONT_SMALL);
            lblEmpty.setForeground(ColorScheme.TEXT_SECONDARY);
            column.add(lblEmpty, "wrap");
        } else {
            for (String item : items) {
                JLabel lblItem = new JLabel("• " + item);
                lblItem.setFont(UIComponents.FONT_SMALL);
                lblItem.setForeground(ColorScheme.TEXT_PRIMARY);
                column.add(lblItem, "wrap");
            }
        }

        return column;
    }

    /**
     * Add status group to stock card
     */
    private void addStatusGroup(JPanel panel, String title, java.util.List<String> items, Color color) {
        // Status title
        JLabel lblStatus = new JLabel(title);
        lblStatus.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD, 11f));
        lblStatus.setForeground(color);
        panel.add(lblStatus, "wrap, gapbottom 3");

        // Items
        for (String item : items) {
            JLabel lblItem = new JLabel("  • " + item);
            lblItem.setFont(UIComponents.FONT_SMALL);
            lblItem.setForeground(ColorScheme.TEXT_PRIMARY);
            panel.add(lblItem, "wrap");
        }

        // Add spacing after group
        panel.add(new JLabel(" "), "wrap, gapbottom 5");
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

            // Total sales today
            if (lblTodaySales != null) {
                String salesSql = "SELECT COALESCE(SUM(total_harga), 0) as total " +
                        "FROM tbl_transaksi_header " +
                        "WHERE DATE(tanggal) = CURRENT_DATE";
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

        DefaultTableModel model = (DefaultTableModel) tableBestSelling.getModel();
        model.setRowCount(0);

        String sql = "SELECT m.nama_menu, k.nama_kategori, " +
                "COALESCE(SUM(td.qty), 0) as total_terjual, " +
                "COALESCE(SUM(td.subtotal), 0) as total_revenue " +
                "FROM tbl_menu m " +
                "JOIN tbl_kategori k ON m.id_kategori = k.id_kategori " +
                "LEFT JOIN tbl_transaksi_detail td ON m.id_menu = td.id_menu " +
                "WHERE m.is_active = TRUE " +
                "GROUP BY m.id_menu, m.nama_menu, k.nama_kategori " +
                "ORDER BY total_terjual DESC " +
                "LIMIT 10";

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
