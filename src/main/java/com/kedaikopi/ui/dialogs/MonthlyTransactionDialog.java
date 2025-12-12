package com.kedaikopi.ui.dialogs;

import com.kedaikopi.util.ColorScheme;
import net.miginfocom.swing.MigLayout;
import com.kedaikopi.config.DatabaseConfig;
import com.kedaikopi.util.ExcelExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Monthly Transaction Report Dialog
 * Shows sales and stock addition reports with Excel export
 */
public class MonthlyTransactionDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(MonthlyTransactionDialog.class);

    private JTabbedPane tabbedPane;
    private JTable salesTable, stockInTable, stockOutTable;
    private DefaultTableModel salesModel, stockInModel, stockOutModel;
    private JLabel lblTotalTransactions, lblTotalRevenue, lblTotalExpenses, lblTotalProfit;
    private JComboBox<String> cmbMonth, cmbYear;
    private int selectedMonth, selectedYear;

    public MonthlyTransactionDialog(Window parent) {
        super(parent, "Data Transaksi", ModalityType.APPLICATION_MODAL);
        setSize(1200, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Initialize with current month/year
        java.util.Calendar cal = java.util.Calendar.getInstance();
        selectedMonth = cal.get(java.util.Calendar.MONTH) + 1; // 1-12
        selectedYear = cal.get(java.util.Calendar.YEAR);

        // Filter panel with month/year dropdowns
        add(createFilterPanel(), BorderLayout.NORTH);

        // Summary cards panel
        JPanel summaryContainer = new JPanel(new BorderLayout());
        summaryContainer.setBackground(Color.WHITE);
        summaryContainer.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        summaryContainer.add(createSummaryPanel(), BorderLayout.CENTER);

        // Main content with summary and tabs
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(summaryContainer, BorderLayout.NORTH);

        // Tab pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("Transaksi Penjualan", createSalesTab());
        tabbedPane.addTab("Stok Masuk", createStockInTab());
        tabbedPane.addTab("Stok Keluar", createStockOutTab());
        centerPanel.add(tabbedPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Button panel
        add(createButtonPanel(), BorderLayout.SOUTH);

        // Load data and show
        loadData();
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[grow][]15[]15[]15[]", "[]10[]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));

        // Title
        JLabel lblTitle = new JLabel("Filter Data Transaksi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(60, 60, 60));
        panel.add(lblTitle, "span, wrap");

        // Month dropdown
        JLabel lblMonth = new JLabel("Bulan:");
        lblMonth.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lblMonth, "skip");

        String[] months = { "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember" };
        cmbMonth = new JComboBox<>(months);
        cmbMonth.setSelectedIndex(selectedMonth - 1);
        cmbMonth.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(cmbMonth);

        // Year dropdown
        JLabel lblYear = new JLabel("Tahun:");
        lblYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lblYear);

        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String[] years = new String[5]; // Last 5 years
        for (int i = 0; i < 5; i++) {
            years[i] = String.valueOf(currentYear - i);
        }
        cmbYear = new JComboBox<>(years);
        cmbYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        // Set selected year to current year if available, otherwise default to first in
        // list
        for (int i = 0; i < years.length; i++) {
            if (Integer.parseInt(years[i]) == selectedYear) {
                cmbYear.setSelectedIndex(i);
                break;
            }
        }
        panel.add(cmbYear);

        // Refresh button
        JButton btnRefresh = new JButton("Tampilkan");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setBackground(new Color(33, 150, 243));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> {
            selectedMonth = cmbMonth.getSelectedIndex() + 1;
            selectedYear = Integer.parseInt((String) cmbYear.getSelectedItem());
            refreshData();
        });
        panel.add(btnRefresh);

        return panel;
    }

    private void refreshData() {
        salesModel.setRowCount(0);
        stockInModel.setRowCount(0);
        stockOutModel.setRowCount(0);
        loadData();
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        lblTotalTransactions = new JLabel("0", SwingConstants.CENTER);
        lblTotalRevenue = new JLabel("Rp 0", SwingConstants.CENTER);
        lblTotalExpenses = new JLabel("Rp 0", SwingConstants.CENTER);
        lblTotalProfit = new JLabel("Rp 0", SwingConstants.CENTER);

        panel.add(createSummaryCard("Total Transaksi", lblTotalTransactions, new Color(33, 150, 243))); // Blue
        panel.add(createSummaryCard("Total Pendapatan", lblTotalRevenue, new Color(76, 175, 80))); // Green
        panel.add(createSummaryCard("Total Pengeluaran", lblTotalExpenses, new Color(244, 67, 54))); // Red
        panel.add(createSummaryCard("Total Laba", lblTotalProfit, new Color(156, 39, 176))); // Purple

        return panel;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createLineBorder(color.darker(), 2));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSalesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = { "Tanggal", "ID", "Kasir", "Subtotal", "Pajak (10%)", "Grand Total", "Menu Terjual",
                "Total Item" };
        salesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        salesTable = createStyledTable(salesModel);
        JScrollPane scrollPane = new JScrollPane(salesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStockInTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = { "Tanggal", "Menu", "Qty Ditambah", "Stok Sebelum", "Stok Setelah", "Admin/Stocker",
                "Catatan" };
        stockInModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        stockInTable = createStyledTable(stockInModel);

        // Green background for stock additions
        stockInTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);

                if (!isSelected) {
                    c.setBackground(new Color(232, 245, 233)); // Light green
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(stockInTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStockOutTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = { "Tanggal", "Menu", "Qty Terjual", "Kasir", "ID Transaksi" };
        stockOutModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        stockOutTable = createStyledTable(stockOutModel);

        // Orange background for stock sold
        stockOutTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);

                if (!isSelected) {
                    c.setBackground(new Color(255, 243, 224)); // Light orange
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(stockOutTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(ColorScheme.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(230, 230, 250));
        table.setGridColor(new Color(220, 220, 220));
        return table;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        JButton btnExport = new JButton("Export ke Excel");
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExport.setBackground(new Color(67, 160, 71));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.addActionListener(e -> exportCurrentTab());

        JButton btnClose = new JButton("Tutup");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.addActionListener(e -> dispose());

        panel.add(btnExport);
        panel.add(btnClose);

        return panel;
    }

    private void loadData() {
        loadSummary();
        loadSalesData();
        loadStockInData();
        loadStockOutData();
    }

    private void loadSalesData() {
        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            String sql = "SELECT th.tanggal, th.id_transaksi_header, th.nama_kasir, " +
                    "th.total_harga AS subtotal, th.pajak, th.grand_total, " +
                    "STRING_AGG(td.nama_menu || ' (' || td.qty || 'x)', ', ' ORDER BY td.nama_menu) AS menu_details, " +
                    "SUM(td.qty) AS total_items " +
                    "FROM tbl_transaksi_header th " +
                    "LEFT JOIN tbl_transaksi_detail td ON th.id_transaksi_header = td.id_transaksi_header " +
                    "WHERE EXTRACT(MONTH FROM th.tanggal) = ? AND EXTRACT(YEAR FROM th.tanggal) = ? " +
                    "GROUP BY th.id_transaksi_header, th.tanggal, th.nama_kasir, th.total_harga, th.pajak, th.grand_total "
                    +
                    "ORDER BY th.tanggal DESC";

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selectedMonth);
                stmt.setInt(2, selectedYear);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        salesModel.addRow(new Object[] {
                                dateFormat.format(rs.getDate("tanggal")),
                                rs.getInt("id_transaksi_header"),
                                rs.getString("nama_kasir"),
                                currencyFormat.format(rs.getDouble("subtotal")),
                                currencyFormat.format(rs.getDouble("pajak")),
                                currencyFormat.format(rs.getDouble("grand_total")),
                                rs.getString("menu_details") != null ? rs.getString("menu_details") : "-",
                                rs.getInt("total_items")
                        });
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading sales data", e);
            showError("Error memuat data penjualan: " + e.getMessage());
        }
    }

    private void loadStockInData() {
        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            String sql = "SELECT " +
                    "    rh.created_at AS tanggal, " +
                    "    m.nama_menu, " +
                    "    rh.qty_added AS qty_ditambah, " +
                    "    rh.qty_before AS stok_sebelum, " +
                    "    rh.qty_after AS stok_setelah, " +
                    "    u.nama_lengkap AS user_name, " +
                    "    COALESCE(rh.notes, 'Penambahan stok') AS catatan " +
                    "FROM tbl_restock_history rh " +
                    "JOIN tbl_menu m ON rh.id_menu = m.id_menu " +
                    "JOIN tbl_user u ON rh.id_user = u.id_user " +
                    "WHERE EXTRACT(MONTH FROM rh.created_at) = ? AND EXTRACT(YEAR FROM rh.created_at) = ? " +
                    "ORDER BY rh.created_at DESC";

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selectedMonth);
                stmt.setInt(2, selectedYear);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        stockInModel.addRow(new Object[] {
                                dateFormat.format(rs.getTimestamp("tanggal")),
                                rs.getString("nama_menu"),
                                "+" + rs.getInt("qty_ditambah"),
                                rs.getInt("stok_sebelum"),
                                rs.getInt("stok_setelah"),
                                rs.getString("user_name"),
                                rs.getString("catatan")
                        });
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading stock IN data", e);
            showError("Error memuat data stok masuk: " + e.getMessage());
        }
    }

    private void loadStockOutData() {
        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            String sql = "SELECT " +
                    "    th.tanggal, " +
                    "    td.nama_menu, " +
                    "    td.qty AS qty_terjual, " +
                    "    th.nama_kasir, " +
                    "    th.id_transaksi_header " +
                    "FROM tbl_transaksi_header th " +
                    "JOIN tbl_transaksi_detail td ON th.id_transaksi_header = td.id_transaksi_header " +
                    "WHERE EXTRACT(MONTH FROM th.tanggal) = ? AND EXTRACT(YEAR FROM th.tanggal) = ? " +
                    "ORDER BY th.tanggal DESC";

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selectedMonth);
                stmt.setInt(2, selectedYear);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        stockOutModel.addRow(new Object[] {
                                dateFormat.format(rs.getTimestamp("tanggal")),
                                rs.getString("nama_menu"),
                                rs.getInt("qty_terjual"),
                                rs.getString("nama_kasir"),
                                "#" + rs.getInt("id_transaksi_header")
                        });
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading stock OUT data", e);
            showError("Error memuat data stok keluar: " + e.getMessage());
        }
    }

    private void loadSummary() {
        double revenue = 0;
        double expenses = 0;

        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            // Total transactions
            String countSql = "SELECT COUNT(*) AS total FROM tbl_transaksi_header " +
                    "WHERE EXTRACT(MONTH FROM tanggal) = ? AND EXTRACT(YEAR FROM tanggal) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
                stmt.setInt(1, selectedMonth);
                stmt.setInt(2, selectedYear);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        lblTotalTransactions.setText(String.valueOf(rs.getInt("total")));
                    }
                }
            }

            // Total revenue
            String revenueSql = "SELECT COALESCE(SUM(grand_total), 0) AS revenue FROM tbl_transaksi_header " +
                    "WHERE EXTRACT(MONTH FROM tanggal) = ? AND EXTRACT(YEAR FROM tanggal) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(revenueSql)) {
                stmt.setInt(1, selectedMonth);
                stmt.setInt(2, selectedYear);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        revenue = rs.getDouble("revenue");
                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        lblTotalRevenue.setText(currencyFormat.format(revenue));
                    }
                }
            }

            // Total expenses (Cost of Goods Sold - approximate as 60% of price)
            String expenseSql = "SELECT COALESCE(SUM(td.qty * m.harga * 0.6), 0) AS expenses " +
                    "FROM tbl_transaksi_header th " +
                    "JOIN tbl_transaksi_detail td ON th.id_transaksi_header = td.id_transaksi_header " +
                    "JOIN tbl_menu m ON td.id_menu = m.id_menu " +
                    "WHERE EXTRACT(MONTH FROM th.tanggal) = ? AND EXTRACT(YEAR FROM th.tanggal) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(expenseSql)) {
                stmt.setInt(1, selectedMonth);
                stmt.setInt(2, selectedYear);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        expenses = rs.getDouble("expenses");
                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        lblTotalExpenses.setText(currencyFormat.format(expenses));
                    }
                }
            }

            // Calculate profit (Revenue - Expenses)
            double profit = revenue - expenses;
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            lblTotalProfit.setText(currencyFormat.format(profit));

        } catch (SQLException e) {
            logger.error("Error loading summary", e);
        }
    }

    private void exportCurrentTab() {
        int selectedTab = tabbedPane.getSelectedIndex();
        JTable currentTable;
        String fileName;

        switch (selectedTab) {
            case 0: // Sales tab
                currentTable = salesTable;
                fileName = "Laporan_Transaksi_Penjualan";
                break;
            case 1: // Stock IN tab
                currentTable = stockInTable;
                fileName = "Laporan_Stok_Masuk";
                break;
            case 2: // Stock OUT tab
                currentTable = stockOutTable;
                fileName = "Laporan_Stok_Keluar";
                break;
            default:
                return;
        }

        boolean success = ExcelExporter.exportToExcel(currentTable, fileName, (JFrame) getOwner());
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "File Excel berhasil disimpan!",
                    "Export Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
