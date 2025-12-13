package com.kedaikopi.ui.dialogs;

import com.kedaikopi.config.DatabaseConfig;
import com.kedaikopi.util.ColorScheme;
import com.kedaikopi.ui.components.UIComponents;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Transaction Log Dialog - Shows detailed transaction history
 * Displays complete transaction information including items, prices, tax, and
 * payment details
 */
public class TransactionLogDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(TransactionLogDialog.class);
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    private JPanel contentPanel;

    private JLabel lblTotalTransactions;
    private JLabel lblTotalRevenue;

    private com.kedaikopi.model.User filterUser; // Filter transactions by specific user (for Kasir)

    public TransactionLogDialog(JFrame parent) {
        this(parent, null); // Call overloaded constructor with no filter
    }

    /**
     * Constructor with user filter (for Kasir)
     * 
     * @param parent     Parent frame
     * @param filterUser User to filter transactions by (null = show all)
     */
    public TransactionLogDialog(JFrame parent, com.kedaikopi.model.User filterUser) {
        super(parent, "Log Transaksi Hari Ini - Detail Lengkap", true);

        this.filterUser = filterUser;

        this.currencyFormat = NumberFormat
                .getCurrencyInstance(new Locale.Builder().setLanguage("id").setRegion("ID").build());
        this.dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy",
                new Locale.Builder().setLanguage("id").setRegion("ID").build());
        this.timeFormat = new SimpleDateFormat("HH:mm:ss");

        initComponents();
        loadTransactions("Hari Ini");

        setSize(1000, 700);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Content Panel (Scrollable)
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ColorScheme.BG_LIGHT);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20", "[grow][][]", "[]10[]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ColorScheme.BORDER_COLOR));

        // Title
        JLabel lblTitle = new JLabel("Log Transaksi Hari Ini");
        lblTitle.setFont(UIComponents.FONT_HEADING);
        lblTitle.setForeground(ColorScheme.ACCENT_BLUE);
        panel.add(lblTitle, "wrap");

        // Summary Cards
        lblTotalTransactions = new JLabel("0");
        lblTotalTransactions.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD, 16f));
        lblTotalTransactions.setForeground(ColorScheme.ACCENT_BLUE);

        lblTotalRevenue = new JLabel("Rp 0");
        lblTotalRevenue.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD, 16f));
        lblTotalRevenue.setForeground(ColorScheme.ACCENT_GREEN);

        JPanel summaryPanel = new JPanel(new MigLayout("insets 0", "[]20[]", "[]"));
        summaryPanel.setOpaque(false);

        JPanel transCard = createMiniCard("Total Transaksi Hari Ini", lblTotalTransactions, ColorScheme.ACCENT_BLUE);
        JPanel revenueCard = createMiniCard("Total Pendapatan Hari Ini", lblTotalRevenue, ColorScheme.ACCENT_GREEN);

        summaryPanel.add(transCard);
        summaryPanel.add(revenueCard);
        panel.add(summaryPanel, "cell 0 1");

        return panel;
    }

    private JPanel createMiniCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new MigLayout("fillx, insets 10", "[center]", "[]8[]"));
        card.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        card.setMinimumSize(new Dimension(180, 75));
        card.setPreferredSize(new Dimension(200, 80));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIComponents.FONT_SMALL);
        lblTitle.setForeground(ColorScheme.TEXT_SECONDARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblTitle, "wrap, align center");

        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel, "align center");

        return card;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[grow][]", "[]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, ColorScheme.BORDER_COLOR));

        JButton btnClose = UIComponents.createButton("Tutup", UIComponents.ButtonType.SECONDARY);
        btnClose.addActionListener(e -> dispose());
        panel.add(btnClose, "cell 1 0");

        return panel;
    }

    private void loadTransactions(String filter) {
        contentPanel.removeAll();

        String dateFilter = getDateFilter(filter);

        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            // Get transactions grouped by date
            String sql = "SELECT " +
                    "DATE(th.tanggal) as trans_date, " +
                    "th.id_transaksi_header, " +
                    "th.tanggal, " +
                    "th.nama_kasir, " +
                    "th.total_harga, " +
                    "th.pajak, " +
                    "th.grand_total, " +
                    "th.uang_bayar, " +
                    "th.kembalian, " +
                    "th.metode_pembayaran " +
                    "FROM tbl_transaksi_header th " +
                    dateFilter;

            // Add user filter for Kasir role
            if (filterUser != null) {
                if (dateFilter.isEmpty()) {
                    sql += "WHERE th.id_user = " + filterUser.getIdUser() + " ";
                } else {
                    sql += "AND th.id_user = " + filterUser.getIdUser() + " ";
                }
            }

            sql += "ORDER BY th.tanggal DESC";

            Map<String, List<Map<String, Object>>> transactionsByDate = new LinkedHashMap<String, List<Map<String, Object>>>();
            int totalCount = 0;
            double totalRevenue = 0;

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String dateKey = rs.getDate("trans_date").toString();

                    Map<String, Object> transaction = new HashMap<String, Object>();
                    transaction.put("id", rs.getInt("id_transaksi_header"));
                    transaction.put("tanggal", rs.getTimestamp("tanggal"));
                    transaction.put("kasir", rs.getString("nama_kasir"));
                    transaction.put("total_harga", rs.getDouble("total_harga"));
                    transaction.put("pajak", rs.getDouble("pajak"));
                    transaction.put("grand_total", rs.getDouble("grand_total"));
                    transaction.put("uang_bayar", rs.getDouble("uang_bayar"));
                    transaction.put("kembalian", rs.getDouble("kembalian"));
                    transaction.put("metode", rs.getString("metode_pembayaran"));

                    transactionsByDate.computeIfAbsent(dateKey, k -> new ArrayList<Map<String, Object>>())
                            .add(transaction);
                    totalCount++;
                    totalRevenue += rs.getDouble("grand_total");
                }
            }

            // Update summary
            lblTotalTransactions.setText(String.valueOf(totalCount));
            lblTotalRevenue.setText(String.format("Rp%,.2f", totalRevenue));

            // Create panels for each date
            if (transactionsByDate.isEmpty()) {
                JLabel noData = new JLabel("Tidak ada transaksi untuk periode ini");
                noData.setFont(UIComponents.FONT_BODY);
                noData.setForeground(ColorScheme.TEXT_SECONDARY);
                noData.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(Box.createVerticalStrut(50));
                contentPanel.add(noData);
            } else {
                for (Map.Entry<String, List<Map<String, Object>>> entry : transactionsByDate.entrySet()) {
                    contentPanel.add(createDateSection(entry.getKey(), entry.getValue(), conn));
                    contentPanel.add(Box.createVerticalStrut(10));
                }
            }

        } catch (SQLException e) {
            logger.error("Error loading transactions", e);
            JOptionPane.showMessageDialog(this,
                    "Error memuat data transaksi: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private String getDateFilter(String filter) {
        switch (filter) {
            case "Hari Ini":
                return "WHERE DATE(th.tanggal) = CURRENT_DATE ";
            case "7 Hari Terakhir":
                return "WHERE th.tanggal >= CURRENT_DATE - INTERVAL '7 days' ";
            case "30 Hari Terakhir":
                return "WHERE th.tanggal >= CURRENT_DATE - INTERVAL '30 days' ";
            default:
                return "";
        }
    }

    private JPanel createDateSection(String dateStr, List<Map<String, Object>> transactions, Connection conn)
            throws SQLException {
        JPanel section = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[]10[]"));
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Date header
        try {
            Date date = java.sql.Date.valueOf(dateStr);
            JLabel lblDate = new JLabel(dateFormat.format(date) + " - " + transactions.size() + " Transaksi");
            lblDate.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD, 14f));
            lblDate.setForeground(ColorScheme.ACCENT_BLUE);
            section.add(lblDate, "wrap");
        } catch (Exception e) {
            JLabel lblDate = new JLabel(dateStr + " - " + transactions.size() + " Transaksi");
            lblDate.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD, 14f));
            lblDate.setForeground(ColorScheme.ACCENT_BLUE);
            section.add(lblDate, "wrap");
        }

        // Transactions for this date
        for (Map<String, Object> trans : transactions) {
            section.add(createTransactionCard(trans, conn), "growx, wrap, gapbottom 10");
        }

        return section;
    }

    private JPanel createTransactionCard(Map<String, Object> transaction, Connection conn) throws SQLException {
        JPanel card = new JPanel(new MigLayout("fill, insets 12", "[grow]", "[]5[]10[]"));
        card.setBackground(new Color(249, 250, 251));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        // Header: Transaction ID, Time, Cashier
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[]20[]push[]", "[]"));
        headerPanel.setOpaque(false);

        JLabel lblId = new JLabel("#" + transaction.get("id"));
        lblId.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD));
        lblId.setForeground(ColorScheme.ACCENT_BLUE);
        headerPanel.add(lblId);

        JLabel lblTime = new JLabel("Waktu: " + timeFormat.format((Timestamp) transaction.get("tanggal")));
        lblTime.setFont(UIComponents.FONT_SMALL);
        lblTime.setForeground(ColorScheme.TEXT_SECONDARY);
        headerPanel.add(lblTime);

        JLabel lblKasir = new JLabel("Kasir: " + transaction.get("kasir"));
        lblKasir.setFont(UIComponents.FONT_SMALL.deriveFont(Font.BOLD));
        lblKasir.setForeground(new Color(103, 58, 183));
        headerPanel.add(lblKasir);

        card.add(headerPanel, "growx, wrap");

        // Items table
        JPanel itemsPanel = createItemsTable((Integer) transaction.get("id"), conn);
        card.add(itemsPanel, "growx, wrap");

        // Footer: Payment details
        JPanel footerPanel = new JPanel(new MigLayout("insets 0", "push[]", "[]2[]2[]2[]2[]2[]"));
        footerPanel.setOpaque(false);

        addPaymentRow(footerPanel, "Subtotal:", currencyFormat.format((Double) transaction.get("total_harga")), false);
        addPaymentRow(footerPanel, "Pajak (10%):", currencyFormat.format((Double) transaction.get("pajak")), false);
        addPaymentRow(footerPanel, "Total:", currencyFormat.format((Double) transaction.get("grand_total")), true);
        addPaymentRow(footerPanel, "Dibayar:", currencyFormat.format((Double) transaction.get("uang_bayar")), false);
        addPaymentRow(footerPanel, "Kembalian:", currencyFormat.format((Double) transaction.get("kembalian")), false);

        String metode = (String) transaction.get("metode");
        JLabel lblMetode = new JLabel("Metode: " + (metode != null ? metode : "Cash"));
        lblMetode.setFont(UIComponents.FONT_SMALL);
        lblMetode.setForeground(ColorScheme.ACCENT_GREEN);
        footerPanel.add(lblMetode, "wrap, gaptop 5");

        card.add(footerPanel, "growx");

        return card;
    }

    private void addPaymentRow(JPanel panel, String label, String value, boolean bold) {
        JPanel row = new JPanel(new MigLayout("insets 0", "[]20[]", "[]"));
        row.setOpaque(false);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(UIComponents.FONT_SMALL.deriveFont(bold ? Font.BOLD : Font.PLAIN));
        lblLabel.setForeground(bold ? ColorScheme.TEXT_PRIMARY : ColorScheme.TEXT_SECONDARY);
        row.add(lblLabel);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(UIComponents.FONT_SMALL.deriveFont(bold ? Font.BOLD : Font.PLAIN, bold ? 13f : 12f));
        lblValue.setForeground(bold ? ColorScheme.ACCENT_GREEN : ColorScheme.TEXT_PRIMARY);
        row.add(lblValue);

        panel.add(row, "wrap");
    }

    private JPanel createItemsTable(int transactionId, Connection conn) throws SQLException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Table model
        String[] columns = { "Menu", "Harga", "Qty", "Subtotal" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Load items
        String sql = "SELECT nama_menu, harga, qty, subtotal " +
                "FROM tbl_transaksi_detail " +
                "WHERE id_transaksi_header = ? " +
                "ORDER BY nama_menu";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[] {
                            rs.getString("nama_menu"),
                            currencyFormat.format(rs.getDouble("harga")),
                            rs.getInt("qty") + "x",
                            currencyFormat.format(rs.getDouble("subtotal"))
                    });
                }
            }
        }

        // Create table
        JTable table = new JTable(model);
        table.setFont(UIComponents.FONT_SMALL);
        table.setRowHeight(24);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(new Color(249, 250, 251));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(ColorScheme.TEXT_SECONDARY);
        header.setFont(UIComponents.FONT_SMALL.deriveFont(Font.BOLD));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BORDER_COLOR));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Right-align numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        });
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        panel.add(table.getTableHeader(), BorderLayout.NORTH);
        panel.add(table, BorderLayout.CENTER);

        return panel;
    }
}
