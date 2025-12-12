package com.kedaikopi.model;

import com.kedaikopi.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Model Kategori - category untuk menu items
 */
public class Kategori {

    private static final Logger logger = LoggerFactory.getLogger(Kategori.class);

    private int idKategori;
    private String namaKategori;
    private String iconName;
    private Timestamp createdAt;

    // Constructors
    public Kategori() {
    }

    public Kategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }

    // Getters and Setters
    public int getIdKategori() {
        return idKategori;
    }

    public void setIdKategori(int idKategori) {
        this.idKategori = idKategori;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public void setNamaKategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Business Methods

    /**
     * Get all categories
     */
    public static List<Kategori> getAll() {
        List<Kategori> categories = new ArrayList<>();
        String sql = "SELECT * FROM tbl_kategori ORDER BY id_kategori ASC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapResultSetToKategori(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting all categories", e);
        }

        return categories;
    }

    /**
     * Get category by ID
     */
    public static Kategori getById(int id) {
        String sql = "SELECT * FROM tbl_kategori WHERE id_kategori = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToKategori(rs);
            }

        } catch (SQLException e) {
            logger.error("Error getting category by ID: {}", id, e);
        }

        return null;
    }

    /**
     * Save category (insert or update)
     */
    public boolean save() {
        if (this.idKategori == 0) {
            return insert();
        } else {
            return update();
        }
    }

    /**
     * Insert new category
     */
    private boolean insert() {
        String sql = "INSERT INTO tbl_kategori (nama_kategori, icon_name) VALUES (?, ?) RETURNING id_kategori";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, this.namaKategori);
            stmt.setString(2, this.iconName != null ? this.iconName : "category");

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.idKategori = rs.getInt(1);
                logger.info("Category inserted: {}", this.namaKategori);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error inserting category: {}", this.namaKategori, e);
        }

        return false;
    }

    /**
     * Update existing category
     */
    private boolean update() {
        String sql = "UPDATE tbl_kategori SET nama_kategori = ?, icon_name = ? WHERE id_kategori = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, this.namaKategori);
            stmt.setString(2, this.iconName != null ? this.iconName : "category");
            stmt.setInt(3, this.idKategori);

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                logger.info("Category updated: {}", this.namaKategori);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error updating category: {}", this.namaKategori, e);
        }

        return false;
    }

    /**
     * Delete category
     */
    public boolean delete() {
        String sql = "DELETE FROM tbl_kategori WHERE id_kategori = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.idKategori);
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                logger.info("Category deleted: {}", this.namaKategori);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error deleting category: {}", this.namaKategori, e);
        }

        return false;
    }

    /**
     * Helper method to map ResultSet to Kategori object
     */
    private static Kategori mapResultSetToKategori(ResultSet rs) throws SQLException {
        Kategori kategori = new Kategori();
        kategori.setIdKategori(rs.getInt("id_kategori"));
        kategori.setNamaKategori(rs.getString("nama_kategori"));
        kategori.setIconName(rs.getString("icon_name"));
        kategori.setCreatedAt(rs.getTimestamp("created_at"));
        return kategori;
    }

    @Override
    public String toString() {
        return namaKategori;
    }
}
