package com.kedaikopi.ui.panels;

import com.kedaikopi.model.MenuKopi;
import com.kedaikopi.model.Kategori;
import com.kedaikopi.model.User;
import com.kedaikopi.ui.components.UIComponents;
import com.kedaikopi.util.ColorScheme;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Inventaris Panel - Manage menu items and stock
 */
public class InventarisPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(InventarisPanel.class);
    User currentUser; // Package-private for access by StockAdjustmentDialog
    private NumberFormat currencyFormat;

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnStock;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilter;

    // Auto-refresh timer for real-time data
    private javax.swing.Timer autoRefreshTimer;

    public InventarisPanel(User user) {
        this.currentUser = user;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        initComponents();
        loadData();
        InventarisAutoRefresh.startAutoRefresh(this); // Auto-refresh every 2 minutes
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]15[]15[grow]"));
        setBackground(ColorScheme.BG_LIGHT);

        // Title - text only
        JLabel title = UIComponents.createLabel("Manajemen Inventaris Menu", UIComponents.LabelType.TITLE);
        add(title, "wrap");

        // Toolbar
        JPanel toolbar = createToolbar();
        add(toolbar, "growx, wrap");

        // Table
        JPanel tablePanel = createTablePanel();
        add(tablePanel, "grow");
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new MigLayout("insets 0", "[]10[]10[]10[]push[]10[]", "[]"));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        // Buttons - text only
        btnAdd = UIComponents.createButton("Tambah Menu", UIComponents.ButtonType.SUCCESS);
        btnEdit = UIComponents.createButton("Edit", UIComponents.ButtonType.PRIMARY);
        btnStock = UIComponents.createButton("Sesuaikan Stok", UIComponents.ButtonType.PRIMARY);
        btnDelete = UIComponents.createButton("Hapus", UIComponents.ButtonType.DANGER);

        // Search
        txtSearch = UIComponents.createTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari menu...");

        btnRefresh = UIComponents.createButton("Refresh", UIComponents.ButtonType.SECONDARY);

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnStock);
        toolbar.add(btnDelete);
        toolbar.add(txtSearch);
        toolbar.add(btnRefresh);

        // Filter - add below in a second row if needed, or remove for cleaner look
        cmbFilter = new JComboBox<>(new String[] { "Semua", "Stok Rendah (<10)", "Stok Habis (0)" });
        cmbFilter.setFont(UIComponents.FONT_BODY);

        // Listeners
        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnStock.addActionListener(e -> showStockDialog());
        btnDelete.addActionListener(e -> deleteMenu());
        btnRefresh.addActionListener(e -> loadData());
        txtSearch.addActionListener(e -> searchMenu());
        cmbFilter.addActionListener(e -> applyFilter());

        return toolbar;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[grow]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1));

        // Table model
        String[] columns = { "ID", "Nama Menu", "Kategori", "Harga", "Stok", "Status", "Aktif" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = UIComponents.createStyledTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);

        // Smooth scrolling configuration
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);

        panel.add(scrollPane, "grow");

        return panel;
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<MenuKopi> menus = MenuKopi.getAll();

        for (MenuKopi menu : menus) {
            String status;
            if (menu.getStok() == 0) {
                status = "HABIS";
            } else if (menu.getStok() < 10) {
                status = "RENDAH";
            } else {
                status = "AMAN";
            }

            tableModel.addRow(new Object[] {
                    menu.getIdMenu(),
                    menu.getNamaMenu(),
                    menu.getKategori().getNamaKategori(),
                    currencyFormat.format(menu.getHarga()),
                    menu.getStok(),
                    status,
                    menu.isActive() ? "Ya" : "Tidak"
            });
        }

        // Force table to update UI
        tableModel.fireTableDataChanged();
        table.revalidate();
        table.repaint();

        logger.info("Loaded {} menu items", menus.size());
    }

    private void applyFilter() {
        String filter = (String) cmbFilter.getSelectedItem();
        tableModel.setRowCount(0);
        List<MenuKopi> menus = MenuKopi.getAll();

        for (MenuKopi menu : menus) {
            boolean include = false;

            if ("Semua".equals(filter)) {
                include = true;
            } else if ("Stok Rendah (<10)".equals(filter) && menu.getStok() < 10 && menu.getStok() > 0) {
                include = true;
            } else if ("Stok Habis (0)".equals(filter) && menu.getStok() == 0) {
                include = true;
            }

            if (include) {
                String status = menu.getStok() == 0 ? "HABIS" : (menu.getStok() < 10 ? "RENDAH" : "AMAN");
                tableModel.addRow(new Object[] {
                        menu.getIdMenu(),
                        menu.getNamaMenu(),
                        menu.getKategori().getNamaKategori(),
                        currencyFormat.format(menu.getHarga()),
                        menu.getStok(),
                        status,
                        menu.isActive() ? "Ya" : "Tidak"
                });
            }
        }
    }

    private void searchMenu() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        tableModel.setRowCount(0);
        List<MenuKopi> menus = MenuKopi.getAll();

        for (MenuKopi menu : menus) {
            if (menu.getNamaMenu().toLowerCase().contains(keyword) ||
                    menu.getKategori().getNamaKategori().toLowerCase().contains(keyword)) {
                String status = menu.getStok() == 0 ? "HABIS" : (menu.getStok() < 10 ? "RENDAH" : "AMAN");
                tableModel.addRow(new Object[] {
                        menu.getIdMenu(),
                        menu.getNamaMenu(),
                        menu.getKategori().getNamaKategori(),
                        currencyFormat.format(menu.getHarga()),
                        menu.getStok(),
                        status,
                        menu.isActive() ? "Ya" : "Tidak"
                });
            }
        }
    }

    private void showAddDialog() {
        MenuDialog dialog = new MenuDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            MenuKopi menu = dialog.getMenu();
            if (menu.save()) {
                UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Menu berhasil ditambahkan!");
                loadData();
            } else {
                UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Gagal menambahkan menu!");
            }
        }
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Pilih menu yang akan diedit!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        MenuKopi menu = MenuKopi.getById(id);

        if (menu != null) {
            MenuDialog dialog = new MenuDialog(SwingUtilities.getWindowAncestor(this), menu);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                MenuKopi updated = dialog.getMenu();
                if (updated.save()) {
                    UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                            "Menu berhasil diupdate!");
                    loadData();
                } else {
                    UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                            "Gagal mengupdate menu!");
                }
            }
        }
    }

    private void showStockDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Pilih menu untuk menyesuaikan stok!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        MenuKopi menu = MenuKopi.getById(id);

        if (menu != null) {
            StockAdjustmentDialog dialog = new StockAdjustmentDialog(SwingUtilities.getWindowAncestor(this), menu);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                loadData();
                UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Stok berhasil diupdate!");
            }
        }
    }

    private void deleteMenu() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Pilih menu yang akan dihapus!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String nama = (String) tableModel.getValueAt(selectedRow, 1);

        boolean confirm = UIComponents.showConfirm((JFrame) SwingUtilities.getWindowAncestor(this),
                "Yakin ingin menghapus menu '" + nama + "'?\nData transaksi terkait akan tetap tersimpan.",
                "Konfirmasi Hapus");

        if (confirm) {
            MenuKopi menu = MenuKopi.getById(id);
            if (menu != null && menu.delete()) {
                UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Menu berhasil dihapus!");
                loadData();
            } else {
                UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Gagal menghapus menu!");
            }
        }
    }
}

