package com.kedaikopi.model;

import com.kedaikopi.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for user activity log
 * Tracks employee login/logout events
 */
public class UserActivityLog {

    private static final Logger logger = LoggerFactory.getLogger(UserActivityLog.class);

    private int idActivity;
    private User user;
    private String activityType; // LOGIN or LOGOUT
    private Timestamp activityTime;
    private String ipAddress;
    private String deviceInfo;
    private String sessionNote;

    // Constructors
    public UserActivityLog() {
    }

    public UserActivityLog(User user, String activityType) {
        this.user = user;
        this.activityType = activityType;
        this.activityTime = Timestamp.valueOf(LocalDateTime.now());
    }

    // Static methods for database operations

    /**
     * Log a login event
     */
    public static boolean logLogin(int userId) {
        return logActivity(userId, "LOGIN", null);
    }

    /**
     * Log a logout event
     */
    public static boolean logLogout(int userId) {
        return logLogout(userId, null);
    }

    /**
     * Log a logout event with note
     */
    public static boolean logLogout(int userId, String note) {
        return logActivity(userId, "LOGOUT", note);
    }

    /**
     * Generic method to log any activity
     */
    private static boolean logActivity(int userId, String activityType, String note) {
        String sql = "INSERT INTO tbl_user_activity_log " +
                "(id_user, activity_type, session_note) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, activityType);
            stmt.setString(3, note);

            int rows = stmt.executeUpdate();
            logger.info("Activity logged: user_id={}, type={}, note={}", userId, activityType, note);
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error logging activity for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get all activity logs for a specific date
     */
    public static List<UserActivityLog> getByDate(java.sql.Date date) {
        List<UserActivityLog> logs = new ArrayList<>();
        String sql = "SELECT a.*, u.username, u.nama_lengkap, u.role " +
                "FROM tbl_user_activity_log a " +
                "JOIN tbl_user u ON a.id_user = u.id_user " +
                "WHERE DATE(a.activity_time) = ? " +
                "ORDER BY a.activity_time DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, date);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            logger.error("Error fetching activity logs by date: {}", e.getMessage(), e);
        }

        return logs;
    }

    /**
     * Get activity logs for a date range
     */
    public static List<UserActivityLog> getByDateRange(java.sql.Date startDate, java.sql.Date endDate) {
        List<UserActivityLog> logs = new ArrayList<>();
        String sql = "SELECT a.*, u.username, u.nama_lengkap, u.role " +
                "FROM tbl_user_activity_log a " +
                "JOIN tbl_user u ON a.id_user = u.id_user " +
                "WHERE DATE(a.activity_time) BETWEEN ? AND ? " +
                "ORDER BY a.activity_time DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            logger.error("Error fetching activity logs by date range: {}", e.getMessage(), e);
        }

