package com.kedaikopi.ui.panels;

import com.kedaikopi.model.Kategori;
import com.kedaikopi.model.User;
import com.kedaikopi.ui.components.UIComponents;
import com.kedaikopi.util.ColorScheme;
import com.kedaikopi.util.IconManager;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Kategori Panel - Manage product categories
 */
public class KategoriPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(KategoriPanel.class);
    private User currentUser;

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JTextField txtSearch;

    public KategoriPanel(User user) {
        this.currentUser = user;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]15[]15[grow]"));
        setBackground(ColorScheme.BG_LIGHT);

        // Title - text only
        JLabel title = UIComponents.createLabel("Manajemen Kategori", UIComponents.LabelType.TITLE);
        add(title, "wrap");

        // Toolbar
        JPanel toolbar = createToolbar();
        add(toolbar, "growx, wrap");

        // Table
        JPanel tablePanel = createTablePanel();
        add(tablePanel, "grow");
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new MigLayout("insets 0", "[]10[]10[]push[]10[]", "[]"));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        // Buttons - text only
        btnAdd = UIComponents.createButton("Tambah", UIComponents.ButtonType.SUCCESS);
        btnEdit = UIComponents.createButton("Edit", UIComponents.ButtonType.PRIMARY);
        btnDelete = UIComponents.createButton("Hapus", UIComponents.ButtonType.DANGER);
        btnRefresh = UIComponents.createButton("Refresh", UIComponents.ButtonType.SECONDARY);

        // Search
        txtSearch = UIComponents.createTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari kategori...");

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(txtSearch);
        toolbar.add(btnRefresh);

        // Listeners
        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteKategori());
        btnRefresh.addActionListener(e -> loadData());
        txtSearch.addActionListener(e -> searchKategori());

        return toolbar;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[grow]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1));

        // Table model
        String[] columns = { "ID", "Nama Kategori", "Icon", "Jumlah Menu", "Tanggal Dibuat" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = UIComponents.createStyledTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);

        // Smooth scrolling configuration
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);

        panel.add(scrollPane, "grow");

        return panel;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Kategori> categories = Kategori.getAll();

        for (Kategori kategori : categories) {
            // Count menu items in this category
            int menuCount = countMenuInCategory(kategori.getIdKategori());

            tableModel.addRow(new Object[] {
                    kategori.getIdKategori(),
                    kategori.getNamaKategori(),
                    kategori.getIconName() != null ? kategori.getIconName() : "-",
                    menuCount + " item",
                    kategori.getCreatedAt() != null ? kategori.getCreatedAt().toString().substring(0, 19) : "-"
            });
        }

        logger.info("Loaded {} categories", categories.size());
    }

    private int countMenuInCategory(int idKategori) {
        try (java.sql.Connection conn = com.kedaikopi.config.DatabaseConfig.getInstance().getConnection();
                java.sql.PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM tbl_menu WHERE id_kategori = ? AND is_active = TRUE")) {
            stmt.setInt(1, idKategori);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (java.sql.SQLException e) {
            logger.error("Error counting menu in category", e);
        }
        return 0;
    }

    private void showAddDialog() {
        KategoriDialog dialog = new KategoriDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Kategori kategori = dialog.getKategori();
            if (kategori.save()) {
                UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Kategori berhasil ditambahkan!");
                loadData();
            } else {
                UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Gagal menambahkan kategori!");
            }
        }
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Pilih kategori yang akan diedit!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        Kategori kategori = Kategori.getById(id);

        if (kategori != null) {
            KategoriDialog dialog = new KategoriDialog(SwingUtilities.getWindowAncestor(this), kategori);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                Kategori updated = dialog.getKategori();
                if (updated.save()) {
                    UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                            "Kategori berhasil diupdate!");
                    loadData();
                } else {
                    UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                            "Gagal mengupdate kategori!");
                }
            }
        }
    }

    private void deleteKategori() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Pilih kategori yang akan dihapus!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String nama = (String) tableModel.getValueAt(selectedRow, 1);

        // Check if category has menu items
        int menuCount = countMenuInCategory(id);
        if (menuCount > 0) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Kategori tidak dapat dihapus karena masih memiliki " + menuCount + " menu!");
            return;
        }

        boolean confirm = UIComponents.showConfirm((JFrame) SwingUtilities.getWindowAncestor(this),
                "Yakin ingin menghapus kategori '" + nama + "'?",
                "Konfirmasi Hapus");

        if (confirm) {
            Kategori kategori = Kategori.getById(id);
            if (kategori != null && kategori.delete()) {
                UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Kategori berhasil dihapus!");
                loadData();
            } else {
                UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Gagal menghapus kategori!");
            }
        }
    }

    private void searchKategori() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        tableModel.setRowCount(0);
        List<Kategori> categories = Kategori.getAll();

        for (Kategori kategori : categories) {
            if (kategori.getNamaKategori().toLowerCase().contains(keyword)) {
                int menuCount = countMenuInCategory(kategori.getIdKategori());
                tableModel.addRow(new Object[] {
                        kategori.getIdKategori(),
                        kategori.getNamaKategori(),
                        kategori.getIconName() != null ? kategori.getIconName() : "-",
                        menuCount + " item",
                        kategori.getCreatedAt() != null ? kategori.getCreatedAt().toString().substring(0, 19) : "-"
                });
            }
        }
    }
}

/**
 * Dialog for adding/editing categories
 */
class KategoriDialog extends JDialog {
    private Kategori kategori;
    private boolean confirmed = false;

    private JTextField txtNama;
    private JTextField txtIcon;

    public KategoriDialog(Window owner, Kategori kategori) {
        super(owner, kategori == null ? "Tambah Kategori" : "Edit Kategori", Dialog.ModalityType.APPLICATION_MODAL);
        this.kategori = kategori != null ? kategori : new Kategori();
        initComponents();
        if (kategori != null) {
            loadData();
        }
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[right]10[300!, grow]", "[]10[]20[]"));
        setBackground(Color.WHITE);

        // Fields
        add(new JLabel("Nama Kategori:"));
        txtNama = UIComponents.createTextField(20);
        add(txtNama, "growx, wrap");

        add(new JLabel("Icon Name:"));
        txtIcon = UIComponents.createTextField(20);
        txtIcon.putClientProperty("JTextField.placeholderText", "e.g., coffee, drink");
        add(txtIcon, "growx, wrap");

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "push[]10[]"));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnSave = UIComponents.createButton("Simpan", UIComponents.ButtonType.SUCCESS);
        JButton btnCancel = UIComponents.createButton("Batal", UIComponents.ButtonType.SECONDARY);

        btnSave.addActionListener(e -> saveKategori());
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        add(buttonPanel, "span 2, growx");

        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void loadData() {
        txtNama.setText(kategori.getNamaKategori());
        txtIcon.setText(kategori.getIconName());
    }

    private void saveKategori() {
        String nama = txtNama.getText().trim();

        if (nama.isEmpty()) {
            UIComponents.showError((JFrame) getOwner(), "Nama kategori tidak boleh kosong!");
            return;
        }

        kategori.setNamaKategori(nama);
        kategori.setIconName(txtIcon.getText().trim());

        confirmed = true;
        dispose();
    }

    public Kategori getKategori() {
        return kategori;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
