package com.kedaikopi.model;

import com.kedaikopi.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Model User - dengan password hashing dan role-based access
 */
public class User {

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    private int idUser;
    private String username;
    private String password; // Hashed password
    private String role; // Kasir, Owner, Stocker
    private String namaLengkap;
    private boolean isActive;
    private Timestamp lastLogin;
    private Timestamp createdAt;

    // Constructors
    public User() {
    }

    public User(int idUser, String username, String role, String namaLengkap) {
        this.idUser = idUser;
        this.username = username;
        this.role = role;
        this.namaLengkap = namaLengkap;
        this.isActive = true;
    }

    // Getters and Setters
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Business Methods

    /**
     * Hash password menggunakan BCrypt
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }

    /**
     * Verify password
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }

    /**
     * Authenticate user - untuk login
     * TEMPORARY: Support both BCrypt dan plain text untuk testing
     */
    public static User authenticate(String username, String password) {
        String sql = "SELECT * FROM tbl_user WHERE username = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                // TEMPORARY: Check if plain text password (for easy testing)
                // If password in database doesn't start with $2a$ or $2b$, it's plain text
                boolean isPlainText = !storedPassword.startsWith("$2a$") && !storedPassword.startsWith("$2b$");

                if (isPlainText) {
                    // Plain text comparison (TEMPORARY - FOR TESTING ONLY!)
                    if (password.equals(storedPassword)) {
                        User user = mapResultSetToUser(rs);
                        user.updateLastLogin();

                        // Start new session
                        startUserSession(user);

                        logger.info("User authenticated successfully (plain text): {}", username);
                        return user;
                    }
                } else {
                    // BCrypt verification (secure, recommended for production)
                    if (verifyPassword(password, storedPassword)) {
                        User user = mapResultSetToUser(rs);
                        user.updateLastLogin();

                        // Start new session
                        startUserSession(user);

                        logger.info("User authenticated successfully (BCrypt): {}", username);
                        return user;
                    }
                }

                logger.warn("Invalid password for user: {}", username);
            } else {
                logger.warn("User not found or inactive: {}", username);
            }

        } catch (SQLException e) {
            logger.error("Error authenticating user: {}", username, e);
        }

        return null;
    }

    /**
     * Start a new user session (for session tracking)
     */
    private static void startUserSession(User user) {
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT fn_start_session(?, ?, ?, ?)")) {
            stmt.setInt(1, user.getIdUser());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getNamaLengkap());
            stmt.setString(4, user.getRole());
            stmt.executeQuery();
            logger.info("Session started for user: {}", user.getUsername());
        } catch (SQLException e) {
            // Don't fail login if session tracking fails
            logger.warn("Failed to start session for user: {}, continuing anyway", user.getUsername(), e);
        }
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        String sql = "UPDATE tbl_user SET last_login = CURRENT_TIMESTAMP WHERE id_user = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.idUser);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating last login for user: {}", this.username, e);
        }
    }

    /**
     * Get all users
     */
    public static List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM tbl_user ORDER BY id_user ASC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting all users", e);
        }

        return users;
    }

    /**
     * Get user by ID
     */
    public static User getById(int id) {
        String sql = "SELECT * FROM tbl_user WHERE id_user = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            logger.error("Error getting user by ID: {}", id, e);
        }

        return null;
    }

    /**
     * Save user (insert or update)
     */
    public boolean save() {
        if (this.idUser == 0) {
            return insert();
        } else {
            return update();
        }
    }

    /**
     * Insert new user
     */
    private boolean insert() {
        String sql = "INSERT INTO tbl_user (username, password, role, nama_lengkap, is_active) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id_user";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, this.username);
            stmt.setString(2, hashPassword(this.password)); // Hash password
            stmt.setString(3, this.role);
            stmt.setString(4, this.namaLengkap);
            stmt.setBoolean(5, this.isActive);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.idUser = rs.getInt(1);
                logger.info("User inserted successfully: {}", this.username);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error inserting user: {}", this.username, e);
        }

        return false;
    }

    /**
     * Update existing user
     */
    private boolean update() {
        String sql = "UPDATE tbl_user SET username = ?, role = ?, nama_lengkap = ?, is_active = ? " +
                "WHERE id_user = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, this.username);
            stmt.setString(2, this.role);
            stmt.setString(3, this.namaLengkap);
            stmt.setBoolean(4, this.isActive);
            stmt.setInt(5, this.idUser);

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                logger.info("User updated successfully: {}", this.username);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error updating user: {}", this.username, e);
        }

        return false;
    }

    /**
     * Change password
     */
    public boolean changePassword(String newPassword) {
        String sql = "UPDATE tbl_user SET password = ? WHERE id_user = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashPassword(newPassword));
            stmt.setInt(2, this.idUser);

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                logger.info("Password changed successfully for user: {}", this.username);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error changing password for user: {}", this.username, e);
        }

        return false;
    }

    /**
     * Delete user
     */
    public boolean delete() {
        String sql = "DELETE FROM tbl_user WHERE id_user = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.idUser);
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                logger.info("User deleted successfully: {}", this.username);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error deleting user: {}", this.username, e);
        }

        return false;
    }

    /**
     * Helper method to map ResultSet to User object
     */
    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUser(rs.getInt("id_user"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setNamaLengkap(rs.getString("nama_lengkap"));
        user.setActive(rs.getBoolean("is_active"));
        user.setLastLogin(rs.getTimestamp("last_login"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }

    @Override
    public String toString() {
        return namaLengkap != null ? namaLengkap : username;
    }
}
