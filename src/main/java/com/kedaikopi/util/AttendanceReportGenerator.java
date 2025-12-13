package com.kedaikopi.util;

import com.kedaikopi.model.UserActivityLog;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Attendance Report Generator
 * Generate Excel reports for employee attendance
 */
public class AttendanceReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceReportGenerator.class);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    /**
     * Generate daily attendance report
     */
    public boolean generateDailyReport(java.sql.Date date, String outputPath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Laporan Harian - " + dateFormat.format(date));

            // Create header
            createHeader(sheet, "LAPORAN KEHADIRAN HARIAN", dateFormat.format(date));

            // Create column headers
            Row headerRow = sheet.createRow(4);
            String[] columns = { "No", "Nama Karyawan", "Role", "Login Pertama", "Logout Terakhir", "Total Jam" };
            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Get activity data
            List<UserActivityLog> logs = UserActivityLog.getByDate(date);
            Map<Integer, DailyActivity> dailyMap = processDailyActivity(logs);

            // Fill data
            int rowNum = 5;
            int no = 1;
            CellStyle dataStyle = createDataStyle(workbook);

            for (DailyActivity activity : dailyMap.values()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(no++);
                row.createCell(1).setCellValue(activity.namaLengkap);
                row.createCell(2).setCellValue(activity.role);
                row.createCell(3)
                        .setCellValue(activity.firstLogin != null ? timeFormat.format(activity.firstLogin) : "-");
                row.createCell(4)
                        .setCellValue(activity.lastLogout != null ? timeFormat.format(activity.lastLogout) : "-");
                row.createCell(5).setCellValue(String.format("%.2f jam", activity.totalHours));

                for (int i = 0; i < 6; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }

            logger.info("Daily report generated: {}", outputPath);
            return true;

        } catch (Exception e) {
            logger.error("Error generating daily report: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generate monthly attendance report
     */
    public boolean generateMonthlyReport(java.sql.Date startDate, java.sql.Date endDate, String outputPath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Laporan Bulanan");

            // Create header
            createHeader(sheet, "LAPORAN KEHADIRAN BULANAN",
                    dateFormat.format(startDate) + " - " + dateFormat.format(endDate));

            // Create column headers
            Row headerRow = sheet.createRow(4);
            String[] columns = { "No", "Nama Karyawan", "Role", "Hadir", "Total Jam", "Rata-rata Jam/Hari" };
            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Get activity data
            List<UserActivityLog> logs = UserActivityLog.getByDateRange(startDate, endDate);
            Map<Integer, MonthlyActivity> monthlyMap = processMonthlyActivity(logs);

            // Fill data
            int rowNum = 5;
            int no = 1;
            CellStyle dataStyle = createDataStyle(workbook);

            for (MonthlyActivity activity : monthlyMap.values()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(no++);
                row.createCell(1).setCellValue(activity.namaLengkap);
                row.createCell(2).setCellValue(activity.role);
                row.createCell(3).setCellValue(activity.daysPresent);
                row.createCell(4).setCellValue(String.format("%.2f jam", activity.totalHours));
                row.createCell(5).setCellValue(String.format("%.2f jam",
                        activity.daysPresent > 0 ? activity.totalHours / activity.daysPresent : 0));

                for (int i = 0; i < 6; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }

            logger.info("Monthly report generated: {}", outputPath);
            return true;

        } catch (Exception e) {
            logger.error("Error generating monthly report: {}", e.getMessage(), e);
            return false;
        }
    }

    private void createHeader(Sheet sheet, String title, String date) {
        // Title row
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);

        // Date row
        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue(date);

        // Empty row for spacing
        sheet.createRow(2);
        sheet.createRow(3);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private Map<Integer, DailyActivity> processDailyActivity(List<UserActivityLog> logs) {
        Map<Integer, DailyActivity> map = new HashMap<>();

        for (UserActivityLog log : logs) {
            int userId = log.getUser().getIdUser();
            DailyActivity activity = map.computeIfAbsent(userId, k -> new DailyActivity());

            activity.namaLengkap = log.getUser().getNamaLengkap();
            activity.role = log.getUser().getRole();

            if ("LOGIN".equals(log.getActivityType())) {
                if (activity.firstLogin == null || log.getActivityTime().before(activity.firstLogin)) {
                    activity.firstLogin = log.getActivityTime();
                }
            } else if ("LOGOUT".equals(log.getActivityType())) {
                if (activity.lastLogout == null || log.getActivityTime().after(activity.lastLogout)) {
                    activity.lastLogout = log.getActivityTime();
                }
            }
        }

        // Calculate total hours
        for (DailyActivity activity : map.values()) {
            if (activity.firstLogin != null && activity.lastLogout != null) {
                long diff = activity.lastLogout.getTime() - activity.firstLogin.getTime();
                activity.totalHours = diff / (1000.0 * 60 * 60); // Convert to hours
            }
        }

        return map;
    }

    private Map<Integer, MonthlyActivity> processMonthlyActivity(List<UserActivityLog> logs) {
        Map<Integer, MonthlyActivity> map = new HashMap<>();
        Map<Integer, Set<String>> daysPresent = new HashMap<>();

        for (UserActivityLog log : logs) {
            int userId = log.getUser().getIdUser();
            MonthlyActivity activity = map.computeIfAbsent(userId, k -> new MonthlyActivity());

            activity.namaLengkap = log.getUser().getNamaLengkap();
            activity.role = log.getUser().getRole();

            // Track days present (LOGIN events)
            if ("LOGIN".equals(log.getActivityType())) {
                Set<String> days = daysPresent.computeIfAbsent(userId, k -> new HashSet<>());
                String dayKey = dateFormat.format(log.getActivityTime());
                days.add(dayKey);
            }
        }

        // Calculate days present and total hours (simplified)
        for (Map.Entry<Integer, MonthlyActivity> entry : map.entrySet()) {
            int userId = entry.getKey();
            MonthlyActivity activity = entry.getValue();

            Set<String> days = daysPresent.get(userId);
            activity.daysPresent = days != null ? days.size() : 0;

            // Estimate 8 hours per day present (can be improved with actual session
            // calculation)
            activity.totalHours = activity.daysPresent * 8.0;
        }

        return map;
    }

    // Helper classes
    private static class DailyActivity {
        String namaLengkap;
        String role;
        Timestamp firstLogin;
        Timestamp lastLogout;
        double totalHours;
    }

    private static class MonthlyActivity {
        String namaLengkap;
        String role;
        int daysPresent;
        double totalHours;
    }
}
