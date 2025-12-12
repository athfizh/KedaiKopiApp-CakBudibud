package com.kedaikopi.model;

import com.kedaikopi.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Model TransaksiHeader - Header for transactions
 */
public class TransaksiHeader {

    private static final Logger logger = LoggerFactory.getLogger(TransaksiHeader.class);

    private int idTransaksiHeader;
    private int idUser;
    private Timestamp tanggal;
    private double totalHarga;
    private double pajak;
    private double grandTotal;
    private double tunai;
    private double kembalian;
    private String metodePembayaran;
    private Timestamp createdAt;

    // Relasi
    private User user;
    private List<TransaksiDetail> details;

    public TransaksiHeader() {
        this.details = new ArrayList<>();
        this.metodePembayaran = "Cash";
    }

    // Getters and Setters
    public int getIdTransaksiHeader() {
        return idTransaksiHeader;
    }

    public void setIdTransaksiHeader(int idTransaksiHeader) {
        this.idTransaksiHeader = idTransaksiHeader;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public Timestamp getTanggal() {
        return tanggal;
    }

    public void setTanggal(Timestamp tanggal) {
        this.tanggal = tanggal;
    }

    public double getTotalHarga() {
        return totalHarga;
    }

    public void setTotalHarga(double totalHarga) {
        this.totalHarga = totalHarga;
    }

    public double getPajak() {
        return pajak;
    }

    public void setPajak(double pajak) {
        this.pajak = pajak;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public double getTunai() {
        return tunai;
    }

    public void setTunai(double tunai) {
        this.tunai = tunai;
    }

    public double getKembalian() {
        return kembalian;
    }

    public void setKembalian(double kembalian) {
        this.kembalian = kembalian;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public void setMetodePembayaran(String metodePembayaran) {
        this.metodePembayaran = metodePembayaran;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<TransaksiDetail> getDetails() {
        return details;
    }

    public void setDetails(List<TransaksiDetail> details) {
        this.details = details;
    }

    /**
     * Save transaction (header + details) in a single database transaction
     */
    public boolean save() {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false); // Start transaction

            logger.info("Starting transaction save - User ID: {}, Total: {}, Items: {}",
                    this.idUser, this.totalHarga, this.details.size());

            // Insert header - Match user's ACTUAL database schema
            String headerSql = "INSERT INTO tbl_transaksi_header (id_user, tanggal, total_harga, pajak, grand_total, uang_bayar, kembalian, nama_kasir, status, metode_pembayaran) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_transaksi_header";

            PreparedStatement headerStmt = conn.prepareStatement(headerSql);
            headerStmt.setInt(1, this.idUser);
            headerStmt.setTimestamp(2, this.tanggal);
            headerStmt.setDouble(3, this.totalHarga);
            headerStmt.setDouble(4, this.pajak); // pajak
            headerStmt.setDouble(5, this.grandTotal); // grand_total
            headerStmt.setDouble(6, this.tunai); // uang_bayar
            headerStmt.setDouble(7, this.kembalian);
            headerStmt.setString(8, this.user != null ? this.user.getNamaLengkap() : "Unknown"); // nama_kasir
            headerStmt.setString(9, "completed"); // status
            headerStmt.setString(10, this.metodePembayaran != null ? this.metodePembayaran : "Cash"); // metode_pembayaran

            ResultSet rs = headerStmt.executeQuery();
            if (rs.next()) {
                this.idTransaksiHeader = rs.getInt(1);
                logger.info("Transaction header inserted successfully - ID: {}", this.idTransaksiHeader);
            } else {
                conn.rollback();
                logger.error("Failed to insert transaction header - no ID returned");
                return false;
            }

            // Insert details - FIXED: include nama_menu (required by database)
            String detailSql = "INSERT INTO tbl_transaksi_detail (id_transaksi_header, id_menu, nama_menu, harga, qty, subtotal) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement detailStmt = conn.prepareStatement(detailSql);

            for (TransaksiDetail detail : this.details) {
                // Get menu info to fill nama_menu
                MenuKopi menu = MenuKopi.getById(detail.getIdMenu());
                if (menu == null) {
                    conn.rollback();
                    logger.error("Menu not found - ID: {}", detail.getIdMenu());
                    return false;
                }

                detailStmt.setInt(1, this.idTransaksiHeader);
                detailStmt.setInt(2, detail.getIdMenu());
                detailStmt.setString(3, menu.getNamaMenu()); // nama_menu (from MenuKopi)
                detailStmt.setDouble(4, detail.getHargaSatuan()); // Column name is 'harga' in database
                detailStmt.setInt(5, detail.getQty());
                detailStmt.setDouble(6, detail.getSubtotal());
                detailStmt.addBatch();

                logger.debug("Added detail to batch - Menu: {}, Qty: {}",
                        menu.getNamaMenu(), detail.getQty());

                // Reduce stock within same transaction
                logger.debug("Attempting to reduce stock for {} - Current: {}, Reduce: {}",
                        menu.getNamaMenu(), menu.getStok(), detail.getQty());

                // Reduce stock (menu already fetched above)
                if (!menu.kurangiStok(detail.getQty(), conn)) {
                    conn.rollback();
                    logger.error("Failed to reduce stock for menu: {} - Stock: {}, Requested: {}",
                            menu.getNamaMenu(), menu.getStok(), detail.getQty());
                    return false;
                }

                logger.debug("Stock reduced successfully for {}", menu.getNamaMenu());
            }

            int[] batchResults = detailStmt.executeBatch();
            logger.info("Transaction details inserted - {} rows affected", batchResults.length);

            conn.commit(); // Commit transaction

            logger.info("Transaction saved successfully. ID: {}, Total: Rp{}",
                    this.idTransaksiHeader, this.totalHarga);
            return true;

        } catch (

        SQLException e) {
            logger.error("SQL Error saving transaction - Code: {}, State: {}, Message: {}",
                    e.getErrorCode(), e.getSQLState(), e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.info("Transaction rolled back");
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
    }

    /**
     * Get all transactions
     */
    public static List<TransaksiHeader> getAll() {
        List<TransaksiHeader> transactions = new ArrayList<>();
        String sql = "SELECT th.*, u.username, u.nama_lengkap " +
                "FROM tbl_transaksi_header th " +
                "LEFT JOIN tbl_user u ON th.id_user = u.id_user " +
                "ORDER BY th.tanggal DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                transactions.add(mapResultSetToHeader(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting all transactions", e);
        }

        return transactions;
    }

    /**
     * Get transaction by ID with details
     */
    public static TransaksiHeader getById(int id) {
        String sql = "SELECT th.*, u.username, u.nama_lengkap " +
                "FROM tbl_transaksi_header th " +
                "LEFT JOIN tbl_user u ON th.id_user = u.id_user " +
                "WHERE th.id_transaksi_header = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                TransaksiHeader header = mapResultSetToHeader(rs);
                // Load details
                header.setDetails(TransaksiDetail.getByTransaksiHeader(id));
                return header;
            }

        } catch (SQLException e) {
            logger.error("Error getting transaction by ID: {}", id, e);
        }

        return null;
    }

