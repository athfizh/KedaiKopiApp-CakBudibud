package com.kedaikopi.ui.dialogs;

import com.kedaikopi.ui.components.UIComponents;
import com.kedaikopi.util.ColorScheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * E-Wallet Payment Dialog
 * Supports GoPay, OVO, DANA, ShopeePay
 */
public class EWalletDialog extends JDialog {

    private boolean success = false;
    private String walletType;
    private double amount;
    private JLabel lblStatus;

    public EWalletDialog(JFrame owner, String walletType, double amount) {
        super(owner, "Pembayaran " + walletType, Dialog.ModalityType.APPLICATION_MODAL);
        this.walletType = walletType;
        this.amount = amount;
        initComponents();

        // Auto-simulate payment after 2.5 seconds
        Timer timer = new Timer(2500, e -> simulatePayment());
        timer.setRepeats(false);
        timer.start();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]15[]15[]20[]"));
        setBackground(Color.WHITE);

        // Title
        JLabel lblTitle = new JLabel(walletType, SwingConstants.CENTER);
        lblTitle.setFont(UIComponents.FONT_HEADING.deriveFont(Font.BOLD, 22f));
        lblTitle.setForeground(getWalletColor());
        add(lblTitle, "wrap, center");

        // Payment info
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, "growx, wrap");

        // Instructions
        JPanel instructionPanel = createInstructionPanel();
        add(instructionPanel, "growx, wrap");

        // Status
        lblStatus = new JLabel("Menunggu konfirmasi pembayaran...", SwingConstants.CENTER);
        lblStatus.setFont(UIComponents.FONT_BODY);
        lblStatus.setForeground(ColorScheme.ACCENT_BLUE);
        add(lblStatus, "wrap, center, gaptop 10");

        // Cancel button
        JButton btnCancel = UIComponents.createButton("Batal", UIComponents.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> {
            success = false;
            dispose();
        });
        add(btnCancel, "center");

        setSize(450, 550); // Increased height from 500 to 550 to prevent scrollbar
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private Color getWalletColor() {
        switch (walletType) {
            case "GoPay":
                return new Color(0, 170, 100);
            case "OVO":
                return new Color(79, 70, 229);
            case "DANA":
                return new Color(37, 99, 235);
            case "ShopeePay":
                return new Color(234, 88, 12);
            default:
                return ColorScheme.ACCENT_BLUE;
        }
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[right]10[grow]", "[]10[]10[]"));
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));

        addInfoRow(panel, "Merchant:", "Kedai Kopi Cak Budi");
        addInfoRow(panel, "Nomor:", "0812-3456-7890");
        addInfoRow(panel, "Jumlah:", String.format("Rp%,.2f", amount));

        return panel;
    }

    private JPanel createInstructionPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[]5[]"));
        panel.setBackground(new Color(59, 130, 246, 10));
        panel.setBorder(BorderFactory.createLineBorder(new Color(59, 130, 246, 50), 1));

        JLabel lblTitle = new JLabel("Instruksi Pembayaran:");
        lblTitle.setFont(UIComponents.FONT_SMALL.deriveFont(Font.BOLD));
        lblTitle.setForeground(ColorScheme.TEXT_PRIMARY);

        String instruction = String.format(
                "<html>" +
                        "1. Buka aplikasi %s<br>" +
                        "2. Scan QR Code atau masukkan nomor merchant<br>" +
                        "3. Konfirmasi pembayaran sejumlah %s<br>" +
                        "4. Tunggu notifikasi pembayaran berhasil" +
                        "</html>",
                walletType,
                String.format("Rp%,.0f", amount));

        JLabel lblInstruction = new JLabel(instruction);
        lblInstruction.setFont(UIComponents.FONT_SMALL);
        lblInstruction.setForeground(ColorScheme.TEXT_SECONDARY);

        panel.add(lblTitle, "wrap");
        panel.add(lblInstruction);

        return panel;
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(UIComponents.FONT_SMALL);
        lblLabel.setForeground(ColorScheme.TEXT_SECONDARY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD));
        lblValue.setForeground(ColorScheme.TEXT_PRIMARY);

        panel.add(lblLabel);
        panel.add(lblValue, "wrap");
    }

    private void simulatePayment() {
        String refNumber = "TRX" + System.currentTimeMillis();

        String message = String.format(
                "<html><center>" +
                        "<b style='color: #059669; font-size: 14px;'>PEMBAYARAN BERHASIL</b><br><br>" +
                        "Ref: %s<br>" +
                        "Jumlah: Rp%,.2f<br><br>" +
                        "Terima kasih!" +
                        "</center></html>",
                refNumber, amount);

        lblStatus.setText(message);
        lblStatus.setForeground(ColorScheme.ACCENT_GREEN);

        success = true;

        // Auto close after 2 seconds
        Timer closeTimer = new Timer(2000, e -> dispose());
        closeTimer.setRepeats(false);
        closeTimer.start();
    }

    public boolean isSuccess() {
        return success;
    }
}
