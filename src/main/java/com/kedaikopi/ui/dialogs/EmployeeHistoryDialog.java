package com.kedaikopi.ui.dialogs;

import com.kedaikopi.model.User;
import com.kedaikopi.model.UserActivityLog;
import com.kedaikopi.ui.components.UIComponents;
import com.kedaikopi.util.ColorScheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class EmployeeHistoryDialog extends JDialog {

    private JTabbedPane tabbedPane;
    private JPanel kasirPanel;
    private JPanel stockerPanel;
    private JComboBox<String> cmbMonth;
    private JSpinner spnYear;
    private NumberFormat currencyFormat = NumberFormat
            .getCurrencyInstance(new Locale.Builder().setLanguage("id").setRegion("ID").build());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    // Constants for payroll calculation
    private static final int WORK_DAYS_PER_MONTH = 26;
    private static final double LATE_PENALTY_PER_MINUTE = 1000.0;
    private static final double OVERTIME_BONUS_PER_HOUR = 15000.0;

    // Cache for table models to refresh easily
    private DefaultTableModel kasirModel;
    private DefaultTableModel stockerModel;

    public EmployeeHistoryDialog(Window owner) {
        super(owner, "Riwayat & Penggajian Karyawan", ModalityType.APPLICATION_MODAL);
        initComponents();
        setSize(1000, 700);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header with Filters
        JPanel headerPanel = new JPanel(new MigLayout("insets 20", "[]20[]10[]push[]", "[]"));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BORDER_COLOR));

        JLabel title = UIComponents.createLabel("Riwayat & Gaji Karyawan", UIComponents.LabelType.TITLE);

        // Month Filter
        String[] months = { "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember" };
        cmbMonth = new JComboBox<>(months);
        cmbMonth.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH));
        cmbMonth.setFont(UIComponents.FONT_BODY);

        // Year Filter
        spnYear = new JSpinner(new SpinnerNumberModel(Calendar.getInstance().get(Calendar.YEAR), 2020, 2030, 1));
        spnYear.setFont(UIComponents.FONT_BODY);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spnYear, "#");
        spnYear.setEditor(editor);

        JButton btnFilter = UIComponents.createButton("Tampilkan", UIComponents.ButtonType.PRIMARY);
        btnFilter.addActionListener(e -> loadData());

        JButton btnExport = UIComponents.createButton("Export Excel/CSV", UIComponents.ButtonType.SECONDARY);
        btnExport.addActionListener(e -> exportData());

        headerPanel.add(title);
        headerPanel.add(new JLabel("Periode:"));
        headerPanel.add(cmbMonth);
        headerPanel.add(spnYear);
        headerPanel.add(btnFilter);
        headerPanel.add(btnExport, "gapleft push"); // Push to right

        add(headerPanel, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIComponents.FONT_BODY_BOLD);

        kasirPanel = createRolePanel("Kasir");
        stockerPanel = createRolePanel("Stocker");

        tabbedPane.addTab("Kasir", kasirPanel);
        tabbedPane.addTab("Stocker", stockerPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Load initial data
        loadData();
    }

    private JPanel createRolePanel(String role) {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[grow]"));
        panel.setBackground(Color.WHITE);

        String[] columns = {
                "Nama Karyawan", "Tanggal", "Shift", "Login", "Logout",
                "Status", "Telat (mnt)", "Lembur (mnt)", "Penalti", "Bonus", "Total Harian"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (role.equals("Kasir"))
            kasirModel = model;
        else
            stockerModel = model;

        JTable table = UIComponents.createStyledTable(model);

        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Nama
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Tanggal
        table.getColumnModel().getColumn(5).setPreferredWidth(80); // Status
        table.getColumnModel().getColumn(10).setPreferredWidth(120); // Total

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        panel.add(scrollPane, "grow");

        // Summary footer could be added here

        return panel;
    }

    private void exportData() {
        JTable activeTable;
        String role;
        if (tabbedPane.getSelectedIndex() == 0) {
            activeTable = (JTable) ((JScrollPane) kasirPanel.getComponent(0)).getViewport().getView();
            role = "Kasir";
        } else {
            activeTable = (JTable) ((JScrollPane) stockerPanel.getComponent(0)).getViewport().getView();
            role = "Stocker";
        }

        if (activeTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk diexport.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Data Payroll");
        String defaultFileName = "Payroll_" + role + "_" + cmbMonth.getSelectedItem() + "_" + spnYear.getValue()
                + ".csv";
        fileChooser.setSelectedFile(new java.io.File(defaultFileName));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile())) {
                // Add BOM for Excel UTF-8 recognition
                writer.write('\uFEFF');

                // Write header
                StringBuilder header = new StringBuilder();
                for (int i = 0; i < activeTable.getColumnCount(); i++) {
                    header.append("\"").append(activeTable.getColumnName(i)).append("\"");
                    if (i < activeTable.getColumnCount() - 1)
                        header.append(";"); // Use semicolon for Excel Region ID
                }
                writer.println(header.toString());

                // Write data
                for (int i = 0; i < activeTable.getRowCount(); i++) {
                    StringBuilder row = new StringBuilder();
                    for (int j = 0; j < activeTable.getColumnCount(); j++) {
                        Object value = activeTable.getValueAt(i, j);
                        String valStr = value != null ? value.toString() : "";
                        // Escape quotes
                        valStr = valStr.replace("\"", "\"\"");
                        // Wrap in quotes
                        row.append("\"").append(valStr).append("\"");

                        if (j < activeTable.getColumnCount() - 1)
                            row.append(";"); // Use semicolon
                    }
                    writer.println(row.toString());
                }

                JOptionPane.showMessageDialog(this, "Data berhasil diexport ke Excel (CSV)!", "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal export data: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadData() {
        int year = (int) spnYear.getValue();
        int month = cmbMonth.getSelectedIndex();

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1, 0, 0, 0);
        Date startDate = new Date(cal.getTimeInMillis());

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = new Date(cal.getTimeInMillis());

        // Clear tables
        kasirModel.setRowCount(0);
        stockerModel.setRowCount(0);

        List<User> users = User.getAll();
        List<AttendanceRecord> allRecords = new ArrayList<>();

        for (User user : users) {
            if ("Owner".equals(user.getRole()))
                continue;

            List<UserActivityLog> logs = UserActivityLog.getByUser(user.getIdUser(), startDate, endDate);

            // Map logs by date
            Map<String, List<UserActivityLog>> logsByDate = new HashMap<>();

            for (UserActivityLog log : logs) {
                String dateKey = dateFormat.format(log.getActivityTime());
                logsByDate.putIfAbsent(dateKey, new ArrayList<>());
                logsByDate.get(dateKey).add(log);
            }

            for (Map.Entry<String, List<UserActivityLog>> entry : logsByDate.entrySet()) {
                List<UserActivityLog> dailyLogs = entry.getValue();
                dailyLogs.sort(Comparator.comparing(UserActivityLog::getActivityTime));

                // Process daily attendance and add to temporary list
                AttendanceRecord record = createDailyAttendanceRecord(user, entry.getKey(), dailyLogs);
                if (record != null) {
                    allRecords.add(record);
                }
            }
        }

        // Sort all records by login time (descending - newest first, or ascending based
        // on user pref. Let's do descending for logs usually, or ascending for daily
        // view. Let's do Ascending as per typical log view)
        // Actually for "Riwayat" usually newest first is better, but typical attendance
        // log is daily. Let's sort by Date then Time.
        allRecords.sort(Comparator.comparing(AttendanceRecord::getLoginTime));

        // Populate tables
        for (AttendanceRecord record : allRecords) {
            Object[] rowData = new Object[] {
                    record.getUserName(),
                    record.getDateStr(), // Use dateStr for "Tanggal" column
                    record.getShiftName(),
                    record.getLoginStr(),
                    record.getLogoutStr(),
                    record.getStatus(), // Use status for "Status" column
                    record.getLateMinutes() > 0 ? record.getLateMinutes() + " mnt" : "-", // "Telat (mnt)"
                    record.getOvertimeMinutes() > 0 ? record.getOvertimeMinutes() + " mnt" : "-", // "Lembur (mnt)"
                    record.getLateMinutes() > 0 ? currencyFormat.format(record.getPenalty()) : "-", // "Penalti"
                    record.getOvertimeMinutes() > 0 ? currencyFormat.format(record.getBonus()) : "-", // "Bonus"
                    currencyFormat.format(record.getTotalDaily()) // "Total Harian"
            };

            if ("Kasir".equals(record.getRole())) {
                kasirModel.addRow(rowData);
            } else if ("Stocker".equals(record.getRole())) {
                stockerModel.addRow(rowData);
            }
        }
    }

    // Helper class for sorting
    private static class AttendanceRecord {
        private String userName;
        private String role;
        private String dateStr; // Added for "Tanggal" column
        private String shiftName;
        private Timestamp loginTime;
        private String loginStr;
        private String logoutStr;
        private String status; // Added for "Status" column
        private long lateMinutes; // Added for "Telat (mnt)"
        private long overtimeMinutes; // Added for "Lembur (mnt)"
        private double penalty; // Added for "Penalti"
        private double bonus; // Added for "Bonus"
        private double totalDaily; // Added for "Total Harian"

        public AttendanceRecord(String userName, String role, String dateStr, String shiftName, Timestamp loginTime,
                String loginStr, String logoutStr, String status, long lateMinutes, long overtimeMinutes,
                double penalty, double bonus, double totalDaily) {
            this.userName = userName;
            this.role = role;
            this.dateStr = dateStr;
            this.shiftName = shiftName;
            this.loginTime = loginTime;
            this.loginStr = loginStr;
            this.logoutStr = logoutStr;
            this.status = status;
            this.lateMinutes = lateMinutes;
            this.overtimeMinutes = overtimeMinutes;
            this.penalty = penalty;
            this.bonus = bonus;
            this.totalDaily = totalDaily;
        }

        public Timestamp getLoginTime() {
            return loginTime;
        }

        public String getUserName() {
            return userName;
        }

        public String getRole() {
            return role;
        }

        public String getDateStr() {
            return dateStr;
        }

        public String getShiftName() {
            return shiftName;
        }

        public String getLoginStr() {
            return loginStr;
        }

        public String getLogoutStr() {
            return logoutStr;
        }

        public String getStatus() {
            return status;
        }

        public long getLateMinutes() {
            return lateMinutes;
        }

        public long getOvertimeMinutes() {
            return overtimeMinutes;
        }

        public double getPenalty() {
            return penalty;
        }

        public double getBonus() {
            return bonus;
        }

        public double getTotalDaily() {
            return totalDaily;
        }
    }

    private AttendanceRecord createDailyAttendanceRecord(User user, String dateStr, List<UserActivityLog> logs) {
        // Find Login and Logout
        Timestamp loginTime = null;
        Timestamp logoutTime = null;

        for (UserActivityLog log : logs) {
            if ("LOGIN".equals(log.getActivityType()) && loginTime == null) {
                loginTime = log.getActivityTime();
            } else if ("LOGOUT".equals(log.getActivityType())) {
                logoutTime = log.getActivityTime(); // Take the last logout
            }
        }

        if (loginTime == null)
            return null; // No login, skip

        // Determine Shift Logic
        String shiftName = "-";
        long lateMinutes = 0;
        long overtimeMinutes = 0;
        String status = "Tepat Waktu";

        // Get expected shift
        com.kedaikopi.model.Shift assignedShift = user.getAssignedShift();

        if (assignedShift != null) {
            shiftName = assignedShift.getShiftName();

            // Calculate Late Status
            Calendar shiftStart = Calendar.getInstance();
            shiftStart.setTime(assignedShift.getStartTime());

            Calendar loginCal = Calendar.getInstance();
            loginCal.setTime(loginTime);

            // Set same YMD for comparison
            Calendar targetStart = Calendar.getInstance();
            targetStart.setTime(loginTime);
            targetStart.set(Calendar.HOUR_OF_DAY, shiftStart.get(Calendar.HOUR_OF_DAY));
            targetStart.set(Calendar.MINUTE, shiftStart.get(Calendar.MINUTE));
            targetStart.set(Calendar.SECOND, 0);

            // Tolerance 15 mins
            Calendar tolerance = (Calendar) targetStart.clone();
            tolerance.add(Calendar.MINUTE, 15);

            if (loginCal.after(tolerance)) {
                long diff = loginCal.getTimeInMillis() - targetStart.getTimeInMillis();
                lateMinutes = diff / (60 * 1000);
                status = "Terlambat";
            }

            // Calculate Overtime (if logout after shift end)
            if (logoutTime != null) {
                Calendar shiftEnd = Calendar.getInstance();
                shiftEnd.setTime(assignedShift.getEndTime());

                Calendar targetEnd = Calendar.getInstance();
                targetEnd.setTime(logoutTime);
                targetEnd.set(Calendar.HOUR_OF_DAY, shiftEnd.get(Calendar.HOUR_OF_DAY));
                targetEnd.set(Calendar.MINUTE, shiftEnd.get(Calendar.MINUTE));

                Calendar actualLogout = Calendar.getInstance();
                actualLogout.setTime(logoutTime);

                if (actualLogout.after(targetEnd)) {
                    long diff = actualLogout.getTimeInMillis() - targetEnd.getTimeInMillis();
                    overtimeMinutes = diff / (60 * 1000);
                }
            }
        }

        // Calculate Pay
        double baseDaily = user.getBaseSalary() > 0 ? user.getBaseSalary() / WORK_DAYS_PER_MONTH : 0;
        double penalty = lateMinutes * LATE_PENALTY_PER_MINUTE;
        double bonus = (overtimeMinutes / 60.0) * OVERTIME_BONUS_PER_HOUR;
        double totalDaily = baseDaily - penalty + bonus;

        return new AttendanceRecord(
                user.getNamaLengkap(),
                user.getRole(),
                dateStr,
                shiftName,
                loginTime,
                timeFormat.format(loginTime),
                logoutTime != null ? timeFormat.format(logoutTime) : "-",
                status,
                lateMinutes,
                overtimeMinutes,
                penalty,
                bonus,
                totalDaily);
    }
}
