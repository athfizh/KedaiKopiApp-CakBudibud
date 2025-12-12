package com.kedaikopi.ui.panels;

import com.kedaikopi.model.*;
import com.kedaikopi.ui.components.UIComponents;
import com.kedaikopi.util.ColorScheme;
import com.kedaikopi.util.ToastNotification;
import com.kedaikopi.util.ReceiptPrinter;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Kasir Panel - Point of Sale (POS) System
 */
public class KasirPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(KasirPanel.class);
    private User currentUser;
    private NumberFormat currencyFormat;

    // Left side - Product list
    private JPanel productPanel;
    private JTextField txtSearch;
    private List<MenuKopi> allMenus;

    // Right side - Shopping cart
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel lblSubtotal, lblTotal;
    private JButton btnPay, btnClearCart;

    // Cart data
    private List<CartItem> cartItems;

    public KasirPanel(User user) {
        this.currentUser = user;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        this.cartItems = new ArrayList<>();
        initComponents();
        loadData();
    }

    private void initComponents() {
        // Compact layout with taller cards (20px insets, 10px gaps)
        setLayout(new MigLayout("fill, insets 20, gap 10", "[grow, fill][450!]", "[]15[600!]"));
        setBackground(ColorScheme.BG_LIGHT);

        // Title - top left corner
        JLabel title = UIComponents.createLabel("Kasir", UIComponents.LabelType.TITLE);
        add(title, "span 2, wrap");

        // Left panel - Products (fixed 600px height with scroll)
        JPanel leftPanel = createProductPanel();
        add(leftPanel, "grow, h 600!");

        // Right panel - Cart (fixed 600px height with scroll)
        JPanel rightPanel = createCartPanel();
        add(rightPanel, "grow, h 600!");
    }

    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Header with search bar
        JPanel headerPanel = new JPanel(new BorderLayout(8, 8));
        headerPanel.setBackground(Color.WHITE);

        JLabel lblHeader = UIComponents.createLabel("Daftar Menu", UIComponents.LabelType.HEADING);
        headerPanel.add(lblHeader, BorderLayout.NORTH);

        // Search bar only (no category dropdown)
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);

        txtSearch = UIComponents.createTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari menu...");
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                searchProducts();
            }
        });

        JButton btnSearch = UIComponents.createButton("Cari", UIComponents.ButtonType.PRIMARY);
        btnSearch.addActionListener(e -> searchProducts());

        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);

        headerPanel.add(searchPanel, BorderLayout.SOUTH);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Products grid - 2 COLUMNS with minimal gaps
        productPanel = new JPanel();
        // Minimal insets and gaps for compact layout
        productPanel.setLayout(
                new MigLayout("wrap 2, insets 5, fillx, aligny top",
                        "[grow, fill]10[grow, fill]",
                        "[]10[]10[]10[]10[]10[]")); // Multiple rows with 10px gap
        productPanel.setBackground(Color.WHITE);

        // Scroll pane with smooth scrolling
        JScrollPane scrollPane = new JScrollPane(productPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Smooth scrolling with larger increment
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[]8[grow]8[]6[]6[]8[]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // Header
        JLabel lblHeader = UIComponents.createLabel("Keranjang Belanja", UIComponents.LabelType.HEADING);
        panel.add(lblHeader, "wrap");

        // Cart table
        String[] columns = { "Menu", "Harga", "Qty", "Subtotal", "" };
        cartModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only delete button column
            }
        };

        cartTable = UIComponents.createStyledTable(cartModel);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(60);

        // Add delete button renderer/editor
        cartTable.getColumnModel().getColumn(4).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JButton btn = new JButton("X");
            btn.setFont(new Font("Arial", Font.BOLD, 10));
            btn.setForeground(Color.WHITE);
            btn.setBackground(ColorScheme.BUTTON_DANGER);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            return btn;
        });

        cartTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = cartTable.rowAtPoint(e.getPoint());
                int col = cartTable.columnAtPoint(e.getPoint());
                if (col == 4 && row >= 0) {
                    removeFromCart(row);
                }
            }
        });

        // Add mouse wheel listener to table to forward to scroll pane
        cartTable.addMouseWheelListener(e -> {
            // Forward mouse wheel event to parent scroll pane
            cartTable.getParent().dispatchEvent(e);
        });

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));

        // Enable mouse wheel scrolling
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Add explicit listener to guarantee mouse wheel works
        scrollPane.addMouseWheelListener(e -> {
            JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
            int amount = e.getUnitsToScroll() * scrollBar.getUnitIncrement();
            scrollBar.setValue(scrollBar.getValue() + amount);
        });

        panel.add(scrollPane, "grow, wrap");

        // Summary
        JPanel summaryPanel = new JPanel(new MigLayout("fill, insets 6", "[right]8[grow, right]"));
        summaryPanel.setBackground(new Color(250, 250, 250));
        summaryPanel.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1));

        JLabel lblSubtotalLabel = new JLabel("Subtotal:");
        lblSubtotalLabel.setFont(UIComponents.FONT_BODY);
        lblSubtotal = new JLabel("Rp 0");
        lblSubtotal.setFont(UIComponents.FONT_BODY);

        JLabel lblTaxLabel = new JLabel("Pajak (10%):");
        lblTaxLabel.setFont(UIComponents.FONT_BODY);
        JLabel lblTax = new JLabel("Rp 0");
        lblTax.setFont(UIComponents.FONT_BODY);
        lblTax.setName("lblTax"); // For easy retrieval

        JLabel lblTotalLabel = new JLabel("TOTAL:");
        lblTotalLabel.setFont(UIComponents.FONT_HEADING);
        lblTotal = new JLabel("Rp 0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotal.setForeground(ColorScheme.ACCENT_GREEN);

        summaryPanel.add(lblSubtotalLabel);
        summaryPanel.add(lblSubtotal, "wrap");
        summaryPanel.add(lblTaxLabel);
        summaryPanel.add(lblTax, "wrap");
        summaryPanel.add(new JSeparator(), "span 2, growx, wrap, gaptop 5, gapbottom 5");
        summaryPanel.add(lblTotalLabel);
        summaryPanel.add(lblTotal, "wrap");

        // Tax note
        JLabel lblNote = new JLabel("* Harga sudah termasuk pajak 10% *");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblNote.setForeground(new Color(100, 100, 100));
        summaryPanel.add(lblNote, "span 2, center, gaptop 3");

        panel.add(summaryPanel, "growx, wrap");

        // Action buttons
        JPanel actionPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow]"));
        actionPanel.setBackground(Color.WHITE);

        btnClearCart = UIComponents.createButton("Bersihkan", UIComponents.ButtonType.SECONDARY);
        btnPay = UIComponents.createButton("BAYAR", UIComponents.ButtonType.SUCCESS);
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 16));

        btnClearCart.addActionListener(e -> clearCart());
        btnPay.addActionListener(e -> processPayment());

        actionPanel.add(btnClearCart, "grow");
        actionPanel.add(btnPay, "grow");

        panel.add(actionPanel, "growx");

        return panel;
    }

    private void loadData() {
        allMenus = MenuKopi.getAllActive();
        displayProducts(allMenus);
    }

    private void displayProducts(List<MenuKopi> menus) {
        productPanel.removeAll();
        logger.info("=== DISPLAYING PRODUCTS ===");
        logger.info("[DEBUG] Displaying {} menu cards in 2-column grid", menus.size());

        // Add cards directly to GridLayout
        for (int i = 0; i < menus.size(); i++) {
            MenuKopi menu = menus.get(i);
            JPanel productCard = createProductCard(menu);
            productPanel.add(productCard);
            logger.info("  Card #{}: {}", (i + 1), menu.getNamaMenu());
        }

        // Add filler if odd number
        if (menus.size() % 2 == 1) {
            JPanel filler = new JPanel();
            filler.setBackground(Color.WHITE);
            productPanel.add(filler);
        }

        productPanel.revalidate();
        productPanel.repaint();

        logger.info("[DEBUG] {} cards displayed in 2-column grid", menus.size());
        logger.info("===========================");
    }

    private JPanel createProductCard(MenuKopi menu) {
        // Compact card with FIXED preferred height to prevent stretching
        JPanel card = new JPanel(new MigLayout("fill, insets 8", "[grow]", "[]3[]3[]3[]5[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        // Set FIXED preferred size to prevent vertical stretching
        card.setPreferredSize(new Dimension(0, 190)); // Fixed height
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        // Menu name
        JLabel lblName = new JLabel(menu.getNamaMenu());
        lblName.setFont(UIComponents.FONT_BODY_BOLD);
        card.add(lblName, "wrap");

        // Category
        JLabel lblCategory = new JLabel(menu.getKategori().getNamaKategori());
        lblCategory.setFont(UIComponents.FONT_SMALL);
        lblCategory.setForeground(ColorScheme.TEXT_SECONDARY);
        card.add(lblCategory, "wrap");

        // Price
        JLabel lblPrice = new JLabel(currencyFormat.format(menu.getHarga()));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPrice.setForeground(ColorScheme.ACCENT_GREEN);
        card.add(lblPrice, "wrap");

        // Stock info
        JLabel lblStock = new JLabel("Stok: " + menu.getStok());
        lblStock.setFont(UIComponents.FONT_SMALL);
        if (menu.getStok() < 10) {
            lblStock.setForeground(ColorScheme.ACCENT_ORANGE);
        }
        card.add(lblStock, "wrap");

        // Add to cart button
        JButton btnAdd = UIComponents.createButton("Tambah", UIComponents.ButtonType.PRIMARY);
        btnAdd.addActionListener(e -> addToCart(menu));
        btnAdd.setEnabled(menu.getStok() > 0);
        card.add(btnAdd, "growx, h 32!");

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ColorScheme.ACCENT_BLUE, 2),
                        BorderFactory.createEmptyBorder(4, 4, 4, 4)));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }
        });

        return card;
    }

    private void searchProducts() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            // Display all menus when search is empty
            displayProducts(allMenus);
            return;
        }

        List<MenuKopi> filtered = new ArrayList<>();
        for (MenuKopi menu : allMenus) {
            if (menu.getNamaMenu().toLowerCase().contains(keyword) ||
                    menu.getKategori().getNamaKategori().toLowerCase().contains(keyword)) {
                filtered.add(menu);
            }
        }
        displayProducts(filtered);
    }

    private void addToCart(MenuKopi menu) {
        // Check if item already in cart
        for (CartItem item : cartItems) {
            if (item.getMenu().getIdMenu() == menu.getIdMenu()) {
                // Increase quantity
                if (item.getQty() < menu.getStok()) {
                    item.setQty(item.getQty() + 1);
                    updateCartDisplay();
                    return;
                } else {
                    UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                            "Stok tidak mencukupi!");
                    return;
                }
            }
        }

        // Add new item to cart
        CartItem newItem = new CartItem(menu, 1);
        cartItems.add(newItem);
        updateCartDisplay();

        // Show toast notification
        ToastNotification.showSuccess(this, menu.getNamaMenu() + " ditambahkan ke keranjang");
        logger.info("Added to cart: {} x1", menu.getNamaMenu());
    }

    private void removeFromCart(int row) {
        if (row >= 0 && row < cartItems.size()) {
            CartItem removed = cartItems.remove(row);
            updateCartDisplay();
            ToastNotification.showInfo(this, removed.getMenu().getNamaMenu() + " dihapus dari keranjang");
            logger.info("Removed from cart: {}", removed.getMenu().getNamaMenu());
        }
    }

    private void updateCartDisplay() {
        cartModel.setRowCount(0);
        double subtotal = 0;

        for (CartItem item : cartItems) {
            double itemSubtotal = item.getQty() * item.getMenu().getHarga();
            subtotal += itemSubtotal;

            cartModel.addRow(new Object[] {
                    item.getMenu().getNamaMenu(),
                    currencyFormat.format(item.getMenu().getHarga()),
                    item.getQty(),
                    currencyFormat.format(itemSubtotal),
                    "X"
            });
        }

        lblSubtotal.setText(currencyFormat.format(subtotal));

        // Calculate tax (10%)
        double tax = subtotal * 0.10;
        double total = subtotal + tax;

        // Update tax label if it exists
        Component[] components = ((JPanel) lblSubtotal.getParent()).getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel && "lblTax".equals(comp.getName())) {
                ((JLabel) comp).setText(currencyFormat.format(tax));
                break;
            }
        }

        lblTotal.setText(currencyFormat.format(total));

        btnPay.setEnabled(!cartItems.isEmpty());
    }

    private void clearCart() {
        if (cartItems.isEmpty()) {
            return;
        }

        boolean confirm = UIComponents.showConfirm((JFrame) SwingUtilities.getWindowAncestor(this),
                "Yakin ingin mengosongkan keranjang?",
                "Konfirmasi");

        if (confirm) {
            cartItems.clear();
            updateCartDisplay();
            logger.info("Cart cleared");
        }
    }

    private void processPayment() {
        if (cartItems.isEmpty()) {
            UIComponents.showError((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Keranjang masih kosong!");
            return;
        }

        // Calculate subtotal and tax
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getQty() * item.getMenu().getHarga();
        }

        double tax = subtotal * 0.10; // 10% tax
        double grandTotal = subtotal + tax;

        PaymentDialog paymentDialog = new PaymentDialog(SwingUtilities.getWindowAncestor(this), subtotal, tax,
                grandTotal);
        paymentDialog.setVisible(true);

        if (paymentDialog.isConfirmed()) {
            // Create transaction
            TransaksiHeader transaksi = new TransaksiHeader();
            transaksi.setIdUser(currentUser.getIdUser());
            transaksi.setTanggal(Timestamp.valueOf(LocalDateTime.now()));
            transaksi.setTotalHarga(subtotal);
            transaksi.setPajak(tax);
            transaksi.setGrandTotal(grandTotal);
            transaksi.setTunai(paymentDialog.getTunai());
            transaksi.setKembalian(paymentDialog.getKembalian());
            transaksi.setMetodePembayaran("Cash");
            transaksi.setUser(currentUser);

            // Add details
            List<TransaksiDetail> details = new ArrayList<>();
            for (CartItem item : cartItems) {
                TransaksiDetail detail = new TransaksiDetail(
                        item.getMenu().getIdMenu(),
                        item.getQty(),
                        item.getMenu().getHarga());
                detail.setMenu(item.getMenu()); // Set Menu object for receipt printer
                details.add(detail);
            }
            transaksi.setDetails(details);

            // Save transaction
            if (transaksi.save()) {
                // Show success toast
                ToastNotification.showSuccess(this,
                        "Transaksi berhasil! Total: " + currencyFormat.format(grandTotal));

                // Clear cart and reload products
                cartItems.clear();
                updateCartDisplay();
                loadData(); // Reload to update stock display

                logger.info("Transaction completed. ID: {}", transaksi.getIdTransaksiHeader());

                // Show receipt print preview (BLOCKS until closed)
                Window parentWindow = SwingUtilities.getWindowAncestor(this);
                ReceiptPrinter.showPrintPreview(parentWindow, transaksi);

                // Auto-refresh dashboard to show updated data
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof com.kedaikopi.ui.MainFrame) {
                    ((com.kedaikopi.ui.MainFrame) window).refreshDashboard();
                }

            } else {
                ToastNotification.showError(this, "Gagal menyimpan transaksi!");
                logger.error("Failed to save transaction");
            }
        }
    }
}

