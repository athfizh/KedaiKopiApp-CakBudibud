package com.kedaikopi.ui.dialogs;

import com.kedaikopi.ui.components.UIComponents;
import com.kedaikopi.util.ColorScheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * EDC (Electronic Data Capture) Simulation Dialog
 * Simulates card payment process (Debit/Credit)
 */
public class EDCDialog extends JDialog {

    private boolean success = false;
    private String cardType;
    private double amount;
    private JLabel lblStatus;
    private JButton btnConfirm;

    public EDCDialog(JFrame owner, String cardType, double amount) {
        super(owner, "EDC - " + cardType, Dialog.ModalityType.APPLICATION_MODAL);
        this.cardType = cardType;
        this.amount = amount;
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]15[]15[]20[]"));
        setBackground(Color.WHITE);

        // Title
        JLabel lblTitle = new JLabel("EDC Payment Terminal", SwingConstants.CENTER);
        lblTitle.setFont(UIComponents.FONT_SUBHEADING);
        lblTitle.setForeground(ColorScheme.TEXT_PRIMARY);
        add(lblTitle, "wrap, center");

        // Payment info
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, "growx, wrap");

        // Status
        lblStatus = new JLabel("Masukkan/Tap kartu Anda...", SwingConstants.CENTER);
        lblStatus.setFont(UIComponents.FONT_BODY);
        lblStatus.setForeground(ColorScheme.TEXT_SECONDARY);
        add(lblStatus, "wrap, center");

        // Buttons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, "growx");

        setSize(450, 480); // Increased from 400 to 480 to prevent scrollbar
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[right]10[grow]", "[]10[]10[]10[]"));
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));

        // Card Type
        addInfoRow(panel, "Jenis Kartu:", cardType);

        // Amount
        addInfoRow(panel, "Jumlah:", String.format("Rp%,.2f", amount));

        // Merchant
        addInfoRow(panel, "Merchant:", "KEDAI KOPI CAK BUDI");

        // Terminal ID
        addInfoRow(panel, "Terminal ID:", "KB" + String.format("%08d", new Random().nextInt(100000000)));

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

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "push[]10[]"));
        panel.setOpaque(false); // Transparent background

        JButton btnCancel = UIComponents.createButton("Batal", UIComponents.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> {
            success = false;
            dispose();
        });

        btnConfirm = UIComponents.createButton("Proses Pembayaran", UIComponents.ButtonType.SUCCESS);
        btnConfirm.addActionListener(e -> processPayment());

        panel.add(btnCancel);
        panel.add(btnConfirm);

        return panel;
    }

    private void processPayment() {
        btnConfirm.setEnabled(false);
        lblStatus.setText("Memproses pembayaran...");
        lblStatus.setForeground(ColorScheme.ACCENT_BLUE);

        // Simulate processing
        Timer timer = new Timer(1500, e -> {
            // Generate approval code
            String approvalCode = String.format("%012d", new Random().nextLong() & 0xFFFFFFFFFFFFL);
            String maskedCard = "****-****-****-" + String.format("%04d", new Random().nextInt(10000));

            String message = String.format(
                    "<html><center>" +
                            "<b style='color: #059669;'>PEMBAYARAN BERHASIL</b><br><br>" +
                            "Kartu: %s<br>" +
                            "Approval Code: %s<br>" +
                            "Jumlah: Rp%,.2f<br><br>" +
                            "Terima kasih!" +
                            "</center></html>",
                    maskedCard, approvalCode, amount);

            lblStatus.setText(message);
            lblStatus.setForeground(ColorScheme.ACCENT_GREEN);

            success = true;

            // Auto close after 2 seconds
            Timer closeTimer = new Timer(2000, evt -> dispose());
            closeTimer.setRepeats(false);
            closeTimer.start();
        });

        timer.setRepeats(false);
        timer.start();
    }

    public boolean isSuccess() {
        return success;
    }
}
