package com.kedaikopi.util;

import com.kedaikopi.model.TransaksiDetail;
import com.kedaikopi.model.TransaksiHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Receipt Printer - Generates thermal printer-style receipts
 * 58mm thermal paper format (42 characters width)
 */
public class ReceiptPrinter {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptPrinter.class);
    private static final int RECEIPT_WIDTH = 42; // Characters
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Show print preview dialog (BLOCKING MODAL)
     */
    public static void showPrintPreview(Window parent, TransaksiHeader transaction) {
        try {
            String receiptText = generateReceiptText(transaction);

            JDialog dialog = new JDialog(parent,
                    "Preview Struk - Transaksi #" + transaction.getIdTransaksiHeader(),
                    Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(500, 700);
            dialog.setLocationRelativeTo(null);

            JTextArea textArea = new JTextArea(receiptText);
            textArea.setFont(new Font("Courier New", Font.PLAIN, 14));
            textArea.setEditable(false);
            textArea.setBackground(Color.WHITE);
            textArea.setBorder(BorderFactory.createEmptyBorder(15, 50, 15, 20));
            textArea.setLineWrap(false);
            textArea.setWrapStyleWord(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Faster scroll

            dialog.add(scrollPane, BorderLayout.CENTER);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            JButton btnPrint = new JButton("Print");
            btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnPrint.setForeground(Color.WHITE);
            btnPrint.setBackground(ColorScheme.ACCENT_BLUE);
            btnPrint.setBorderPainted(false);
            btnPrint.setFocusPainted(false);
            btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnPrint.addActionListener(e -> {
                printReceipt(transaction);
                dialog.dispose();
            });

            JButton btnSave = new JButton("Save as TXT");
            btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btnSave.setBackground(ColorScheme.ACCENT_GREEN);
            btnSave.setForeground(Color.WHITE);
            btnSave.setBorderPainted(false);
            btnSave.setFocusPainted(false);
            btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSave.addActionListener(e -> {
                saveReceiptToFile(transaction, receiptText);
            });

            JButton btnClose = new JButton("Close");
            btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btnClose.setBackground(ColorScheme.BUTTON_SECONDARY);
            btnClose.setForeground(Color.WHITE);
            btnClose.setBorderPainted(false);
            btnClose.setFocusPainted(false);
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.addActionListener(e -> dialog.dispose());

            buttonPanel.add(btnPrint);
            buttonPanel.add(btnSave);
            buttonPanel.add(btnClose);

            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // BLOCKS HERE until user closes dialog ✅
            dialog.setVisible(true);

        } catch (Exception e) {
            logger.error("Error showing receipt preview", e);
            JOptionPane.showMessageDialog(parent,
                    "Gagal menampilkan struk: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generate receipt text in thermal printer format
     */
    public static String generateReceiptText(TransaksiHeader transaction) {
        StringBuilder receipt = new StringBuilder();

        // Header
        receipt.append(line("=")).append("\n");
        receipt.append(center("KEDAI KOPI CAK BUDIBUD")).append("\n");
        receipt.append(center("Jl. Soekarno Hatta No. 9")).append("\n");
        receipt.append(center("Malang, Jawa Timur")).append("\n");
        receipt.append(center("Telp: (0341) 404040")).append("\n");
        receipt.append(line("=")).append("\n\n");

        // Transaction info
        String kasir = transaction.getUser().getNamaLengkap();
        String tanggal = dateFormat.format(transaction.getTanggal());
        String noTrx = String.format("TRX-%04d", transaction.getIdTransaksiHeader());

        receipt.append(leftRight("Kasir", kasir)).append("\n");
        receipt.append(leftRight("Tanggal", tanggal)).append("\n");
        receipt.append(leftRight("No. Trx", noTrx)).append("\n\n");

        // Items header
        receipt.append(line("-")).append("\n");
        receipt.append(String.format("%-24s %3s %10s\n", "Item", "Qty", "Subtotal"));
        receipt.append(line("-")).append("\n");

        // Items
        for (TransaksiDetail detail : transaction.getDetails()) {
            String nama = detail.getMenu().getNamaMenu();
            if (nama.length() > 24) {
                nama = nama.substring(0, 21) + "...";
            }

            int qty = detail.getQty();
            double itemSubtotal = qty * detail.getSubtotal() / qty; // Subtotal per item

            receipt.append(String.format("%-24s %3d %10s\n",
                    nama, qty, formatCurrency(itemSubtotal)));
        }

        receipt.append(line("-")).append("\n\n");

        // Summary - use transaction values directly
        double transactionSubtotal = transaction.getTotalHarga();
        double tax = transaction.getPajak();
        double grandTotal = transaction.getGrandTotal();
        double tunai = transaction.getTunai();
        double kembalian = transaction.getKembalian();

        receipt.append(leftRight("SUBTOTAL", formatCurrency(transactionSubtotal))).append("\n");
        receipt.append(leftRight("PPN (10%)", formatCurrency(tax))).append("\n");
        receipt.append(line("-")).append("\n");
        receipt.append(leftRight("TOTAL", formatCurrency(grandTotal))).append("\n");
        receipt.append(center("* Harga sudah termasuk pajak 10% *")).append("\n");
        receipt.append(line("=")).append("\n\n");

        // Payment
        receipt.append(leftRight("TUNAI", formatCurrency(tunai))).append("\n");
        receipt.append(leftRight("KEMBALIAN", formatCurrency(kembalian))).append("\n\n");

        // Footer
        receipt.append(line("=")).append("\n");
        receipt.append(center("Terima Kasih Atas Kunjungan Anda")).append("\n");
        receipt.append(center("Silakan Datang Kembali!")).append("\n");
        receipt.append(line("=")).append("\n");

        return receipt.toString();
    }

    /**
     * Print receipt to printer
     */
    public static void printReceipt(TransaksiHeader transaction) {
        try {
            String receiptText = generateReceiptText(transaction);

            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName("Receipt - TRX-" + transaction.getIdTransaksiHeader());

            printerJob.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) {
                        return NO_SUCH_PAGE;
                    }

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    // Set font
                    Font font = new Font("Courier New", Font.PLAIN, 10);
                    g2d.setFont(font);
                    g2d.setColor(Color.BLACK);

                    // Print text line by line
                    int y = 20;
                    FontMetrics metrics = g2d.getFontMetrics(font);
                    int lineHeight = metrics.getHeight();

                    String[] lines = receiptText.split("\n");
                    for (String line : lines) {
                        g2d.drawString(line, 10, y);
                        y += lineHeight;
                    }

                    return PAGE_EXISTS;
                }
            });

            // Show print dialog
            if (printerJob.printDialog()) {
                printerJob.print();
                logger.info("Receipt printed for transaction {}", transaction.getIdTransaksiHeader());
                JOptionPane.showMessageDialog(null, "Struk berhasil dicetak!", "Print Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (PrinterException e) {
            logger.error("Error printing receipt", e);
            JOptionPane.showMessageDialog(null, "Gagal mencetak struk: " + e.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Save receipt to text file
     */
    private static void saveReceiptToFile(TransaksiHeader transaction, String receiptText) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Receipt");
        fileChooser.setSelectedFile(new java.io.File("Receipt_TRX-" + transaction.getIdTransaksiHeader() + ".txt"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(fileChooser.getSelectedFile())) {
                writer.write(receiptText);
                logger.info("Receipt saved to file: {}", fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(null, "Struk berhasil disimpan!", "Save Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                logger.error("Error saving receipt to file", e);
                JOptionPane.showMessageDialog(null, "Gagal menyimpan struk: " + e.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Helper methods for formatting

    private static String line(CharSequence charSequence) {
        String ch = charSequence.toString();
        return ch.repeat(RECEIPT_WIDTH);
    }

    private static String center(String text) {
        int padding = (RECEIPT_WIDTH - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    private static String leftRight(String left, String right) {
        int spaces = RECEIPT_WIDTH - left.length() - right.length();
        return left + " ".repeat(Math.max(1, spaces)) + right;
    }

    private static String formatCurrency(double amount) {
        String formatted = currencyFormat.format(amount);
        // Remove "Rp" prefix for compact display
        return formatted.replace("Rp", "").trim();
    }
}
