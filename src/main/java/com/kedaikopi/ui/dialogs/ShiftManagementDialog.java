package com.kedaikopi.ui.dialogs;

import com.kedaikopi.model.Shift;
import com.kedaikopi.model.User;
import com.kedaikopi.util.ColorScheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Time;
import java.text.SimpleDateFormat;

/**
 * Shift Management Dialog
 * Create, edit, and delete work shifts
 */
public class ShiftManagementDialog extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public ShiftManagementDialog(Window parent, User currentUser) {
        super(parent, "Manajemen Shift Kerja", ModalityType.APPLICATION_MODAL);
        // currentUser parameter for future use (role-based features)
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 15", "[grow]", "[][grow][]"));

        // Header
        JLabel lblTitle = new JLabel("Manajemen Shift Kerja");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(lblTitle, "wrap");

        // Table
        String[] columns = { "ID", "Nama Shift", "Jam Mulai", "Jam Selesai", "Warna" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(ColorScheme.PRIMARY_LIGHT);
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, "grow, wrap");

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("fillx", "push[]10[]10[]10[]", ""));

        JButton btnAdd = createButton("Tambah Shift", new Color(76, 175, 80));
        btnAdd.addActionListener(e -> showAddDialog());

        JButton btnEdit = createButton("Edit", ColorScheme.ACCENT_BLUE);
        btnEdit.addActionListener(e -> showEditDialog());

        JButton btnDelete = createButton("Hapus", ColorScheme.BUTTON_DANGER);
        btnDelete.addActionListener(e -> deleteShift());

        JButton btnClose = createButton("Tutup", new Color(240, 240, 240), Color.DARK_GRAY);
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClose);

        add(buttonPanel, "growx");

        setSize(700, 450);
        setLocationRelativeTo(getParent());
    }

    private JButton createButton(String text, Color bg) {
        return createButton(text, bg, Color.WHITE);
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadData() {
        tableModel.setRowCount(0);

        for (Shift shift : Shift.getAllShifts()) {
            tableModel.addRow(new Object[] {
                    shift.getIdShift(),
                    shift.getShiftName(),
                    timeFormat.format(shift.getStartTime()),
                    timeFormat.format(shift.getEndTime()),
                    shift.getColorCode()
            });
        }
    }

    private void showAddDialog() {
        ShiftFormDialog dialog = new ShiftFormDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Pilih shift yang akan diedit!",
                    "Perhatian",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idShift = (int) tableModel.getValueAt(selectedRow, 0);
        Shift shift = Shift.getById(idShift);

        ShiftFormDialog dialog = new ShiftFormDialog(this, shift);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void deleteShift() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Pilih shift yang akan dihapus!",
                    "Perhatian",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus shift ini?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int idShift = (int) tableModel.getValueAt(selectedRow, 0);
            Shift shift = Shift.getById(idShift);

            if (shift != null && shift.delete()) {
                JOptionPane.showMessageDialog(this,
                        "Shift berhasil dihapus!",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Gagal menghapus shift!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Inner dialog for add/edit shift
     */
    private class ShiftFormDialog extends JDialog {
        private Shift shift;
        private boolean saved = false;

        private JTextField txtName;
        private JSpinner spnStartHour, spnStartMin, spnEndHour, spnEndMin;
        private JTextField txtColor;

        public ShiftFormDialog(Dialog parent, Shift shift) {
            super(parent, shift == null ? "Tambah Shift" : "Edit Shift", true);
            this.shift = shift;
            initForm();
        }

        private void initForm() {
            setLayout(new MigLayout("fill, insets 20", "[100][grow]", ""));

            // Nama Shift
            add(new JLabel("Nama Shift:"));
            txtName = new JTextField(shift != null ? shift.getShiftName() : "");
            add(txtName, "growx, wrap");

            // Jam Mulai
            add(new JLabel("Jam Mulai:"));
            JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            spnStartHour = new JSpinner(new SpinnerNumberModel(7, 0, 23, 1));
            spnStartMin = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
            startPanel.add(spnStartHour);
            startPanel.add(new JLabel(":"));
            startPanel.add(spnStartMin);
            add(startPanel, "wrap");

            // Jam Selesai
            add(new JLabel("Jam Selesai:"));
            JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            spnEndHour = new JSpinner(new SpinnerNumberModel(15, 0, 23, 1));
            spnEndMin = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
            endPanel.add(spnEndHour);
            endPanel.add(new JLabel(":"));
            endPanel.add(spnEndMin);
            add(endPanel, "wrap");

            // Warna
            add(new JLabel("Kode Warna:"));
            txtColor = new JTextField(shift != null ? shift.getColorCode() : "#3498db");
            add(txtColor, "growx, wrap");

            // Load existing data
            if (shift != null) {
                Time start = shift.getStartTime();
                Time end = shift.getEndTime();
                spnStartHour.setValue(start.toLocalTime().getHour());
                spnStartMin.setValue(start.toLocalTime().getMinute());
                spnEndHour.setValue(end.toLocalTime().getHour());
                spnEndMin.setValue(end.toLocalTime().getMinute());
            }

            // Buttons
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnSave = new JButton("Simpan");
            btnSave.addActionListener(e -> save());
            JButton btnCancel = new JButton("Batal");
            btnCancel.addActionListener(e -> dispose());

            btnPanel.add(btnSave);
            btnPanel.add(btnCancel);
            add(btnPanel, "span, alignx right, wrap");

            pack();
            setLocationRelativeTo(getParent());
        }

        private void save() {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama shift harus diisi!");
                return;
            }

            int startHour = (int) spnStartHour.getValue();
            int startMin = (int) spnStartMin.getValue();
            int endHour = (int) spnEndHour.getValue();
            int endMin = (int) spnEndMin.getValue();

            Time startTime = Time.valueOf(String.format("%02d:%02d:00", startHour, startMin));
            Time endTime = Time.valueOf(String.format("%02d:%02d:00", endHour, endMin));

            if (shift == null) {
                shift = new Shift();
            }

            shift.setShiftName(name);
            shift.setStartTime(startTime);
            shift.setEndTime(endTime);
            shift.setColorCode(txtColor.getText().trim());

            boolean success = shift.getIdShift() > 0 ? shift.update() : shift.save();

            if (success) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan shift!");
            }
        }

        public boolean isSaved() {
            return saved;
        }
    }
}