/**
 * Inner class to represent cart items
 */
class CartItem {
    private MenuKopi menu;
    private int qty;

    public CartItem(MenuKopi menu, int qty) {
        this.menu = menu;
        this.qty = qty;
    }

    public MenuKopi getMenu() {
        return menu;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
}

/**
 * Payment Dialog
 */
class PaymentDialog extends JDialog {
    private double subtotal;
    private double tax;
    private double grandTotal;
    private double tunai;
    private double kembalian;
    private boolean confirmed = false;

    private JLabel lblSubtotal, lblTax, lblGrandTotal, lblKembalian;
    private JTextField txtTunai;

    public PaymentDialog(Window owner, double subtotal, double tax, double grandTotal) {
        super(owner, "Pembayaran", Dialog.ModalityType.APPLICATION_MODAL);
        this.subtotal = subtotal;
        this.tax = tax;
        this.grandTotal = grandTotal;
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 25", "[right]15[350!, grow]", "[]10[]10[]15[]15[]20[]"));
        setBackground(Color.WHITE);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        // Subtotal
        JLabel lblSubtotalLabel = new JLabel("Subtotal:");
        lblSubtotalLabel.setFont(UIComponents.FONT_BODY);
        lblSubtotal = new JLabel(currencyFormat.format(subtotal));
        lblSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 16));