    /**
     * Get transactions for today
     */
    public static List<TransaksiHeader> getToday() {
        List<TransaksiHeader> transactions = new ArrayList<>();
        String sql = "SELECT th.*, u.username, u.nama_lengkap " +
                "FROM tbl_transaksi_header th " +
                "LEFT JOIN tbl_user u ON th.id_user = u.id_user " +
                "WHERE DATE(th.tanggal) = CURRENT_DATE " +
                "ORDER BY th.tanggal DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                transactions.add(mapResultSetToHeader(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting today's transactions", e);
        }

        return transactions;
    }

    /**
     * Helper method to map ResultSet to TransaksiHeader
     */
    private static TransaksiHeader mapResultSetToHeader(ResultSet rs) throws SQLException {
        TransaksiHeader header = new TransaksiHeader();
        header.setIdTransaksiHeader(rs.getInt("id_transaksi_header"));
        header.setIdUser(rs.getInt("id_user"));
        header.setTanggal(rs.getTimestamp("tanggal"));
        header.setTotalHarga(rs.getDouble("total_harga"));
        header.setTunai(rs.getDouble("tunai"));
        header.setKembalian(rs.getDouble("kembalian"));
        header.setMetodePembayaran(rs.getString("metode_pembayaran"));
        header.setCreatedAt(rs.getTimestamp("created_at"));

        // Map user if available
        try {
            User user = new User();
            user.setIdUser(rs.getInt("id_user"));
            user.setUsername(rs.getString("username"));
            user.setNamaLengkap(rs.getString("nama_lengkap"));
            header.setUser(user);
        } catch (SQLException e) {
            // User fields might not be in the result set
        }

        return header;
    }

    @Override
    public String toString() {
        return "Transaksi #" + idTransaksiHeader;
    }
}
