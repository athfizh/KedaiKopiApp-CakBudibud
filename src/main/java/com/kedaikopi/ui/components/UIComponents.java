package com.kedaikopi.ui.components;

import com.kedaikopi.util.ColorScheme;
import com.kedaikopi.util.IconManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Reusable UI component factory for consistent styling
 */
public class UIComponents {

    // Standard fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);

    /**
     * Create a styled button
     */
    public static JButton createButton(String text, ButtonType type) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

        switch (type) {
            case PRIMARY:
                button.setBackground(ColorScheme.BUTTON_PRIMARY);
                button.setForeground(Color.WHITE);
                addHoverEffect(button, ColorScheme.BUTTON_PRIMARY, ColorScheme.BUTTON_PRIMARY_HOVER);
                break;
            case SUCCESS:
                button.setBackground(ColorScheme.BUTTON_SUCCESS);
                button.setForeground(Color.WHITE);
                addHoverEffect(button, ColorScheme.BUTTON_SUCCESS, ColorScheme.BUTTON_SUCCESS_HOVER);
                break;
            case DANGER:
                button.setBackground(ColorScheme.BUTTON_DANGER);
                button.setForeground(Color.WHITE);
                addHoverEffect(button, ColorScheme.BUTTON_DANGER, ColorScheme.BUTTON_DANGER_HOVER);
                break;
            case SECONDARY:
                button.setBackground(ColorScheme.BUTTON_SECONDARY);
                button.setForeground(Color.WHITE);
                addHoverEffect(button, ColorScheme.BUTTON_SECONDARY, ColorScheme.BUTTON_SECONDARY_HOVER);
                break;
        }

        return button;
    }

    /**
     * Create a button with icon
     */
    public static JButton createIconButton(String text, Icon icon, ButtonType type) {
        JButton button = createButton(text, type);
        button.setIcon(icon);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setIconTextGap(8);
        return button;
    }

    /**
     * Create a styled text field
     */
    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.INPUT_BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)));

        // Focus border
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ColorScheme.INPUT_BORDER_FOCUS, 2),
                        new EmptyBorder(7, 9, 7, 9)));
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ColorScheme.INPUT_BORDER, 1),
                        new EmptyBorder(8, 10, 8, 10)));
            }
        });

        return field;
    }

    /**
     * Create a styled number field (only accepts numbers)
     */
    public static JTextField createNumberField(int columns) {
        JTextField field = createTextField(columns);

        // Only allow numbers
        field.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != '\b') {
                    evt.consume();
                }
            }
        });

        return field;
    }

    /**
     * Create a styled table with modern look
     */
    public static JTable createStyledTable(javax.swing.table.TableModel model) {
        JTable table = new JTable(model);

        // Table styling
        table.setFont(FONT_BODY);
        table.setRowHeight(35);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(true);
        table.setGridColor(ColorScheme.TABLE_GRID);
        table.setSelectionBackground(ColorScheme.TABLE_SELECTION_BG);
        table.setSelectionForeground(ColorScheme.TABLE_SELECTION_FG);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(ColorScheme.TABLE_HEADER_BG);
        header.setForeground(ColorScheme.TABLE_HEADER_FG);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setReorderingAllowed(false);

        // Alternating row colors
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? ColorScheme.TABLE_ROW_EVEN : ColorScheme.TABLE_ROW_ODD);
                }

                setBorder(new EmptyBorder(5, 10, 5, 10));
                return c;
            }
        };

        // Apply renderer to all columns
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        return table;
    }

    /**
     * Create a card-style panel with shadow
     */
    public static JPanel createCard(String title, LayoutManager layout) {
        JPanel card = new JPanel(layout);
        card.setBackground(ColorScheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1),
                new EmptyBorder(15, 15, 15, 15)));

        return card;
    }

    /**
     * Create a statistic card for dashboard
     */
    public static JPanel createStatCard(String title, String value, Color accentColor, Icon icon) {
        // Reduced insets from 20 to 12 for more compact card
        JPanel card = new JPanel(new MigLayout("fill, insets 12", "[grow][]", "[]6[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.CARD_BORDER, 1),
                new EmptyBorder(0, 0, 0, 0)));

        // Set max size to prevent card from being too wide
        card.setMaximumSize(new Dimension(280, 100));
        card.setPreferredSize(new Dimension(260, 95));

        // Title - smaller font
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Reduced from FONT_SMALL (11px)
        lblTitle.setForeground(ColorScheme.TEXT_SECONDARY);

        // Value - smaller font
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Reduced from 28px
        lblValue.setForeground(accentColor);

        // Icon (if provided)
        if (icon != null) {
            JLabel lblIcon = new JLabel(icon);
            card.add(lblTitle, "cell 0 0");
            card.add(lblIcon, "cell 1 0 1 2, center");
            card.add(lblValue, "cell 0 1");
        } else {
            card.add(lblTitle, "wrap");
            card.add(lblValue, "wrap");
        }

        return card;
    }

    /**
     * Create a label with specific styling
     */
    public static JLabel createLabel(String text, LabelType type) {
        JLabel label = new JLabel(text);

        switch (type) {
            case TITLE:
                label.setFont(FONT_TITLE);
                label.setForeground(ColorScheme.TEXT_PRIMARY);
                break;
            case HEADING:
                label.setFont(FONT_HEADING);
                label.setForeground(ColorScheme.TEXT_PRIMARY);
                break;
            case SUBHEADING:
                label.setFont(FONT_SUBHEADING);
                label.setForeground(ColorScheme.TEXT_SECONDARY);
                break;
            case BODY:
                label.setFont(FONT_BODY);
                label.setForeground(ColorScheme.TEXT_PRIMARY);
                break;
            case SMALL:
                label.setFont(FONT_SMALL);
                label.setForeground(ColorScheme.TEXT_SECONDARY);
                break;
        }

        return label;
    }

    /**
     * Show success notification (toast-style)
     */
    public static void showSuccess(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Sukses",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show error notification
     */
    public static void showError(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show confirmation dialog
     */
    public static boolean showConfirm(JFrame parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Add hover effect to button
     */
    private static void addHoverEffect(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalColor);
            }
        });
    }

    /**
     * Create a separator line
     */
    public static JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(ColorScheme.DIVIDER_COLOR);
        return separator;
    }

    // Enums for type-safe component creation
    public enum ButtonType {
        PRIMARY, SUCCESS, DANGER, SECONDARY
    }

    public enum LabelType {
        TITLE, HEADING, SUBHEADING, BODY, SMALL
    }
}
