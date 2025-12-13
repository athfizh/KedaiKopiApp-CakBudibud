package com.kedaikopi.ui.dialogs;

import com.kedaikopi.ui.components.UIComponents;
import com.kedaikopi.util.ColorScheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Payment Method Selection Dialog
 * Allows cashier to select payment method and shows appropriate payment info
 */
public class PaymentMethodDialog extends JDialog {

    private String selectedMethod = "Cash";
    private boolean confirmed = false;
    private double totalAmount;
    private double cashAmount;
    private double change;

    private JTextField txtCashAmount;
    private JLabel lblChange;
    private ButtonGroup paymentGroup;

    public PaymentMethodDialog(Window owner, double totalAmount) {
        super(owner, "Pilih Metode Pembayaran", Dialog.ModalityType.APPLICATION_MODAL);
        this.totalAmount = totalAmount;
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 15", "[grow]", "[]10[]10[]15[]"));
        setBackground(Color.WHITE);

        // Title
        JLabel lblTitle = UIComponents.createLabel("Metode Pembayaran", UIComponents.LabelType.SUBHEADING);
        add(lblTitle, "wrap");

        // Amount info
        JPanel amountPanel = createAmountPanel();
        add(amountPanel, "growx, wrap");

        // Payment methods
        JPanel methodsPanel = createPaymentMethodsPanel();
        add(methodsPanel, "growx, wrap");

        // Buttons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, "growx");

        setSize(700, 540); // Taller to show all buttons
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private JPanel createAmountPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[]3[]"));
        panel.setBackground(new Color(59, 130, 246, 20));
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.ACCENT_BLUE, 1));

        JLabel lblLabel = new JLabel("Total Pembayaran:");
        lblLabel.setFont(UIComponents.FONT_BODY);
        lblLabel.setForeground(ColorScheme.TEXT_SECONDARY);

        JLabel lblAmount = new JLabel(String.format("Rp%,.2f", totalAmount));
        lblAmount.setFont(UIComponents.FONT_HEADING.deriveFont(Font.BOLD, 20f));
        lblAmount.setForeground(ColorScheme.ACCENT_BLUE);

        panel.add(lblLabel, "wrap");
        panel.add(lblAmount);

        return panel;
    }

    private JPanel createPaymentMethodsPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 10", "[grow][grow]", "[]8[]8[]8[]8[]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        paymentGroup = new ButtonGroup();

        // Cash with amount input - FULL WIDTH (span 2)
        JRadioButton rbCash = createPaymentRadio("Cash", "Pembayaran tunai");
        panel.add(rbCash, "span 2, wrap");

        // Cash amount panel - FULL WIDTH (span 2)
        JPanel cashPanel = new JPanel(new MigLayout("insets 0 30 0 0", "[]10[200]20[]", "[]"));
        cashPanel.setBackground(Color.WHITE);

        JLabel lblCashAmount = new JLabel("Uang Diterima:");
        lblCashAmount.setFont(UIComponents.FONT_SMALL);
        txtCashAmount = UIComponents.createNumberField(15);
        txtCashAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculateChange();
            }
        });

        lblChange = new JLabel("Kembalian: Rp0");
        lblChange.setFont(UIComponents.FONT_BODY.deriveFont(Font.BOLD));
        lblChange.setForeground(ColorScheme.ACCENT_GREEN);

        cashPanel.add(lblCashAmount);
        cashPanel.add(txtCashAmount);
        cashPanel.add(lblChange);

        panel.add(cashPanel, "span 2, wrap, hidemode 3");

        // TWO COLUMNS for other payment methods
        // Column 1
        JRadioButton rbDebit = createPaymentRadio("Debit Card", "Kartu debit");
        panel.add(rbDebit);

        // Column 2
        JRadioButton rbCredit = createPaymentRadio("Credit Card", "Kartu kredit");
        panel.add(rbCredit, "wrap");

        // Column 1
        JRadioButton rbQRIS = createPaymentRadio("QRIS", "Scan QR");
        panel.add(rbQRIS);

        // Column 2
        JRadioButton rbGoPay = createPaymentRadio("GoPay", "E-Wallet GoPay");
        panel.add(rbGoPay, "wrap");

        // Column 1
        JRadioButton rbOVO = createPaymentRadio("OVO", "E-Wallet OVO");
        panel.add(rbOVO);

        // Column 2
        JRadioButton rbDANA = createPaymentRadio("DANA", "E-Wallet DANA");
        panel.add(rbDANA, "wrap");

        // Column 1
        JRadioButton rbShopeePay = createPaymentRadio("ShopeePay", "E-Wallet ShopeePay");
        panel.add(rbShopeePay, "span 2");

        // Show/hide cash panel based on selection
        rbCash.addActionListener(e -> cashPanel.setVisible(true));
        rbDebit.addActionListener(e -> cashPanel.setVisible(false));
        rbCredit.addActionListener(e -> cashPanel.setVisible(false));
        rbQRIS.addActionListener(e -> cashPanel.setVisible(false));
        rbGoPay.addActionListener(e -> cashPanel.setVisible(false));
        rbOVO.addActionListener(e -> cashPanel.setVisible(false));
        rbDANA.addActionListener(e -> cashPanel.setVisible(false));
        rbShopeePay.addActionListener(e -> cashPanel.setVisible(false));

        rbCash.setSelected(true);

        return panel;
    }

    private JRadioButton createPaymentRadio(String method, String description) {
        JRadioButton radio = new JRadioButton(
                "<html><b>" + method + "</b><br>" +
                        "<span style='font-size:10px; color:#6B7280;'>" + description + "</span></html>");
        radio.setBackground(Color.WHITE);
        radio.setFocusPainted(false);
        radio.setFont(UIComponents.FONT_BODY);
        radio.addActionListener(e -> selectedMethod = method);

        paymentGroup.add(radio);

        return radio;
    }

    private void calculateChange() {
        try {
            String cashStr = txtCashAmount.getText().trim();
            if (!cashStr.isEmpty()) {
                cashAmount = Double.parseDouble(cashStr);
                change = cashAmount - totalAmount;

                if (change >= 0) {
                    lblChange.setText(String.format("Kembalian: Rp%,.0f", change));
                    lblChange.setForeground(ColorScheme.ACCENT_GREEN);
                } else {
                    lblChange.setText(String.format("Kurang: Rp%,.0f", -change));
                    lblChange.setForeground(ColorScheme.ACCENT_RED);
                }
            } else {
                lblChange.setText("Kembalian: Rp0");
                lblChange.setForeground(ColorScheme.TEXT_SECONDARY);
            }
        } catch (NumberFormatException e) {
            lblChange.setText("Kembalian: Rp0");
            lblChange.setForeground(ColorScheme.TEXT_SECONDARY);
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "push[]10[]"));
        panel.setOpaque(false); // No white background

        JButton btnCancel = UIComponents.createButton("Batal", UIComponents.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = UIComponents.createButton("Konfirmasi Pembayaran", UIComponents.ButtonType.SUCCESS);
        btnConfirm.addActionListener(e -> processPayment());

        panel.add(btnCancel);
        panel.add(btnConfirm);

        return panel;
    }

    private void processPayment() {
        // Validate based on payment method
        if ("Cash".equals(selectedMethod)) {
            if (txtCashAmount.getText().trim().isEmpty()) {
                UIComponents.showError((JFrame) getOwner(), "Masukkan jumlah uang yang diterima!");
                return;
            }

            try {
                cashAmount = Double.parseDouble(txtCashAmount.getText().trim());
                if (cashAmount < totalAmount) {
                    UIComponents.showError((JFrame) getOwner(),
                            String.format("Uang tidak cukup! Kurang: Rp%,.0f", totalAmount - cashAmount));
                    return;
                }
                change = cashAmount - totalAmount;
            } catch (NumberFormatException e) {
                UIComponents.showError((JFrame) getOwner(), "Jumlah uang tidak valid!");
                return;
            }
        } else {
            // For non-cash, show payment-specific dialog
            boolean success = showPaymentDialog();
            if (!success) {
                return;
            }
            cashAmount = totalAmount;
            change = 0;
        }

        confirmed = true;
        dispose();
    }

    private boolean showPaymentDialog() {
        switch (selectedMethod) {
            case "Debit Card":
            case "Credit Card":
                EDCDialog edcDialog = new EDCDialog((JFrame) getOwner(), selectedMethod, totalAmount);
                edcDialog.setVisible(true);
                return edcDialog.isSuccess();

            case "QRIS":
                QRISDialog qrisDialog = new QRISDialog((JFrame) getOwner(), totalAmount);
                qrisDialog.setVisible(true);
                return qrisDialog.isSuccess();

            case "GoPay":
            case "OVO":
            case "DANA":
            case "ShopeePay":
                EWalletDialog ewalletDialog = new EWalletDialog((JFrame) getOwner(), selectedMethod, totalAmount);
                ewalletDialog.setVisible(true);
                return ewalletDialog.isSuccess();

            default:
                return true;
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getSelectedMethod() {
        return selectedMethod;
    }

    public double getCashAmount() {
        return cashAmount;
    }

    public double getChange() {
        return change;
    }
}
