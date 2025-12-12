package com.kedaikopi.model;

import com.kedaikopi.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Model MenuKopi - menu items dengan relasi ke Kategori
 */
public class MenuKopi {

    private static final Logger logger = LoggerFactory.getLogger(MenuKopi.class);

    private int idMenu;
    private String namaMenu;
    private Kategori kategori;
    private double harga;
    private int stok;
    private String imagePath;
    private String deskripsi;
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructors
    public MenuKopi() {
        this.kategori = new Kategori();
    }

    // Getters and Setters
    public int getIdMenu() {
        return idMenu;
    }

    public void setIdMenu(int idMenu) {
        this.idMenu = idMenu;
    }

    public String getNamaMenu() {
        return namaMenu;
    }

    public void setNamaMenu(String namaMenu) {
        this.namaMenu = namaMenu;
    }

    public Kategori getKategori() {
        return kategori;
    }

    public void setKategori(Kategori kategori) {
        this.kategori = kategori;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business Methods

    /**
     * Get all menu items
     */
    public static List<MenuKopi> getAll() {
        List<MenuKopi> menus = new ArrayList<>();
        String sql = "SELECT m.*, k.nama_kategori, k.icon_name " +
                "FROM tbl_menu m " +
                "LEFT JOIN tbl_kategori k ON m.id_kategori = k.id_kategori " +
                "ORDER BY m.id_menu ASC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                menus.add(mapResultSetToMenu(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting all menu items", e);
        }

        return menus;
    }

    /**
     * Get active menu items only
     */
    public static List<MenuKopi> getAllActive() {
        List<MenuKopi> menus = new ArrayList<>();
        String sql = "SELECT m.*, k.nama_kategori " +
                "FROM tbl_menu m " +
                "LEFT JOIN tbl_kategori k ON m.id_kategori = k.id_kategori " +
                "WHERE m.is_active = true " +
                "ORDER BY k.nama_kategori, m.nama_menu";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                menus.add(mapResultSetToMenu(rs));
            }

            logger.info("Loaded {} active menu items", menus.size());

        } catch (SQLException e) {
            logger.error("Error getting active menu items", e);
            e.printStackTrace();
        }

        return menus;
    }

    /**
     * Get menu by ID
     */
    public static MenuKopi getById(int id) {
        String sql = "SELECT m.*, k.nama_kategori, k.icon_name " +
                "FROM tbl_menu m " +
                "LEFT JOIN tbl_kategori k ON m.id_kategori = k.id_kategori " +
                "WHERE m.id_menu = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMenu(rs);
            }

        } catch (SQLException e) {
            logger.error("Error getting menu by ID: {}", id, e);
        }

        return null;
    }

    /**
     * Save menu (insert or update)
     */
    public boolean save() {
        if (this.idMenu == 0) {
            return insert();
        } else {
            return update();
        }
    }

    /**
     * Insert new menu
     */
    private boolean insert() {
        String sql = "INSERT INTO tbl_menu (nama_menu, id_kategori, harga, stok, image_path, deskripsi, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_menu";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, this.namaMenu);
            stmt.setInt(2, this.kategori.getIdKategori());
            stmt.setDouble(3, this.harga);
            stmt.setInt(4, this.stok);
            stmt.setString(5, this.imagePath);
            stmt.setString(6, this.deskripsi);
            stmt.setBoolean(7, this.isActive);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.idMenu = rs.getInt(1);
                logger.info("Menu inserted: {}", this.namaMenu);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error inserting menu: {}", this.namaMenu, e);
        }

        return false;
    }

    /**
     * Update existing menu
     */
    private boolean update() {
        String sql = "UPDATE tbl_menu SET nama_menu = ?, id_kategori = ?, harga = ?, " +
                "stok = ?, image_path = ?, deskripsi = ?, is_active = ? WHERE id_menu = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, this.namaMenu);
            stmt.setInt(2, this.kategori.getIdKategori());
            stmt.setDouble(3, this.harga);
            stmt.setInt(4, this.stok);
            stmt.setString(5, this.imagePath);
            stmt.setString(6, this.deskripsi);
            stmt.setBoolean(7, this.isActive);
            stmt.setInt(8, this.idMenu);

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                logger.info("Menu updated: {}", this.namaMenu);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error updating menu: {}", this.namaMenu, e);
        }

        return false;
    }

    /**
     * Delete menu
     */
    public boolean delete() {
        String sql = "DELETE FROM tbl_menu WHERE id_menu = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.idMenu);
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                logger.info("Menu deleted: {}", this.namaMenu);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error deleting menu: {}", this.namaMenu, e);
        }

        return false;
    }

    /**
     * Reduce stock - called when item is sold
     */
    public boolean kurangiStok(int jumlah) {
        String sql = "UPDATE tbl_menu SET stok = stok - ? WHERE id_menu = ? AND stok >= ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, jumlah);
            stmt.setInt(2, this.idMenu);
            stmt.setInt(3, jumlah);

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                this.stok -= jumlah;
                logger.info("Stock reduced for {}: -{}", this.namaMenu, jumlah);
                return true;
            } else {
                logger.warn("Insufficient stock for {}", this.namaMenu);
                return false;
            }

        } catch (SQLException e) {
            logger.error("Error reducing stock for: {}", this.namaMenu, e);
        }

        return false;
    }

    /**
     * Reduce stock using existing connection (for transactions)
     */
    public boolean kurangiStok(int jumlah, Connection conn) {
        String sql = "UPDATE tbl_menu SET stok = stok - ? WHERE id_menu = ? AND stok >= ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, jumlah);
            stmt.setInt(2, this.idMenu);
            stmt.setInt(3, jumlah);

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                this.stok -= jumlah;
                logger.info("Stock reduced for {}: -{}", this.namaMenu, jumlah);
                return true;
            } else {
                logger.warn("Insufficient stock for {}", this.namaMenu);
                return false;
            }

        } catch (SQLException e) {
            logger.error("Error reducing stock for: {}", this.namaMenu, e);
        }

        return false;
    }

    /**
     * Check if stock is low (< 10)
     */
    public boolean isLowStock() {
        return this.stok < 10;
    }

    /**
     * Helper method to map ResultSet to MenuKopi object
     */
    private static MenuKopi mapResultSetToMenu(ResultSet rs) throws SQLException {
        MenuKopi menu = new MenuKopi();
        menu.setIdMenu(rs.getInt("id_menu"));
        menu.setNamaMenu(rs.getString("nama_menu"));

        // Map kategori
        Kategori kategori = new Kategori();
        kategori.setIdKategori(rs.getInt("id_kategori"));
        kategori.setNamaKategori(rs.getString("nama_kategori"));
        kategori.setIconName(rs.getString("icon_name"));
        menu.setKategori(kategori);

        menu.setHarga(rs.getDouble("harga"));
        menu.setStok(rs.getInt("stok"));
        menu.setImagePath(rs.getString("image_path"));
        menu.setDeskripsi(rs.getString("deskripsi"));
        menu.setActive(rs.getBoolean("is_active"));
        menu.setCreatedAt(rs.getTimestamp("created_at"));
        menu.setUpdatedAt(rs.getTimestamp("updated_at"));

        return menu;
    }

    @Override
    public String toString() {
        return namaMenu;
    }
}