/**
 * Dialog for adding/editing menu items
 */
class MenuDialog extends JDialog {
    private MenuKopi menu;
    private boolean confirmed = false;

    private JTextField txtNama, txtHarga, txtStok, txtImagePath, txtDeskripsi;
    private JComboBox<Kategori> cmbKategori;
    private JCheckBox chkActive;

    public MenuDialog(Window owner, MenuKopi menu) {
        super(owner, menu == null ? "Tambah Menu" : "Edit Menu", Dialog.ModalityType.APPLICATION_MODAL);
        this.menu = menu != null ? menu : new MenuKopi();
        initComponents();
        if (menu != null) {
            loadData();
        }
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[right]10[400!, grow]", "[]10[]10[]10[]10[]10[]20[]"));
        setBackground(Color.WHITE);

        // Fields
        add(new JLabel("Nama Menu:*"));
        txtNama = UIComponents.createTextField(30);
        add(txtNama, "growx, wrap");

        add(new JLabel("Kategori:*"));
        cmbKategori = new JComboBox<>();
        cmbKategori.setFont(UIComponents.FONT_BODY);
        loadKategoriData();
        add(cmbKategori, "growx, wrap");

        add(new JLabel("Harga:*"));
        txtHarga = UIComponents.createNumberField(15);
        add(txtHarga, "growx, wrap");

        add(new JLabel("Stok Awal:*"));
        txtStok = UIComponents.createNumberField(15);
        add(txtStok, "growx, wrap");

        add(new JLabel("Gambar Path:"));
        txtImagePath = UIComponents.createTextField(30);
        txtImagePath.putClientProperty("JTextField.placeholderText", "Opsional");
        add(txtImagePath, "growx, wrap");

        add(new JLabel("Deskripsi:"));
        txtDeskripsi = UIComponents.createTextField(30);
        txtDeskripsi.putClientProperty("JTextField.placeholderText", "Opsional");
        add(txtDeskripsi, "growx, wrap");

        add(new JLabel(""));
        chkActive = new JCheckBox("Menu Aktif");
        chkActive.setSelected(true);
        chkActive.setFont(UIComponents.FONT_BODY);
        chkActive.setBackground(Color.WHITE);
        add(chkActive, "wrap");

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "push[]10[]"));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnSave = UIComponents.createButton("Simpan", UIComponents.ButtonType.SUCCESS);
        JButton btnCancel = UIComponents.createButton("Batal", UIComponents.ButtonType.SECONDARY);

        btnSave.addActionListener(e -> saveMenu());
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        add(buttonPanel, "span 2, growx");

        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void loadKategoriData() {
        List<Kategori> categories = Kategori.getAll();
        for (Kategori k : categories) {
            cmbKategori.addItem(k);
        }
    }

    public void loadData() {
        txtNama.setText(menu.getNamaMenu());
        txtHarga.setText(String.valueOf((int) menu.getHarga()));
        txtStok.setText(String.valueOf(menu.getStok()));
        txtImagePath.setText(menu.getImagePath());
        txtDeskripsi.setText(menu.getDeskripsi());
        chkActive.setSelected(menu.isActive());

        // Select kategori
        for (int i = 0; i < cmbKategori.getItemCount(); i++) {
            if (cmbKategori.getItemAt(i).getIdKategori() == menu.getKategori().getIdKategori()) {
                cmbKategori.setSelectedIndex(i);
                break;
            }
        }
    }

    private void saveMenu() {
        String nama = txtNama.getText().trim();
        String hargaStr = txtHarga.getText().trim();
        String stokStr = txtStok.getText().trim();

        if (nama.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
            UIComponents.showError((JFrame) getOwner(), "Nama, harga, dan stok wajib diisi!");
            return;
        }

        if (cmbKategori.getSelectedItem() == null) {
            UIComponents.showError((JFrame) getOwner(), "Pilih kategori!");
            return;
        }

        try {
            double harga = Double.parseDouble(hargaStr);
            int stok = Integer.parseInt(stokStr);

            if (harga <= 0 || stok < 0) {
                UIComponents.showError((JFrame) getOwner(), "Harga harus > 0 dan stok >= 0!");
                return;
            }

            menu.setNamaMenu(nama);
            menu.setKategori((Kategori) cmbKategori.getSelectedItem());
            menu.setHarga(harga);
            menu.setStok(stok);
            menu.setImagePath(txtImagePath.getText().trim());
            menu.setDeskripsi(txtDeskripsi.getText().trim());
            menu.setActive(chkActive.isSelected());

            confirmed = true;
            dispose();

        } catch (NumberFormatException e) {
            UIComponents.showError((JFrame) getOwner(), "Harga dan stok harus berupa angka!");
        }
    }

    public MenuKopi getMenu() {
        return menu;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}

