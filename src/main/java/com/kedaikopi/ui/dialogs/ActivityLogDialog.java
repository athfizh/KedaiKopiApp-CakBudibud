package com.kedaikopi.ui.dialogs;

import com.kedaikopi.model.User;
import com.kedaikopi.model.UserActivityLog;
import com.kedaikopi.util.ColorScheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Enhanced Activity Log Dialog with date picker, login/logout times, and shift
 * detection
 */
public class ActivityLogDialog extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbDateFilter;
    private JLabel lblTotalLogin;
    private JLabel lblActiveNow;
    private JLabel lblInactive;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    // Custom date selection
    private JSpinner spnYear, spnMonth, spnDay;
    private java.sql.Date customDate = null;

    public ActivityLogDialog(Window parent, User currentUser) {
        super(parent, "Log Riwayat Tim", ModalityType.APPLICATION_MODAL);
        // currentUser passed but not stored (not needed for current implementation)
        initComponents();
        loadData("Hari Ini");
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 15", "[grow]", "[]15[]15[grow][]"));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, "wrap, growx");

        // Summary Cards
        JPanel summaryPanel = createSummaryPanel();
        add(summaryPanel, "wrap, growx");

        // Table
        JScrollPane tableScroll = createTablePanel();
        add(tableScroll, "grow, wrap");

        // Bottom buttons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, "growx");

        setSize(1200, 650);
        setLocationRelativeTo(getParent());
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx", "[]10[][]", ""));
        panel.setOpaque(false);

        // Title
        JLabel lblTitle = new JLabel("Log Riwayat Tim");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ColorScheme.TEXT_PRIMARY);

        // Date Filter Label (fixed spacing)
        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Filter Dropdown
        cmbDateFilter = new JComboBox<>(new String[] {
                "Hari Ini", "7 Hari", "30 Hari", "Pilih Tanggal", "Semua"
        });
        cmbDateFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbDateFilter.addActionListener(e -> {
            String selected = (String) cmbDateFilter.getSelectedItem();
            if ("Pilih Tanggal".equals(selected)) {
                showDatePicker();
            } else {
                loadData(selected);
            }
        });

        panel.add(lblTitle, "pushx");
        panel.add(lblFilter);
        panel.add(cmbDateFilter);

        return panel;
    }

    /**
     * Show custom date picker dialog
     */
    private void showDatePicker() {
        JDialog dateDialog = new JDialog(this, "Pilih Tanggal", true);
        dateDialog.setLayout(new MigLayout("fill, insets 20", "[grow]", "[]15[]"));

        // Date selection panel
        JPanel datePanel = new JPanel(new MigLayout("", "[]10[]10[]", ""));

        Calendar cal = Calendar.getInstance();

        // Year spinner
        spnYear = new JSpinner(new SpinnerNumberModel(cal.get(Calendar.YEAR), 2020, 2030, 1));
        JSpinner.NumberEditor yearEditor = new JSpinner.NumberEditor(spnYear, "0000");
        spnYear.setEditor(yearEditor);

        // Month spinner (1-12)
        spnMonth = new JSpinner(new SpinnerNumberModel(cal.get(Calendar.MONTH) + 1, 1, 12, 1));

        // Day spinner (1-31)
        spnDay = new JSpinner(new SpinnerNumberModel(cal.get(Calendar.DAY_OF_MONTH), 1, 31, 1));

        datePanel.add(new JLabel("Tahun:"));
        datePanel.add(spnYear, "w 80!");
        datePanel.add(new JLabel("Bulan:"));
        datePanel.add(spnMonth, "w 60!");
        datePanel.add(new JLabel("Tanggal:"));
        datePanel.add(spnDay, "w 60!");

        dateDialog.add(datePanel, "wrap, growx");

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Batal");

        btnOk.addActionListener(e -> {
            int year = (int) spnYear.getValue();
            int month = (int) spnMonth.getValue() - 1; // Calendar months are 0-based
            int day = (int) spnDay.getValue();

            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, day, 0, 0, 0);
            selectedCal.set(Calendar.MILLISECOND, 0);

            customDate = new java.sql.Date(selectedCal.getTimeInMillis());
            dateDialog.dispose();
            loadDataForDate(customDate);
        });

        btnCancel.addActionListener(e -> {
            cmbDateFilter.setSelectedIndex(0); // Reset to "Hari Ini"
            dateDialog.dispose();
        });

        btnPanel.add(btnOk);
        btnPanel.add(btnCancel);
        dateDialog.add(btnPanel, "growx");

        dateDialog.pack();
        dateDialog.setLocationRelativeTo(this);
        dateDialog.setVisible(true);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 0", "[grow][grow][grow]", ""));
        panel.setOpaque(false);

        // Total Login Today
        JPanel card1 = createMiniCard("Total Login Hari Ini", "", ColorScheme.ACCENT_BLUE);
        lblTotalLogin = (JLabel) ((JPanel) card1.getComponent(1)).getComponent(0);

        // Active Now
        JPanel card2 = createMiniCard("Aktif Sekarang", "", ColorScheme.ACCENT_GREEN);
        lblActiveNow = (JLabel) ((JPanel) card2.getComponent(1)).getComponent(0);

        // Inactive 3+ Days
        JPanel card3 = createMiniCard("Tidak Aktif (3+ Hari)", "", ColorScheme.ACCENT_RED);
        lblInactive = (JLabel) ((JPanel) card3.getComponent(1)).getComponent(0);

        panel.add(card1, "growx");
        panel.add(card2, "growx");
        panel.add(card3, "growx");

        return panel;
    }

    private JPanel createMiniCard(String title, String value, Color color) {
        JPanel card = new JPanel(new MigLayout("fillx, insets 10", "[center]", "[]8[]"));
        card.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(ColorScheme.TEXT_SECONDARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(color);
        lblValue.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle, BorderLayout.CENTER);

        JPanel valuePanel = new JPanel(new BorderLayout());
        valuePanel.setOpaque(false);
        valuePanel.add(lblValue, BorderLayout.CENTER);

        card.add(titlePanel, "wrap, align center");
        card.add(valuePanel, "align center");

        return card;
    }

    private JScrollPane createTablePanel() {
        // NEW COLUMNS: Nama, Role, Shift, Login, Logout, Durasi, Keterangan
        String[] columns = { "Nama Karyawan", "Role", "Shift", "Login", "Logout", "Durasi", "Keterangan" };
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
        table.setSelectionBackground(ColorScheme.ACCENT_BLUE);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(230, 230, 230));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(180); // Nama
        table.getColumnModel().getColumn(1).setPreferredWidth(80); // Role
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Shift
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Login
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Logout
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Durasi
        table.getColumnModel().getColumn(6).setPreferredWidth(180); // Keterangan

        // Custom renderer for shift column
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected && value != null) {
                    String shift = value.toString();
                    if ("Pagi".equals(shift)) {
                        c.setForeground(new Color(255, 152, 0)); // Orange
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if ("Siang".equals(shift)) {
                        c.setForeground(new Color(33, 150, 243)); // Blue
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if ("Malam".equals(shift)) {
                        c.setForeground(new Color(156, 39, 176)); // Purple
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }

                setHorizontalAlignment(CENTER);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        return scrollPane;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 0", "push[]10[]10[]", ""));
        panel.setOpaque(false);

        JButton btnExport = new JButton("Export Excel");
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExport.setBackground(new Color(76, 175, 80)); // Green
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.setBorderPainted(false);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.addActionListener(e -> exportToExcel());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setBackground(ColorScheme.ACCENT_BLUE);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadData((String) cmbDateFilter.getSelectedItem()));

        JButton btnClose = new JButton("Tutup");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClose.setBackground(new Color(240, 240, 240));
        btnClose.setForeground(Color.DARK_GRAY);
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        panel.add(btnExport);
        panel.add(btnRefresh);
        panel.add(btnClose);

        return panel;
    }

    /**
     * Load data for specific date
     */
    private void loadDataForDate(java.sql.Date date) {
        tableModel.setRowCount(0);

        // Get all activities for the date
        List<UserActivityLog> logs = UserActivityLog.getByDate(date);

        // Process and display
        processAndDisplayLogs(logs);
        updateSummaryCards();
    }

    /**
     * Load data based on filter
     */
    private void loadData(String filter) {
        tableModel.setRowCount(0);

        // Calculate date range
        Calendar cal = Calendar.getInstance();
        java.sql.Date endDate = new java.sql.Date(cal.getTimeInMillis());
        java.sql.Date startDate;

        switch (filter) {
            case "Hari Ini":
                startDate = endDate;
                break;
            case "7 Hari":
                cal.add(Calendar.DAY_OF_MONTH, -7);
                startDate = new java.sql.Date(cal.getTimeInMillis());
                break;
            case "30 Hari":
                cal.add(Calendar.DAY_OF_MONTH, -30);
                startDate = new java.sql.Date(cal.getTimeInMillis());
                break;
            case "Pilih Tanggal":
                return; // Will be handled by date picker
            default: // Semua
                cal.add(Calendar.YEAR, -1);
                startDate = new java.sql.Date(cal.getTimeInMillis());
                break;
        }

        // Load data
        List<UserActivityLog> logs = UserActivityLog.getByDateRange(startDate, endDate);

        // Process and display
        processAndDisplayLogs(logs);
        updateSummaryCards();
    }

    /**
     * Process logs: match LOGIN with LOGOUT, detect shift, calculate duration
     */
    private void processAndDisplayLogs(List<UserActivityLog> logs) {
        // Clear table first
        tableModel.setRowCount(0);

        // Sort logs by time ASCENDING for proper login-logout pairing
        logs.sort(Comparator.comparing(UserActivityLog::getActivityTime));

        // Map to track open sessions by user ID
        Map<Integer, UserActivityLog> openSessions = new HashMap<>();

        for (UserActivityLog log : logs) {
            String activityType = log.getActivityType();
            User user = log.getUser();

            if ("LOGIN".equals(activityType)) {
                // Start a new session or overwrite checking
                // If there's already an open session, we might want to close it as "Incomplete"
                // or just overwrite. For simplicity and log flow, let's treat every LOGIN as
                // starting a row.
                // But to pair with logout, we need to hold it.

                // If previous session exists (unclosed), add it as row with "-" logout
                if (openSessions.containsKey(user.getIdUser())) {
                    UserActivityLog prevLogin = openSessions.get(user.getIdUser());
                    addTableRow(prevLogin, null);
                }
                openSessions.put(user.getIdUser(), log);

            } else if ("LOGOUT".equals(activityType)) {
                // Find matching login
                if (openSessions.containsKey(user.getIdUser())) {
                    UserActivityLog loginLog = openSessions.remove(user.getIdUser());
                    addTableRow(loginLog, log);
                } else {
                    // Logout without login (orphaned or from previous day not in range)
                    // Optional: Show it? User wants "23" rows if count says 23.
                    // If we only show paired, we might miss counts.
                    // Let's infer a row.
                    addTableRow(null, log);
                }
            }
        }

        // Add remaining open sessions
        for (UserActivityLog loginLog : openSessions.values()) {
            addTableRow(loginLog, null);
        }

        // REVERSE table rows to show newest first (bottom row becomes top row)
        reverseTableRows();
    }

    /**
     * Reverse the order of table rows to show newest entries first
     */
    private void reverseTableRows() {
        int rowCount = tableModel.getRowCount();
        if (rowCount <= 1)
            return; // Nothing to reverse

        // Store all rows
        java.util.List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            Object[] rowData = new Object[tableModel.getColumnCount()];
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                rowData[j] = tableModel.getValueAt(i, j);
            }
            rows.add(rowData);
        }

        // Clear and re-add in reverse order
        tableModel.setRowCount(0);
        for (int i = rows.size() - 1; i >= 0; i--) {
            tableModel.addRow(rows.get(i));
        }
    }

    private void addTableRow(UserActivityLog loginLog, UserActivityLog logoutLog) {
        User user = (loginLog != null) ? loginLog.getUser() : logoutLog.getUser();
        Timestamp loginTime = (loginLog != null) ? loginLog.getActivityTime() : null;
        Timestamp logoutTime = (logoutLog != null) ? logoutLog.getActivityTime() : null;
        String note = (logoutLog != null && logoutLog.getSessionNote() != null) ? logoutLog.getSessionNote() : "-";

        // Detect shift
        String shift = detectShift(loginTime != null ? loginTime : logoutTime); // Use whichever time we have

        // Duration
        String duration = "-";
        if (loginTime != null && logoutTime != null) {
            long diff = logoutTime.getTime() - loginTime.getTime();
            long hours = diff / (1000 * 60 * 60);
            long minutes = (diff % (1000 * 60 * 60)) / (1000 * 60);
            duration = String.format("%dh %dm", hours, minutes);
        }

        tableModel.addRow(new Object[] {
                user.getNamaLengkap(),
                user.getRole(),
                shift,
                loginTime != null ? timeFormat.format(loginTime) : "-",
                logoutTime != null ? timeFormat.format(logoutTime) : "-",
                duration,
                note
        });
    }

    /**
     * Auto-detect shift based on login time
     * Pagi: 06:00-14:00
     * Siang: 14:00-22:00
     * Malam: 22:00-06:00
     */
    private String detectShift(Timestamp loginTime) {
        if (loginTime == null)
            return "-";

        Calendar cal = Calendar.getInstance();
        cal.setTime(loginTime);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour < 14) {
            return "Pagi";
        } else if (hour >= 14 && hour < 22) {
            return "Siang";
        } else {
            return "Malam";
        }
    }

    private void updateSummaryCards() {
        // Count today's logins
        int todayLoginCount = UserActivityLog.getByDate(
                new java.sql.Date(System.currentTimeMillis())).size();
        lblTotalLogin.setText(String.valueOf(todayLoginCount));

        // Count active sessions
        int activeSessions = UserActivityLog.getActiveSessions().size();
        lblActiveNow.setText(String.valueOf(activeSessions));

        // Count inactive employees (3+ days)
        int inactiveCount = UserActivityLog.getInactiveEmployeeCount(3);
        lblInactive.setText(String.valueOf(inactiveCount));
    }

    private void exportToExcel() {
        String filter = (String) cmbDateFilter.getSelectedItem();

        // Calculate date range
        Calendar cal = Calendar.getInstance();
        java.sql.Date tempEndDate = new java.sql.Date(cal.getTimeInMillis());
        java.sql.Date tempStartDate;

        if ("Hari Ini".equals(filter)) {
            tempStartDate = tempEndDate;
        } else if ("7 Hari".equals(filter)) {
            cal.add(Calendar.DAY_OF_MONTH, -7);
            tempStartDate = new java.sql.Date(cal.getTimeInMillis());
        } else if ("30 Hari".equals(filter)) {
            cal.add(Calendar.DAY_OF_MONTH, -30);
            tempStartDate = new java.sql.Date(cal.getTimeInMillis());
        } else if ("Pilih Tanggal".equals(filter) && customDate != null) {
            tempStartDate = customDate;
            tempEndDate = customDate;
        } else { // Semua
            cal.add(Calendar.YEAR, -1);
            tempStartDate = new java.sql.Date(cal.getTimeInMillis());
        }

        // Make final for lambda use
        final java.sql.Date startDate = tempStartDate;
        final java.sql.Date endDate = tempEndDate;

        // File chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan");
        fileChooser.setSelectedFile(new java.io.File("Laporan_Kehadiran_" +
                new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + ".xlsx"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String tempPath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!tempPath.toLowerCase().endsWith(".xlsx")) {
                tempPath += ".xlsx";
            }
            final String filePath = tempPath; // Make final for lambda

            // Generate report in background
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    com.kedaikopi.util.AttendanceReportGenerator generator = new com.kedaikopi.util.AttendanceReportGenerator();

                    if ("Hari Ini".equals(filter) || "Pilih Tanggal".equals(filter)) {
                        return generator.generateDailyReport(startDate, filePath);
                    } else {
                        return generator.generateMonthlyReport(startDate, endDate, filePath);
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(ActivityLogDialog.this,
                                    "Laporan berhasil di-export ke:\n" + filePath,
                                    "Export Berhasil",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(ActivityLogDialog.this,
                                    "Gagal meng-export laporan!",
                                    "Export Gagal",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ActivityLogDialog.this,
                                "Error: " + e.getMessage(),
                                "Export Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
        }
    }
}
