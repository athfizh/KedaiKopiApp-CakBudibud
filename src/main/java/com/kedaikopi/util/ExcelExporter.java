package com.kedaikopi.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for exporting JTable data to Excel files
 */
public class ExcelExporter {

    /**
     * Export JTable data to Excel with user-selectable location
     * 
     * @param table           The JTable to export
     * @param defaultFileName Default filename for the export
     * @param parent          Parent component for file chooser dialog
     * @return true if export succeeded, false otherwise
     */
    public static boolean exportToExcel(JTable table, String defaultFileName, JFrame parent) {
        // Show file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File Excel");
        fileChooser.setSelectedFile(new File(defaultFileName + ".xlsx"));

        // Filter to only show Excel files
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xlsx");
            }

            @Override
            public String getDescription() {
                return "Excel Files (*.xlsx)";
            }
        });

        int result = fileChooser.showSaveDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Ensure .xlsx extension
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }

            try {
                return writeExcel(table, file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent,
                        "Error saat menyimpan file: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return false; // User cancelled
    }

    /**
     * Export multiple JTables to Excel with each table in a separate sheet (with
     * summary section)
     * 
     * @param tables          Array of JTables to export
     * @param sheetNames      Array of sheet names for each table
     * @param summaryLabels   Array of summary labels to include in first sheet
     * @param summaryValues   Array of summary values to include in first sheet
     * @param defaultFileName Default filename for the export
     * @param parent          Parent component for file chooser dialog
     * @return true if export succeeded, false otherwise
     */
    public static boolean exportMultipleSheetsToExcel(JTable[] tables, String[] sheetNames,
            String[] summaryLabels, String[] summaryValues,
            String defaultFileName, JFrame parent) {
        if (tables == null || sheetNames == null || tables.length != sheetNames.length) {
            throw new IllegalArgumentException("Tables and sheet names arrays must have the same length");
        }

        // Show file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File Excel");
        fileChooser.setSelectedFile(new File(defaultFileName + ".xlsx"));

        // Filter to only show Excel files
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xlsx");
            }

            @Override
            public String getDescription() {
                return "Excel Files (*.xlsx)";
            }
        });

        int result = fileChooser.showSaveDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Ensure .xlsx extension
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }

            try {
                return writeMultipleSheetsExcelWithSummary(tables, sheetNames, summaryLabels, summaryValues, file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent,
                        "Error saat menyimpan file: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return false; // User cancelled
    }

    /**
     * Export multiple JTables to Excel with each table in a separate sheet (without
     * summary)
     * 
     * @param tables          Array of JTables to export
     * @param sheetNames      Array of sheet names for each table
     * @param defaultFileName Default filename for the export
     * @param parent          Parent component for file chooser dialog
     * @return true if export succeeded, false otherwise
     */
    public static boolean exportMultipleSheetsToExcel(JTable[] tables, String[] sheetNames,
            String defaultFileName, JFrame parent) {
        if (tables == null || sheetNames == null || tables.length != sheetNames.length) {
            throw new IllegalArgumentException("Tables and sheet names arrays must have the same length");
        }

        // Show file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File Excel");
        fileChooser.setSelectedFile(new File(defaultFileName + ".xlsx"));

        // Filter to only show Excel files
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xlsx");
            }

            @Override
            public String getDescription() {
                return "Excel Files (*.xlsx)";
            }
        });

        int result = fileChooser.showSaveDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Ensure .xlsx extension
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }

            try {
                return writeMultipleSheetsExcel(tables, sheetNames, file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent,
                        "Error saat menyimpan file: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return false; // User cancelled
    }

    /**
     * Write table data to Excel file
     */
    private static boolean writeExcel(JTable table, File file) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        TableModel model = table.getModel();

        // Create header row with styling
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int col = 0; col < model.getColumnCount(); col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(model.getColumnName(col));
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        CellStyle dataStyle = createDataStyle(workbook);

        for (int row = 0; row < model.getRowCount(); row++) {
            Row excelRow = sheet.createRow(row + 1);

            for (int col = 0; col < model.getColumnCount(); col++) {
                Cell cell = excelRow.createCell(col);
                Object value = model.getValueAt(row, col);

                if (value != null) {
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }

                cell.setCellStyle(dataStyle);
            }
        }

        // Auto-size columns
        for (int col = 0; col < model.getColumnCount(); col++) {
            sheet.autoSizeColumn(col);
            // Add extra padding
            int currentWidth = sheet.getColumnWidth(col);
            sheet.setColumnWidth(col, currentWidth + 1000);
        }

        // Enable filters
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                0, model.getRowCount(), 0, model.getColumnCount() - 1));

        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }

        workbook.close();
        return true;
    }

    /**
     * Write multiple tables to Excel file with separate sheets and summary section
     */
    private static boolean writeMultipleSheetsExcelWithSummary(JTable[] tables, String[] sheetNames,
            String[] summaryLabels, String[] summaryValues, File file) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // Create a sheet for each table
        for (int sheetIndex = 0; sheetIndex < tables.length; sheetIndex++) {
            JTable table = tables[sheetIndex];
            String sheetName = sheetNames[sheetIndex];
            Sheet sheet = workbook.createSheet(sheetName);

            TableModel model = table.getModel();
            int currentRow = 0;

            // For first sheet, add summary section
            if (sheetIndex == 0 && summaryLabels != null && summaryValues != null) {
                // Create summary title
                Row titleRow = sheet.createRow(currentRow++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("RINGKASAN FINANSIAL");
                CellStyle titleStyle = createTitleStyle(workbook);
                titleCell.setCellStyle(titleStyle);

                // Merge cells for title (across all columns)
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                        currentRow - 1, currentRow - 1, 0, Math.max(2, model.getColumnCount() - 1)));

                currentRow++; // Empty row

                // Create summary rows (2 columns per row: Label | Value)
                CellStyle summaryLabelStyle = createSummaryLabelStyle(workbook);
                CellStyle summaryValueStyle = createSummaryValueStyle(workbook);

                for (int i = 0; i < summaryLabels.length; i++) {
                    Row summaryRow = sheet.createRow(currentRow++);

                    Cell labelCell = summaryRow.createCell(0);
                    labelCell.setCellValue(summaryLabels[i]);
                    labelCell.setCellStyle(summaryLabelStyle);

                    Cell valueCell = summaryRow.createCell(1);
                    valueCell.setCellValue(summaryValues[i]);
                    valueCell.setCellStyle(summaryValueStyle);
                }

                currentRow += 2; // Add spacing before table
            }

            // Create header row with styling
            Row headerRow = sheet.createRow(currentRow++);
            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int col = 0; col < model.getColumnCount(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(model.getColumnName(col));
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            CellStyle dataStyle = createDataStyle(workbook);

            for (int row = 0; row < model.getRowCount(); row++) {
                Row excelRow = sheet.createRow(currentRow++);

                for (int col = 0; col < model.getColumnCount(); col++) {
                    Cell cell = excelRow.createCell(col);
                    Object value = model.getValueAt(row, col);

                    if (value != null) {
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }

                    cell.setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int col = 0; col < Math.max(2, model.getColumnCount()); col++) {
                sheet.autoSizeColumn(col);
                // Add extra padding
                int currentWidth = sheet.getColumnWidth(col);
                sheet.setColumnWidth(col, currentWidth + 1000);
            }

            // Enable filters on table data (only if there are rows)
            if (model.getRowCount() > 0) {
                int tableStartRow = sheetIndex == 0 && summaryLabels != null ? summaryLabels.length + 3 : 0;
                sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                        tableStartRow, tableStartRow + model.getRowCount(),
                        0, model.getColumnCount() - 1));
            }
        }

        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }

        workbook.close();
        return true;
    }

    /**
     * Write multiple tables to Excel file with separate sheets
     */
    private static boolean writeMultipleSheetsExcel(JTable[] tables, String[] sheetNames,
            File file) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // Create a sheet for each table
        for (int sheetIndex = 0; sheetIndex < tables.length; sheetIndex++) {
            JTable table = tables[sheetIndex];
            String sheetName = sheetNames[sheetIndex];
            Sheet sheet = workbook.createSheet(sheetName);

            TableModel model = table.getModel();

            // Create header row with styling
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int col = 0; col < model.getColumnCount(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(model.getColumnName(col));
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            CellStyle dataStyle = createDataStyle(workbook);

            for (int row = 0; row < model.getRowCount(); row++) {
                Row excelRow = sheet.createRow(row + 1);

                for (int col = 0; col < model.getColumnCount(); col++) {
                    Cell cell = excelRow.createCell(col);
                    Object value = model.getValueAt(row, col);

                    if (value != null) {
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }

                    cell.setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int col = 0; col < model.getColumnCount(); col++) {
                sheet.autoSizeColumn(col);
                // Add extra padding
                int currentWidth = sheet.getColumnWidth(col);
                sheet.setColumnWidth(col, currentWidth + 1000);
            }

            // Enable filters (only if there are rows)
            if (model.getRowCount() > 0) {
                sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                        0, model.getRowCount(), 0, model.getColumnCount() - 1));
            }
        }

        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }

        workbook.close();
        return true;
    }

    /**
     * Create professional header style
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Bold font
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        // Background color
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Alignment
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * Create data cell style
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Alignment
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * Create title style for summary section
     */
    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Bold large font
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);

        // Alignment
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * Create summary label style
     */
    private static CellStyle createSummaryLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Bold font
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        // Borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Alignment
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * Create summary value style
     */
    private static CellStyle createSummaryValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Regular font
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        // Borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Alignment
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }
}
