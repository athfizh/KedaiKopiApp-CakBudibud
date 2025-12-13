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
    private JLabel lblTotalTransactions, lblLabaKotor, lblPengeluaranRestock, lblTotalPajak, lblTotalGaji,
            lblLabaBersih;
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
        // 6 cards in one row - compressed layout
        JPanel panel = new JPanel(new GridLayout(1, 6, 8, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        lblTotalTransactions = new JLabel("0", SwingConstants.CENTER);
        lblLabaKotor = new JLabel("Rp 0", SwingConstants.CENTER);
        lblPengeluaranRestock = new JLabel("Rp 0", SwingConstants.CENTER);
        lblTotalPajak = new JLabel("Rp 0", SwingConstants.CENTER);
        lblTotalGaji = new JLabel("Rp 0", SwingConstants.CENTER);
        lblLabaBersih = new JLabel("Rp 0", SwingConstants.CENTER);

        panel.add(createSummaryCard("Total Transaksi", lblTotalTransactions, new Color(33, 150, 243))); // Blue
        panel.add(createSummaryCard("Laba Kotor", lblLabaKotor, new Color(76, 175, 80))); // Green
        panel.add(createSummaryCard("Pengeluaran Restock", lblPengeluaranRestock, new Color(244, 67, 54))); // Red
        panel.add(createSummaryCard("Pajak", lblTotalPajak, new Color(255, 152, 0))); // Orange
        panel.add(createSummaryCard("Gaji Karyawan", lblTotalGaji, new Color(103, 58, 183))); // Purple
        panel.add(createSummaryCard("Laba Bersih", lblLabaBersih, new Color(0, 150, 136))); // Teal

        return panel;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createLineBorder(color.darker(), 2));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        // Add tooltip to show full value when text is truncated
        valueLabel.addPropertyChangeListener("text", evt -> {
            String text = valueLabel.getText();
            if (text != null && !text.isEmpty()) {
                valueLabel.setToolTipText(text); // Show full value on hover
            }
        });

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
            NumberFormat currencyFormat = NumberFormat
                    .getCurrencyInstance(new Locale.Builder().setLanguage("id").setRegion("ID").build());

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
        double labaKotor = 0; // Total Pendapatan (grand_total)
        double pengeluaran = 0; // Pengeluaran restock
        double totalPajak = 0; // Total pajak
        double totalGaji = 0; // Total gaji karyawan aktif

        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            NumberFormat currencyFormat = NumberFormat
                    .getCurrencyInstance(new Locale.Builder().setLanguage("id").setRegion("ID").build());

            // 1. Total transactions
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

            // 2. Laba Kotor = Total Pendapatan (SUM of grand_total)
            String labaSql = "SELECT COALESCE(SUM(grand_total), 0) AS laba_kotor FROM tbl_transaksi_header " +
                    "WHERE EXTRACT(MONTH FROM tanggal) = ? AND EXTRACT(YEAR FROM tanggal) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(labaSql)) {
                stmt.setInt(1, selectedMonth);
                stmt.setInt(2, selectedYear);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        labaKotor = rs.getDouble("laba_kotor");
                        lblLabaKotor.setText(currencyFormat.format(labaKotor));
                    }
                }
            }

            // 3. Pengeluaran Restock = Total harga menu yang direstock
            String restockSql = "SELECT COALESCE(SUM(rh.qty_added * m.harga), 0) AS pengeluaran " +
                    "FROM tbl_restock_history rh " +
                    "JOIN tbl_menu m ON rh.id_menu = m.id_menu " +
                    "WHERE EXTRACT(MONTH FROM rh.created_at) = ? AND EXTRACT(YEAR FROM rh.created_at) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(restockSql)) {
                stmt.setInt(1, selectedMonth);
                stmt.setInt(2, selectedYear);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        pengeluaran = rs.getDouble("pengeluaran");
                        lblPengeluaranRestock.setText(currencyFormat.format(pengeluaran));
                    }
                }
            }

            // 4. Total Pajak = SUM(pajak) from transactions
            String pajakSql = "SELECT COALESCE(SUM(pajak), 0) AS total_pajak FROM tbl_transaksi_header " +
                    "WHERE EXTRACT(MONTH FROM tanggal) = ? AND EXTRACT(YEAR FROM tanggal) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(pajakSql)) {
                stmt.setInt(1, selectedMonth);
                stmt.setInt(2, selectedYear);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalPajak = rs.getDouble("total_pajak");
                        lblTotalPajak.setText(currencyFormat.format(totalPajak));
                    }
                }
            }

            // 5. Total Gaji Karyawan = SUM of monthly salaries (active employees only)
            String gajiSql = "SELECT COALESCE(SUM(base_salary), 0) AS total_gaji FROM tbl_user " +
                    "WHERE is_active = TRUE";
            try (PreparedStatement stmt = conn.prepareStatement(gajiSql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalGaji = rs.getDouble("total_gaji");
                        lblTotalGaji.setText(currencyFormat.format(totalGaji));
                    }
                }
            }

            // 6. Laba Bersih = Laba Kotor - Pengeluaran - Pajak - Gaji
            double labaBersih = labaKotor - pengeluaran - totalPajak - totalGaji;
            lblLabaBersih.setText(currencyFormat.format(labaBersih));

        } catch (SQLException e) {
            logger.error("Error loading summary", e);
        }
    }

    private void exportCurrentTab() {
        // Prepare arrays for all 3 tables
        JTable[] tables = new JTable[] {
                salesTable,
                stockInTable,
                stockOutTable
        };

        // Prepare sheet names
        String[] sheetNames = new String[] {
                "Transaksi Penjualan",
                "Stok Masuk",
                "Stok Keluar"
        };

        // Prepare summary values to include in Excel
        String[] summaryLabels = new String[] {
                "Total Transaksi",
                "Laba Kotor",
                "Pengeluaran Restock",
                "Pajak",
                "Gaji Karyawan",
                "Laba Bersih"
        };

        String[] summaryValues = new String[] {
                lblTotalTransactions.getText(),
                lblLabaKotor.getText(),
                lblPengeluaranRestock.getText(),
                lblTotalPajak.getText(),
                lblTotalGaji.getText(),
                lblLabaBersih.getText()
        };

        // Generate default filename with month and year
        String[] monthNames = { "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember" };
        String monthName = monthNames[selectedMonth - 1];
        String defaultFileName = String.format("Laporan_Bulanan_%s_%d", monthName, selectedYear);

        // Export all sheets to single Excel file with summary
        boolean success = ExcelExporter.exportMultipleSheetsToExcel(
                tables,
                sheetNames,
                summaryLabels,
                summaryValues,
                defaultFileName,
                (JFrame) getOwner());

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "File Excel dengan 3 sheet berhasil disimpan!\n" +
                            "- Transaksi Penjualan\n" +
                            "- Stok Masuk\n" +
                            "- Stok Keluar\n\n" +
                            "Termasuk ringkasan finansial di sheet pertama",
                    "Export Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