/**
 * Dialog for stock adjustment
 */
class StockAdjustmentDialog extends JDialog {
    private MenuKopi menu;
    private boolean confirmed = false;

    private JLabel lblCurrentStock;
    private JTextField txtAdjustment;
    private JRadioButton rbAdd, rbSubtract;

    public StockAdjustmentDialog(Window owner, MenuKopi menu) {
        super(owner, "Sesuaikan Stok", Dialog.ModalityType.APPLICATION_MODAL);
        this.menu = menu;
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[right]10[300!, grow]", "[]10[]10[]10[]20[]"));
        setBackground(Color.WHITE);

        // Current stock
        add(new JLabel("Menu:"));
        add(new JLabel(menu.getNamaMenu()), "wrap");

        add(new JLabel("Stok Sekarang:"));
        lblCurrentStock = new JLabel(String.valueOf(menu.getStok()) + " unit");
        lblCurrentStock.setFont(UIComponents.FONT_BODY_BOLD);
        add(lblCurrentStock, "wrap");

        // Operation type
        add(new JLabel("Operasi:"));
        JPanel opPanel = new JPanel(new MigLayout("insets 0", "[]20[]"));
        opPanel.setBackground(Color.WHITE);
        rbAdd = new JRadioButton("Tambah Stok");
        rbSubtract = new JRadioButton("Kurangi Stok");
        rbAdd.setSelected(true);
        rbAdd.setBackground(Color.WHITE);
        rbSubtract.setBackground(Color.WHITE);
        rbAdd.setFont(UIComponents.FONT_BODY);
        rbSubtract.setFont(UIComponents.FONT_BODY);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbAdd);
        bg.add(rbSubtract);
        opPanel.add(rbAdd);
        opPanel.add(rbSubtract);
        add(opPanel, "wrap");

