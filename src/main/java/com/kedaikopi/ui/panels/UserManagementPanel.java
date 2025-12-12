package com.kedaikopi.ui.panels;

import com.kedaikopi.model.User;
import com.kedaikopi.ui.components.UIComponents;
import com.kedaikopi.util.ColorScheme;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * User Management Panel - Manage system users (Owner only)
 */
public class UserManagementPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementPanel.class);
    private User currentUser;

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnToggleActive, btnResetPassword, btnRefresh;
    private JTextField txtSearch;

    public UserManagementPanel(User user) {
        this.currentUser = user;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]15[]15[grow]"));
        setBackground(ColorScheme.BG_LIGHT);

        // Title
        JLabel title = UIComponents.createLabel("Manajemen User", UIComponents.LabelType.TITLE);
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

        // Buttons
        btnAdd = UIComponents.createButton("Tambah User", UIComponents.ButtonType.SUCCESS);
        btnEdit = UIComponents.createButton("Edit", UIComponents.ButtonType.PRIMARY);
        btnDelete = UIComponents.createButton("Hapus", UIComponents.ButtonType.DANGER);
        btnToggleActive = UIComponents.createButton("Toggle Aktif", UIComponents.ButtonType.SECONDARY);
        btnResetPassword = UIComponents.createButton("Reset Password", UIComponents.ButtonType.SECONDARY);

        // Search
        txtSearch = UIComponents.createTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari user...");

        btnRefresh = UIComponents.createButton("Refresh", UIComponents.ButtonType.SECONDARY);

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnToggleActive);
        toolbar.add(btnResetPassword);
        toolbar.add(txtSearch);
        toolbar.add(btnRefresh);

        // Listeners
        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteUser());
        btnToggleActive.addActionListener(e -> toggleActiveStatus());
        btnResetPassword.addActionListener(e -> resetPassword());
        btnRefresh.addActionListener(e -> loadData());
        txtSearch.addActionListener(e -> searchUser());

        return toolbar;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[grow]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1));

        // Table model - Now displaying Username column
        String[] columns = { "ID", "Username", "Role", "Nama Lengkap", "Aktif", "Dibuat" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = UIComponents.createStyledTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(130); // Username (auto-generated)
        table.getColumnModel().getColumn(2).setPreferredWidth(90); // Role
        table.getColumnModel().getColumn(3).setPreferredWidth(200); // Nama Lengkap
        table.getColumnModel().getColumn(4).setPreferredWidth(70); // Aktif
        table.getColumnModel().getColumn(5).setPreferredWidth(180); // Dibuat

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
        List<User> users = User.getAll();

        for (User user : users) {
            // Column order: ID, Username, Role, Nama Lengkap, Aktif, Dibuat
            tableModel.addRow(new Object[] {
                    user.getIdUser(),
                    user.getUsername(), // Show auto-generated username
                    user.getRole(),
                    user.getNamaLengkap(),
                    user.isActive() ? "Ya" : "Tidak",
                    user.getCreatedAt() != null ? user.getCreatedAt().toString().substring(0, 19) : "-"
            });
        }

        logger.info("Loaded {} users", users.size());
    }

    private void searchUser() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        tableModel.setRowCount(0);
        List<User> users = User.getAll();

        for (User user : users) {
            if (user.getUsername().toLowerCase().contains(keyword) ||
                    user.getNamaLengkap().toLowerCase().contains(keyword) ||
                    user.getRole().toLowerCase().contains(keyword)) {
                // Column order: ID, Username, Role, Nama Lengkap, Aktif, Dibuat
                tableModel.addRow(new Object[] {
                        user.getIdUser(),
                        user.getUsername(), // Show auto-generated username
                        user.getRole(),
                        user.getNamaLengkap(),
                        user.isActive() ? "Ya" : "Tidak",
                        user.getCreatedAt() != null ? user.getCreatedAt().toString().substring(0, 19) : "-"
                });
            }
        }
    }

    private void showAddDialog() {
        UserDialog dialog = new UserDialog(SwingUtilities.getWindowAncestor(this), null, currentUser);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            User user = dialog.getUser();
            if (user.save()) {
                UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                        "User berhasil ditambahkan!");
                loadData();
            } else {
                UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Gagal menambahkan user!");
            }
        }
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Pilih user yang akan diedit!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        User user = User.getById(id);

        if (user != null) {
            UserDialog dialog = new UserDialog(SwingUtilities.getWindowAncestor(this), user, currentUser);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                User updated = dialog.getUser();
                boolean success = true;

                // Save user data (username, role, nama_lengkap, is_active)
                if (updated.save()) {
                    // If password was changed in dialog, update it separately using
                    // changePassword()
                    if (dialog.isPasswordChanged()) {
                        String newPassword = updated.getPassword();
                        success = updated.changePassword(newPassword);
                        if (!success) {
                            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                                    "User berhasil diupdate, tapi gagal mengubah password!");
                            logger.error("Failed to update password for user: {}", updated.getUsername());
                        } else {
                            logger.info("Password updated for user: {}", updated.getUsername());
                        }
                    }

                    if (success) {
                        UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                                "User berhasil diupdate!");
                        logger.info("User updated successfully: {}", updated.getUsername());
                    }
                    loadData();
                } else {
                    UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                            "Gagal mengupdate user!");
                    logger.error("Failed to update user: {}", updated.getUsername());
                }
            }
        }
    }

    private void deleteUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Pilih user yang akan dihapus!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        User user = User.getById(id);

        if (user != null) {
            // Prevent deleting Owner users for security
            if (user.getRole().equals("Owner")) {
                UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Tidak dapat menghapus user dengan role Owner!");
                logger.warn("Attempted to delete Owner user: {}", user.getUsername());
                return;
            }

            // Prevent self-deletion
            if (user.getIdUser() == currentUser.getIdUser()) {
                UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Tidak dapat menghapus akun sendiri!");
                return;
            }

            boolean confirm = UIComponents.showConfirm((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Yakin ingin menghapus user '" + user.getNamaLengkap() + "'?\nAksi ini tidak dapat dibatalkan!",
                    "Konfirmasi Hapus");

            if (confirm) {
                if (user.delete()) {
                    UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                            "User berhasil dihapus!");
                    logger.info("User deleted: {} by {}", user.getUsername(), currentUser.getUsername());
                    loadData();
                } else {
                    UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                            "Gagal menghapus user!");
                    logger.error("Failed to delete user: {}", user.getUsername());
                }
            }
        }
    }

    private void toggleActiveStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Pilih user untuk toggle status!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1); // Now Username is column 1

        // Prevent disabling self
        if (id == currentUser.getIdUser()) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Tidak dapat menonaktifkan akun sendiri!");
            return;
        }

        User user = User.getById(id);
        if (user != null) {
            user.setActive(!user.isActive());
            if (user.save()) {
                String status = user.isActive() ? "diaktifkan" : "dinonaktifkan";
                UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                        "User '" + username + "' berhasil " + status + "!");
                loadData();
            } else {
                UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Gagal mengubah status user!");
            }
        }
    }

    private void resetPassword() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Pilih user untuk reset password!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1); // Now Username is column 1

        boolean confirm = UIComponents.showConfirm((JFrame) SwingUtilities.getWindowAncestor(this),
                "Reset password untuk user '" + username + "'?\nPassword akan di-reset ke 'admin123'",
                "Konfirmasi Reset Password");

        if (confirm) {
            User user = User.getById(id);
            if (user != null) {
                // Use changePassword() method to properly update password in database
                if (user.changePassword("admin123")) {
                    UIComponents.showSuccess((JFrame) SwingUtilities.getWindowAncestor(this),
                            "Password berhasil di-reset ke 'admin123'");
                    logger.info("Password reset for user: {}", username);
                } else {
                    UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                            "Gagal reset password!");
                    logger.error("Failed to reset password for user: {}", username);
                }
            }
        }
    }
}

