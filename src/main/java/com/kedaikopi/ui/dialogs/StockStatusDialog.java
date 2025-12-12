package com.kedaikopi.ui.dialogs;

import com.kedaikopi.util.ColorScheme;
import com.kedaikopi.config.DatabaseConfig;
import com.kedaikopi.util.ExcelExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Stock Status Report Dialog
 * Shows comprehensive stock status with color-coded categories
 */
public class StockStatusDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(StockStatusDialog.class);

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblHabis, lblKritis, lblRendah, lblAman;

    public StockStatusDialog(JFrame parent) {
        super(parent, "📊 Laporan Status Stok", true);
        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Header panel with summary
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Table panel
        add(createTablePanel(), BorderLayout.CENTER);

        // Button panel
        add(createButtonPanel(), BorderLayout.SOUTH);

        // Load data
        loadStockData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        // Stats cards
        lblHabis = new JLabel("0", SwingConstants.CENTER);
        lblKritis = new JLabel("0", SwingConstants.CENTER);
        lblRendah = new JLabel("0", SwingConstants.CENTER);
        lblAman = new JLabel("0", SwingConstants.CENTER);

        panel.add(createStatCard("HABIS", lblHabis, new Color(220, 53, 69)));
        panel.add(createStatCard("KRITIS", lblKritis, new Color(255, 152, 0)));
        panel.add(createStatCard("RENDAH", lblRendah, new Color(255, 235, 59)));
        panel.add(createStatCard("AMAN", lblAman, new Color(76, 175, 80)));

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createLineBorder(color.darker(), 2));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        String[] columns = { "Nama Menu", "Kategori", "Stok", "Harga", "Nilai Stok", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(ColorScheme.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(230, 230, 250));

        // Custom renderer for Status column
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value.toString();
                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 11));

                if (!isSelected) {
                    switch (status) {
                        case "HABIS":
                            c.setBackground(new Color(255, 235, 238));
                            setForeground(new Color(220, 53, 69));
                            break;
                        case "KRITIS":
                            c.setBackground(new Color(255, 243, 224));
                            setForeground(new Color(255, 152, 0));
                            break;
                        case "RENDAH":
                            c.setBackground(new Color(255, 253, 231));
                            setForeground(new Color(255, 152, 0));
                            break;
                        default: // AMAN
                            c.setBackground(new Color(232, 245, 233));
                            setForeground(new Color(76, 175, 80));
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
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
        btnExport.addActionListener(e -> exportToExcel());

        JButton btnClose = new JButton("Tutup");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.addActionListener(e -> dispose());

        panel.add(btnExport);
        panel.add(btnClose);

        return panel;
    }

    private void loadStockData() {
        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            // Load summary stats
            String statsSql = "SELECT " +
                    "COUNT(CASE WHEN stok = 0 THEN 1 END) AS habis, " +
                    "COUNT(CASE WHEN stok <= 5 AND stok > 0 THEN 1 END) AS kritis, " +
                    "COUNT(CASE WHEN stok <= 10 AND stok > 5 THEN 1 END) AS rendah, " +
                    "COUNT(CASE WHEN stok > 10 THEN 1 END) AS aman " +
                    "FROM tbl_menu WHERE is_active = TRUE";

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(statsSql)) {
                if (rs.next()) {
                    lblHabis.setText(String.valueOf(rs.getInt("habis")));
                    lblKritis.setText(String.valueOf(rs.getInt("kritis")));
                    lblRendah.setText(String.valueOf(rs.getInt("rendah")));
                    lblAman.setText(String.valueOf(rs.getInt("aman")));
                }
            }

            // Load table data
            String dataSql = "SELECT m.nama_menu, k.nama_kategori, m.stok, m.harga, " +
                    "(m.stok * m.harga) AS nilai_stok, " +
                    "CASE WHEN m.stok = 0 THEN 'HABIS' " +
                    "WHEN m.stok <= 5 THEN 'KRITIS' " +
                    "WHEN m.stok <= 10 THEN 'RENDAH' " +
                    "ELSE 'AMAN' END AS status " +
                    "FROM tbl_menu m " +
                    "JOIN tbl_kategori k ON m.id_kategori = k.id_kategori " +
                    "WHERE m.is_active = TRUE " +
                    "ORDER BY m.stok ASC, m.nama_menu";

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(dataSql)) {
                while (rs.next()) {
                    tableModel.addRow(new Object[] {
                            rs.getString("nama_menu"),
                            rs.getString("nama_kategori"),
                            rs.getInt("stok"),
                            currencyFormat.format(rs.getDouble("harga")),
                            currencyFormat.format(rs.getDouble("nilai_stok")),
                            rs.getString("status")
                    });
                }
            }

        } catch (SQLException e) {
            logger.error("Error loading stock data", e);
            JOptionPane.showMessageDialog(this,
                    "Error memuat data stok: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToExcel() {
        boolean success = ExcelExporter.exportToExcel(table, "Laporan_Status_Stok", (JFrame) getOwner());
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "File Excel berhasil disimpan!",
                    "Export Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