        return logs;
    }

    /**
     * Get activity logs for a specific user
     */
    public static List<UserActivityLog> getByUser(int userId, java.sql.Date startDate, java.sql.Date endDate) {
        List<UserActivityLog> logs = new ArrayList<>();
        String sql = "SELECT a.*, u.username, u.nama_lengkap, u.role " +
                "FROM tbl_user_activity_log a " +
                "JOIN tbl_user u ON a.id_user = u.id_user " +
                "WHERE a.id_user = ? AND DATE(a.activity_time) BETWEEN ? AND ? " +
                "ORDER BY a.activity_time DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            logger.error("Error fetching activity logs for user {}: {}", userId, e.getMessage(), e);
        }

        return logs;
    }

    /**
     * Get inactive employees (not logged in for X days)
     */
    public static List<InactiveEmployee> getInactiveEmployees(int daysThreshold) {
        List<InactiveEmployee> inactive = new ArrayList<>();
        String sql = "SELECT * FROM vw_inactive_employees " +
                "WHERE days_inactive >= ? " +
                "ORDER BY days_inactive DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, daysThreshold);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                InactiveEmployee ie = new InactiveEmployee();
                ie.idUser = rs.getInt("id_user");
                ie.username = rs.getString("username");
                ie.namaLengkap = rs.getString("nama_lengkap");
                ie.role = rs.getString("role");
                ie.lastLogin = rs.getTimestamp("last_login");
                ie.daysInactive = rs.getInt("days_inactive");
                inactive.add(ie);
            }

        } catch (SQLException e) {
            logger.error("Error fetching inactive employees: {}", e.getMessage(), e);
        }

        return inactive;
    }

    /**
     * Get count of inactive employees
     */
    public static int getInactiveEmployeeCount(int daysThreshold) {
        String sql = "SELECT COUNT(*) FROM vw_inactive_employees WHERE days_inactive >= ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, daysThreshold);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.error("Error counting inactive employees: {}", e.getMessage(), e);
        }

        return 0;
    }

    /**
     * Get currently active sessions (logged in today, not logged out yet)
     */
    public static List<UserActivityLog> getActiveSessions() {
        List<UserActivityLog> sessions = new ArrayList<>();
        String sql = "SELECT u.id_user, u.username, u.nama_lengkap, u.role, " +
                "login.activity_time, login.id_activity " +
                "FROM tbl_user u " +
                "JOIN tbl_user_activity_log login ON u.id_user = login.id_user " +
                "WHERE login.activity_type = 'LOGIN' " +
                "AND DATE(login.activity_time) = CURRENT_DATE " +
                "AND NOT EXISTS ( " +
                "    SELECT 1 FROM tbl_user_activity_log logout " +
                "    WHERE logout.id_user = login.id_user " +
                "    AND logout.activity_type = 'LOGOUT' " +
                "    AND logout.activity_time > login.activity_time " +
                "    AND DATE(logout.activity_time) = CURRENT_DATE " +
                ") " +
                "ORDER BY login.activity_time DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserActivityLog log = new UserActivityLog();
                log.idActivity = rs.getInt("id_activity");
                log.activityTime = rs.getTimestamp("activity_time");
                log.activityType = "LOGIN";

                User u = new User();
                u.setIdUser(rs.getInt("id_user"));
                u.setUsername(rs.getString("username"));
                u.setNamaLengkap(rs.getString("nama_lengkap"));
                u.setRole(rs.getString("role"));
                log.user = u;

                sessions.add(log);
            }

        } catch (SQLException e) {
            logger.error("Error fetching active sessions: {}", e.getMessage(), e);
        }

        return sessions;
    }

    /**
     * Calculate session duration in hours
     */
    public static double calculateSessionDuration(int loginActivityId) {
        String sql = "SELECT " +
                "EXTRACT(EPOCH FROM (logout.activity_time - login.activity_time))/3600 as hours " +
                "FROM tbl_user_activity_log login " +
                "LEFT JOIN LATERAL ( " +
                "    SELECT activity_time " +
                "    FROM tbl_user_activity_log " +
                "    WHERE id_user = login.id_user " +
                "    AND activity_type = 'LOGOUT' " +
                "    AND activity_time > login.activity_time " +
                "    AND DATE(activity_time) = DATE(login.activity_time) " +
                "    ORDER BY activity_time " +
                "    LIMIT 1 " +
                ") logout ON true " +
                "WHERE login.id_activity = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loginActivityId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("hours");
            }

        } catch (SQLException e) {
            logger.error("Error calculating session duration: {}", e.getMessage(), e);
        }

        return 0.0;
    }

    /**
     * Map ResultSet to UserActivityLog object
     */
    private static UserActivityLog mapResultSet(ResultSet rs) throws SQLException {
        UserActivityLog log = new UserActivityLog();
        log.idActivity = rs.getInt("id_activity");
        log.activityType = rs.getString("activity_type");
        log.activityTime = rs.getTimestamp("activity_time");
        log.ipAddress = rs.getString("ip_address");
        log.deviceInfo = rs.getString("device_info");
        log.sessionNote = rs.getString("session_note");

        User user = new User();
        user.setIdUser(rs.getInt("id_user"));
        user.setUsername(rs.getString("username"));
        user.setNamaLengkap(rs.getString("nama_lengkap"));
        user.setRole(rs.getString("role"));
        log.user = user;

        return log;
    }

    // Getters and setters
    public int getIdActivity() {
        return idActivity;
    }

    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Timestamp getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(Timestamp activityTime) {
        this.activityTime = activityTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getSessionNote() {
        return sessionNote;
    }

    public void setSessionNote(String sessionNote) {
        this.sessionNote = sessionNote;
    }

    /**
     * Inner class for inactive employee data
     */
    public static class InactiveEmployee {
        public int idUser;
        public String username;
        public String namaLengkap;
        public String role;
        public Timestamp lastLogin;
        public int daysInactive;
    }
}
