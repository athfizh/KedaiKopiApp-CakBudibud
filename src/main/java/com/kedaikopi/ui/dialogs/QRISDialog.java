package com.kedaikopi.ui.dialogs;

import com.kedaikopi.ui.components.UIComponents;
import com.kedaikopi.util.ColorScheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * QRIS Payment Dialog
 * Shows QR code and merchant information for QRIS payment
 */
public class QRISDialog extends JDialog {

    private boolean success = false;
    private double amount;
    private JLabel lblStatus;

    public QRISDialog(JFrame owner, double amount) {
        super(owner, "Pembayaran QRIS", Dialog.ModalityType.APPLICATION_MODAL);
        this.amount = amount;
        initComponents();

        // Auto-simulate payment after 3 seconds
        Timer timer = new Timer(3000, e -> simulatePayment());
        timer.setRepeats(false);
        timer.start();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]10[]15[]15[]20[]"));
        setBackground(Color.WHITE);

        // Title
        JLabel lblTitle = new JLabel("Scan QR Code", SwingConstants.CENTER);
        lblTitle.setFont(UIComponents.FONT_SUBHEADING);
        lblTitle.setForeground(ColorScheme.TEXT_PRIMARY);
        add(lblTitle, "wrap, center");

        // QR Code placeholder (fake QR)
        JPanel qrPanel = createQRCodePanel();
        add(qrPanel, "wrap, center");

        // Merchant info
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, "growx, wrap");

        // Status
        lblStatus = new JLabel("Menunggu pembayaran...", SwingConstants.CENTER);
        lblStatus.setFont(UIComponents.FONT_BODY);
        lblStatus.setForeground(ColorScheme.ACCENT_BLUE);
        add(lblStatus, "wrap, center");

        // Cancel button
        JButton btnCancel = UIComponents.createButton("Batal", UIComponents.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> {
            success = false;
            dispose();
        });
        add(btnCancel, "center");

        setSize(450, 600); // Increased height to prevent scrollbar
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private JPanel createQRCodePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(250, 250));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 2));

        // Fake QR code using text pattern - maximize size to fill card
        JTextArea qrCode = new JTextArea();
        qrCode.setEditable(false);
        qrCode.setFont(new Font("Courier New", Font.BOLD, 20)); // Increased to 20px bold for maximum fill
        qrCode.setBackground(Color.WHITE);
        qrCode.setText(generateFakeQR());
        qrCode.setFocusable(false);
        qrCode.setBorder(null);
        qrCode.setMargin(new Insets(0, 0, 0, 0)); // No margin

        panel.add(qrCode, new GridBagConstraints());

        return panel;
    }

    private String generateFakeQR() {
        // Simple fake QR code pattern

        String pattern = "█▀▀▀▀▀█ ▀▀█▄ █▀▀▀▀▀█\n" +
                "█ ███ █ ▀▄▀█ █ ███ █\n" +
                "█ ▀▀▀ █ █▄ ▀ █ ▀▀▀ █\n" +
                "▀▀▀▀▀▀▀ █▄█ █ ▀▀▀▀▀▀\n" +
                " ▀█▄█▀▀▀ ▄██ ▀█  █▀▄\n" +
                "█▀▀ ▀▀█▄▀ ▄█▀▀▄█▀▄▀▀\n" +
                "▄█▀▄█ ▀███▄  ▄▄▀ ▄  \n" +
                "█▀▀▀▀▀█ ▀ ▀█ █▄█ ▀ ▀\n" +
                "█ ███ █ ▄███▀▀▀▄▄█▀\n" +
                "█ ▀▀▀ █ ██▄█ ▀ █▄ ▄▀\n" +
                "▀▀▀▀▀▀▀ ▀   ▀ ▀ ▀  ▀";

        return pattern;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15", "[right]10[grow]", "[]5[]5[]5[]"));
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));

        addInfoRow(panel, "Merchant:", "KEDAI KOPI CAK BUDI");
        addInfoRow(panel, "NMID:", "ID1234567890123456");
        addInfoRow(panel, "Lokasi:", "Malang, Jawa Timur");
        addInfoRow(panel, "Jumlah:", String.format("Rp%,.2f", amount));

        return panel;
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(UIComponents.FONT_SMALL);
        lblLabel.setForeground(ColorScheme.TEXT_SECONDARY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(UIComponents.FONT_BODY);
        lblValue.setForeground(ColorScheme.TEXT_PRIMARY);

        panel.add(lblLabel);
        panel.add(lblValue, "wrap");
    }

    private void simulatePayment() {
        lblStatus.setText("Pembayaran Berhasil!");
        lblStatus.setForeground(ColorScheme.ACCENT_GREEN);
        lblStatus.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD));

        success = true;

        // Auto close after 1.5 seconds
        Timer closeTimer = new Timer(1500, e -> dispose());
        closeTimer.setRepeats(false);
        closeTimer.start();
    }

    public boolean isSuccess() {
        return success;
    }
}