        // Adjustment amount
        add(new JLabel("Jumlah:*"));
        txtAdjustment = UIComponents.createNumberField(15);
        add(txtAdjustment, "growx, wrap");

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "push[]10[]"));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnSave = UIComponents.createButton("Simpan", UIComponents.ButtonType.SUCCESS);
        JButton btnCancel = UIComponents.createButton("Batal", UIComponents.ButtonType.SECONDARY);

        btnSave.addActionListener(e -> saveAdjustment());
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        add(buttonPanel, "span 2, growx");

        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void saveAdjustment() {
        String amountStr = txtAdjustment.getText().trim();

        if (amountStr.isEmpty()) {
            UIComponents.showError((JFrame) getOwner(), "Masukkan jumlah penyesuaian!");
            return;
        }

        try {
            int amount = Integer.parseInt(amountStr);

            if (amount < 0) {
                UIComponents.showError((JFrame) getOwner(), "Jumlah harus positif!");
                return;
            }

            int oldStock = menu.getStok();
            int newStock;
            boolean isAddition = rbAdd.isSelected();

            if (isAddition) {
                newStock = oldStock + amount;
            } else {
                newStock = oldStock - amount;
                if (newStock < 0) {
                    UIComponents.showError((JFrame) getOwner(), "Stok tidak boleh kurang dari 0!");
                    return;
                }
            }

            menu.setStok(newStock);
            if (menu.save()) {
                // If adding stock, record to restock history
                if (isAddition) {
                    try (java.sql.Connection conn = com.kedaikopi.config.DatabaseConfig.getInstance().getConnection()) {
                        String sql = "INSERT INTO tbl_restock_history " +
                                "(id_menu, qty_before, qty_added, qty_after, id_user, notes, created_at) " +
                                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

                        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setInt(1, menu.getIdMenu());
                            stmt.setInt(2, oldStock);
                            stmt.setInt(3, amount);
                            stmt.setInt(4, newStock);

                            // Get current user from parent InventarisPanel
                            User currentUser = getCurrentUser();
                            int userId = 1; // Default fallback
                            if (currentUser != null) {
                                userId = currentUser.getIdUser();
                            } else {
                                // Get first available user ID from database as fallback
                                try (java.sql.ResultSet rs = conn.createStatement()
                                        .executeQuery("SELECT MIN(id_user) FROM tbl_user")) {
                                    if (rs.next()) {
                                        userId = rs.getInt(1);
                                    }
                                } catch (Exception e) {
                                    org.slf4j.LoggerFactory.getLogger(StockAdjustmentDialog.class)
                                            .warn("Could not get fallback user ID: {}", e.getMessage());
                                }
                            }
                            stmt.setInt(5, userId);
                            stmt.setString(6, "Penambahan stok manual");

                            int rowsAffected = stmt.executeUpdate();
                            org.slf4j.LoggerFactory.getLogger(StockAdjustmentDialog.class)
                                    .info("Restock history saved: menu_id={}, qty={}, rows={}",
                                            menu.getIdMenu(), amount, rowsAffected);
                        }
                    } catch (Exception ex) {
                        org.slf4j.LoggerFactory.getLogger(StockAdjustmentDialog.class)
                                .error("Failed to record restock history for menu_id={}: {}",
                                        menu.getIdMenu(), ex.getMessage(), ex);
                        // Show error to user
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Peringatan: Stok berhasil diubah tetapi riwayat gagal dicatat.\n" + ex.getMessage(),
                                "Warning",
                                javax.swing.JOptionPane.WARNING_MESSAGE);
                    }
                }

                confirmed = true;
                dispose();
            } else {
                UIComponents.showError((JFrame) getOwner(), "Gagal menyimpan perubahan stok!");
            }

        } catch (NumberFormatException e) {
            UIComponents.showError((JFrame) getOwner(), "Jumlah harus berupa angka!");
        }
    }

    private User getCurrentUser() {
        // Try to get user from parent InventarisPanel
        try {
            Window owner = getOwner();
            if (owner instanceof JFrame) {
                JFrame frame = (JFrame) owner;
                java.awt.Component[] components = frame.getContentPane().getComponents();
                for (java.awt.Component comp : components) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;
                        // Search for InventarisPanel
                        for (java.awt.Component child : panel.getComponents()) {
                            if (child instanceof InventarisPanel) {
                                InventarisPanel invPanel = (InventarisPanel) child;
                                return invPanel.currentUser;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Fallback: return null
        }
        return null;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}

/**
 * Auto-refresh methods for InventarisPanel
 */
class InventarisAutoRefresh {

    /**
     * Start auto-refresh timer for Inventaris panel (2 minutes)
     * This keeps stock data up-to-date without user intervention
     */
    static void startAutoRefresh(InventarisPanel panel) {
        // Auto-refresh every 2 minutes (120,000 ms)
        javax.swing.Timer timer = new javax.swing.Timer(120000, e -> {
            SwingUtilities.invokeLater(() -> {
                panel.loadData();
                org.slf4j.LoggerFactory.getLogger(InventarisPanel.class)
                        .info("Auto-refreshed Inventaris data");
            });
        });
        timer.start();

        // Store timer reference in panel for cleanup
        try {
            java.lang.reflect.Field field = InventarisPanel.class.getDeclaredField("autoRefreshTimer");
            field.setAccessible(true);
            field.set(panel, timer);
        } catch (Exception ex) {
            // Ignore reflection error
        }
    }
}