/**
 * Dialog for adding/editing users
 */
class UserDialog extends JDialog {
    private User user;
    private User currentUser;
    private boolean confirmed = false;
    private boolean isEditMode;
    private boolean passwordChanged = false; // Track if password was changed

    private JTextField txtUsername;
    private JTextField txtNamaLengkap;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JCheckBox chkActive;

    public UserDialog(Window owner, User user, User currentUser) {
        super(owner, user == null ? "Tambah User" : "Edit User", Dialog.ModalityType.APPLICATION_MODAL);
        this.user = user != null ? user : new User();
        this.currentUser = currentUser;
        this.isEditMode = user != null;
        initComponents();
        if (user != null) {
            loadData();
        }
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[right]10[400!, grow]", "[]10[]10[]10[]10[]10[]20[]"));
        setBackground(Color.WHITE);

        // Username field - editable for both add and edit modes
        add(new JLabel("Username:*"));
        txtUsername = UIComponents.createTextField(30);
        if (isEditMode) {
            // In edit mode, show current username (editable)
            txtUsername.setToolTipText("Username dapat diubah");
        } else {
            // In add mode, will auto-generate from nama lengkap but can be edited
            txtUsername.setToolTipText("Auto-generate dari nama lengkap, dapat diubah manual");
        }
        add(txtUsername, "growx, wrap");

        // Nama Lengkap field
        add(new JLabel("Nama Lengkap:*"));
        txtNamaLengkap = UIComponents.createTextField(30);

        // Auto-generate username when typing nama lengkap (works in both add and edit
        // modes)
        txtNamaLengkap.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                // Only auto-fill if username field is empty or was auto-generated
                String currentUsername = txtUsername.getText().trim();
                if (currentUsername.isEmpty() || isAutoGenerated(currentUsername)) {
                    String namaLengkap = txtNamaLengkap.getText().trim();
                    if (!namaLengkap.isEmpty()) {
                        String autoUsername = generateUsernameFromName(namaLengkap);
                        txtUsername.setText(autoUsername);
                    }
                }
            }
        });
        add(txtNamaLengkap, "growx, wrap");

        add(new JLabel("Password:*"));
        txtPassword = new JPasswordField();
        txtPassword.setFont(UIComponents.FONT_BODY);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        if (isEditMode) {
            txtPassword.putClientProperty("JTextField.placeholderText", "Kosongkan jika tidak ingin mengubah");
        }
        add(txtPassword, "growx, wrap");

        add(new JLabel("Role:*"));
        // Restrict role selection: Only Kasir and Stocker for new users or non-Owner
        // edits
        if (isEditMode && user.getRole().equals("Owner")) {
            // If editing an existing Owner, include Owner in dropdown
            cmbRole = new JComboBox<>(new String[] { "Owner", "Kasir", "Stocker" });
        } else {
            // For new users or editing Kasir/Stocker, only allow Kasir and Stocker
            cmbRole = new JComboBox<>(new String[] { "Kasir", "Stocker" });
        }
        cmbRole.setFont(UIComponents.FONT_BODY);
        add(cmbRole, "growx, wrap");

        add(new JLabel(""));
        chkActive = new JCheckBox("User Aktif");
        chkActive.setSelected(true);
        chkActive.setFont(UIComponents.FONT_BODY);
        chkActive.setBackground(Color.WHITE);
        add(chkActive, "wrap");

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "push[]10[]"));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnSave = UIComponents.createButton("Simpan", UIComponents.ButtonType.SUCCESS);
        JButton btnCancel = UIComponents.createButton("Batal", UIComponents.ButtonType.SECONDARY);

        btnSave.addActionListener(e -> saveUser());
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        add(buttonPanel, "span 2, growx");

        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void loadData() {
        // Load existing user data into fields
        txtUsername.setText(user.getUsername());
        txtNamaLengkap.setText(user.getNamaLengkap());
        cmbRole.setSelectedItem(user.getRole());
        chkActive.setSelected(user.isActive());
    }

    private void saveUser() {
        String username = txtUsername.getText().trim();
        String namaLengkap = txtNamaLengkap.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // Validation
        if (username.isEmpty()) {
            UIComponents.showError((JFrame) getOwner(), "Username wajib diisi!");
            txtUsername.requestFocus();
            return;
        }

        if (namaLengkap.isEmpty()) {
            UIComponents.showError((JFrame) getOwner(), "Nama lengkap wajib diisi!");
            txtNamaLengkap.requestFocus();
            return;
        }

        if (!isEditMode && password.isEmpty()) {
            UIComponents.showError((JFrame) getOwner(), "Password wajib diisi untuk user baru!");
            txtPassword.requestFocus();
            return;
        }

        // Validate username format (lowercase alphanumeric only)
        if (!username.matches("^[a-z0-9]+$")) {
            UIComponents.showError((JFrame) getOwner(),
                    "Username hanya boleh berisi huruf kecil dan angka (tanpa spasi atau karakter khusus)!");
            txtUsername.requestFocus();
            return;
        }

        // Check for duplicate username
        if (isEditMode) {
            // In edit mode, check if username changed and if new username already exists
            if (!username.equals(user.getUsername()) && usernameExists(username)) {
                UIComponents.showError((JFrame) getOwner(),
                        "Username '" + username + "' sudah digunakan! Silakan pilih username lain.");
                txtUsername.requestFocus();
                return;
            }
        } else {
            // In add mode, just check if username exists
            if (usernameExists(username)) {
                UIComponents.showError((JFrame) getOwner(),
                        "Username '" + username + "' sudah digunakan! Silakan pilih username lain.");
                txtUsername.requestFocus();
                return;
            }
        }

        // Set user data
        user.setUsername(username);
        user.setNamaLengkap(namaLengkap);

        // Track if password was changed in edit mode
        if (!password.isEmpty()) {
            user.setPassword(password); // Store plain password temporarily
            passwordChanged = true;
        } else {
            passwordChanged = false;
        }

        user.setRole((String) cmbRole.getSelectedItem());
        user.setActive(chkActive.isSelected());

        confirmed = true;
        dispose();
    }

    /**
     * Check if the given string looks like an auto-generated username
     * Used to determine if we should overwrite username field when typing nama
     * lengkap
     */
    private boolean isAutoGenerated(String username) {
        // Simple heuristic: if it's all lowercase with no spaces/special chars
        return username.matches("^[a-z0-9]+$");
    }

    /**
     * Generate username from nama lengkap
     * Format: lowercase with no spaces
     * Example: "Budi Santoso" -> "budisantoso"
     * Handle duplicates by adding numbers: "budisantoso1", "budisantoso2", etc.
     */
    private String generateUsernameFromName(String namaLengkap) {
        // Base username: lowercase, remove spaces and special chars
        String baseUsername = namaLengkap.toLowerCase()
                .replaceAll("\\s+", "") // Remove all whitespace
                .replaceAll("[^a-z0-9]", ""); // Remove special characters

        // Check if username already exists
        String username = baseUsername;
        int counter = 1;

        while (usernameExists(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    /**
     * Check if username already exists in database
     * In edit mode, excludes the current user being edited
     */
    private boolean usernameExists(String username) {
        List<User> allUsers = User.getAll();
        for (User u : allUsers) {
            // Skip the current user when checking in edit mode
            if (isEditMode && u.getIdUser() == user.getIdUser()) {
                continue;
            }
            if (u.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public User getUser() {
        return user;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }
}
