package com.kedaikopi.model;

import com.kedaikopi.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shift Model - Represents work shift definition
 */
public class Shift {

    private static final Logger logger = LoggerFactory.getLogger(Shift.class);

    private int idShift;
    private String shiftName;
    private Time startTime;
    private Time endTime;
    private String colorCode;
    private boolean isActive;
    private Timestamp createdAt;

    // Constructors
    public Shift() {
    }

    public Shift(int idShift, String shiftName, Time startTime, Time endTime, String colorCode) {
        this.idShift = idShift;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.colorCode = colorCode;
        this.isActive = true;
    }

    // CRUD operations

    /**
     * Get all active shifts
     */
    public static List<Shift> getAllShifts() {
        List<Shift> shifts = new ArrayList<>();
        String sql = "SELECT * FROM tbl_shift WHERE is_active = true ORDER BY start_time";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                shifts.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting shifts: {}", e.getMessage(), e);
        }

        return shifts;
    }

    /**
     * Get shift by ID
     */
    public static Shift getById(int idShift) {
        String sql = "SELECT * FROM tbl_shift WHERE id_shift = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idShift);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }

        } catch (SQLException e) {
            logger.error("Error getting shift {}: {}", idShift, e.getMessage(), e);
        }

        return null;
    }

    /**
     * Save new shift
     */
    public boolean save() {
        String sql = "INSERT INTO tbl_shift (shift_name, start_time, end_time, color_code) " +
                "VALUES (?, ?, ?, ?) RETURNING id_shift";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, shiftName);
            stmt.setTime(2, startTime);
            stmt.setTime(3, endTime);
            stmt.setString(4, colorCode);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.idShift = rs.getInt(1);
                logger.info("Shift created: {}", shiftName);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error saving shift: {}", e.getMessage(), e);
        }

        return false;
    }

    /**
     * Update existing shift
     */
    public boolean update() {
        String sql = "UPDATE tbl_shift SET shift_name = ?, start_time = ?, " +
                "end_time = ?, color_code = ? WHERE id_shift = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, shiftName);
            stmt.setTime(2, startTime);
            stmt.setTime(3, endTime);
            stmt.setString(4, colorCode);
            stmt.setInt(5, idShift);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("Shift updated: {}", shiftName);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error updating shift: {}", e.getMessage(), e);
        }

        return false;
    }

    /**
     * Delete shift (soft delete)
     */
    public boolean delete() {
        String sql = "UPDATE tbl_shift SET is_active = false WHERE id_shift = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idShift);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                logger.info("Shift deleted: {}", shiftName);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error deleting shift: {}", e.getMessage(), e);
        }

        return false;
    }

    private static Shift mapResultSet(ResultSet rs) throws SQLException {
        Shift shift = new Shift();
        shift.idShift = rs.getInt("id_shift");
        shift.shiftName = rs.getString("shift_name");
        shift.startTime = rs.getTime("start_time");
        shift.endTime = rs.getTime("end_time");
        shift.colorCode = rs.getString("color_code");
        shift.isActive = rs.getBoolean("is_active");
        shift.createdAt = rs.getTimestamp("created_at");
        return shift;
    }

    // Getters and setters
    public int getIdShift() {
        return idShift;
    }

    public void setIdShift(int idShift) {
        this.idShift = idShift;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
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

    @Override
    public String toString() {
        return shiftName;
    }
}
