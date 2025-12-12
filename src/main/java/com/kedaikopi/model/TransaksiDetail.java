package com.kedaikopi.model;

import com.kedaikopi.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Model TransaksiDetail - Line items for transactions
 */
public class TransaksiDetail {

    private static final Logger logger = LoggerFactory.getLogger(TransaksiDetail.class);

    private int idTransaksiDetail;
    private int idTransaksiHeader;
    private int idMenu;
    private int qty;
    private double hargaSatuan;
    private double subtotal;
    private Timestamp createdAt;

    // Relasi
    private MenuKopi menu;

    public TransaksiDetail() {
    }

    public TransaksiDetail(int idMenu, int qty, double hargaSatuan) {
        this.idMenu = idMenu;
        this.qty = qty;
        this.hargaSatuan = hargaSatuan;
        this.subtotal = qty * hargaSatuan;
    }

    // Getters and Setters
    public int getIdTransaksiDetail() {
        return idTransaksiDetail;
    }

    public void setIdTransaksiDetail(int idTransaksiDetail) {
        this.idTransaksiDetail = idTransaksiDetail;
    }

    public int getIdTransaksiHeader() {
        return idTransaksiHeader;
    }

    public void setIdTransaksiHeader(int idTransaksiHeader) {
        this.idTransaksiHeader = idTransaksiHeader;
    }

    public int getIdMenu() {
        return idMenu;
    }

    public void setIdMenu(int idMenu) {
        this.idMenu = idMenu;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
        this.subtotal = qty * this.hargaSatuan;
    }

    public double getHargaSatuan() {
        return hargaSatuan;
    }

    public void setHargaSatuan(double hargaSatuan) {
        this.hargaSatuan = hargaSatuan;
        this.subtotal = this.qty * hargaSatuan;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public MenuKopi getMenu() {
        return menu;
    }

    public void setMenu(MenuKopi menu) {
        this.menu = menu;
    }

    /**
     * Get all details for a transaction header
     */
    public static List<TransaksiDetail> getByTransaksiHeader(int idTransaksiHeader) {
        List<TransaksiDetail> details = new ArrayList<>();
        String sql = "SELECT td.*, m.nama_menu, m.harga, k.nama_kategori " +
                "FROM tbl_transaksi_detail td " +
                "LEFT JOIN tbl_menu m ON td.id_menu = m.id_menu " +
                "LEFT JOIN tbl_kategori k ON m.id_kategori = k.id_kategori " +
                "WHERE td.id_transaksi_header = ? " +
                "ORDER BY td.id_transaksi_detail ASC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idTransaksiHeader);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                details.add(mapResultSetToDetail(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting transaction details for header: {}", idTransaksiHeader, e);
        }

        return details;
    }

    /**
     * Helper method to map ResultSet to TransaksiDetail
     */
    private static TransaksiDetail mapResultSetToDetail(ResultSet rs) throws SQLException {
        TransaksiDetail detail = new TransaksiDetail();
        detail.setIdTransaksiDetail(rs.getInt("id_transaksi_detail"));
        detail.setIdTransaksiHeader(rs.getInt("id_transaksi_header"));
        detail.setIdMenu(rs.getInt("id_menu"));
        detail.setQty(rs.getInt("qty"));
        detail.setHargaSatuan(rs.getDouble("harga_satuan"));
        detail.setSubtotal(rs.getDouble("subtotal"));
        detail.setCreatedAt(rs.getTimestamp("created_at"));

        // Map menu if available
        try {
            MenuKopi menu = new MenuKopi();
            menu.setIdMenu(rs.getInt("id_menu"));
            menu.setNamaMenu(rs.getString("nama_menu"));
            menu.setHarga(rs.getDouble("harga"));

            Kategori kategori = new Kategori();
            kategori.setNamaKategori(rs.getString("nama_kategori"));
            menu.setKategori(kategori);

            detail.setMenu(menu);
        } catch (SQLException e) {
            // Menu fields might not be in the result set
        }

        return detail;
    }

    @Override
    public String toString() {
        return qty + "x " + (menu != null ? menu.getNamaMenu() : "Item #" + idMenu);
    }
}