        add(lblSubtotalLabel);
        add(lblSubtotal, "wrap");

        // Tax
        JLabel lblTaxLabel = new JLabel("Pajak (10%):");
        lblTaxLabel.setFont(UIComponents.FONT_BODY);
        lblTax = new JLabel(currencyFormat.format(tax));
        lblTax.setFont(new Font("Segoe UI", Font.BOLD, 16));

        add(lblTaxLabel);
        add(lblTax, "wrap");

        // Separator
        add(new JSeparator(), "span 2, growx, wrap, gaptop 5, gapbottom 5");

        // Grand Total
        JLabel lblGrandTotalLabel = new JLabel("Total Belanja:");
        lblGrandTotalLabel.setFont(UIComponents.FONT_HEADING);
        lblGrandTotal = new JLabel(currencyFormat.format(grandTotal));
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblGrandTotal.setForeground(ColorScheme.ACCENT_GREEN);

        add(lblGrandTotalLabel);
        add(lblGrandTotal, "wrap");

        // Tax inclusion note
        JLabel lblNote = new JLabel("* Total sudah termasuk pajak 10% *");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNote.setForeground(new Color(100, 100, 100));
        add(lblNote, "skip, center, wrap");

        // Tunai input
        JLabel lblTunaiLabel = new JLabel("Tunai:");
        lblTunaiLabel.setFont(UIComponents.FONT_HEADING);
        txtTunai = UIComponents.createNumberField(20);
        txtTunai.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtTunai.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                calculateChange();
            }
        });

        add(lblTunaiLabel);
        add(txtTunai, "growx, wrap");

        // Kembalian
        JLabel lblKembalianLabel = new JLabel("Kembalian:");
        lblKembalianLabel.setFont(UIComponents.FONT_HEADING);
        lblKembalian = new JLabel("Rp 0");
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblKembalian.setForeground(ColorScheme.ACCENT_BLUE);

        add(lblKembalianLabel);
        add(lblKembalian, "wrap");

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]"));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnConfirm = UIComponents.createButton("KONFIRMASI", UIComponents.ButtonType.SUCCESS);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JButton btnCancel = UIComponents.createButton("Batal", UIComponents.ButtonType.SECONDARY);

        btnConfirm.addActionListener(e -> confirmPayment());
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnConfirm, "grow");
        buttonPanel.add(btnCancel, "grow");

        add(buttonPanel, "span 2, growx");

        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);

        // Focus on tunai field
        SwingUtilities.invokeLater(() -> txtTunai.requestFocus());
    }

    private void calculateChange() {
        try {
            String tunaiStr = txtTunai.getText().trim();
            if (!tunaiStr.isEmpty()) {
                tunai = Double.parseDouble(tunaiStr);
                kembalian = tunai - grandTotal;

                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                lblKembalian.setText(currencyFormat.format(kembalian));

                if (kembalian < 0) {
                    lblKembalian.setForeground(ColorScheme.BUTTON_DANGER);
                } else {
                    lblKembalian.setForeground(ColorScheme.ACCENT_BLUE);
                }
            }
        } catch (NumberFormatException e) {
            lblKembalian.setText("Rp 0");
        }
    }

    private void confirmPayment() {
        try {
            String tunaiStr = txtTunai.getText().trim();
            if (tunaiStr.isEmpty()) {
                UIComponents.showError((JFrame) getOwner(), "Masukkan jumlah tunai!");
                return;
            }

            tunai = Double.parseDouble(tunaiStr);
            kembalian = tunai - grandTotal;

            if (kembalian < 0) {
                UIComponents.showError((JFrame) getOwner(),
                        "Uang tidak cukup!\nKurang: " +
                                NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(-kembalian));
                return;
            }

            confirmed = true;
            dispose();

        } catch (NumberFormatException e) {
            UIComponents.showError((JFrame) getOwner(), "Jumlah tunai tidak valid!");
        }
    }

    public double getTunai() {
        return tunai;
    }

    public double getKembalian() {
        return kembalian;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
