package com.kedaikopi.test;

import com.kedaikopi.config.DatabaseConfig;

import java.sql.*;

/**
 * Test utility to directly query database and check menu count
 */
public class TestMenuQuery {

    public static void main(String[] args) {
        System.out.println("=== TESTING MENU QUERY ===");

        String sql = "SELECT m.*, k.nama_kategori, k.icon_name " +
                "FROM tbl_menu m " +
                "LEFT JOIN tbl_kategori k ON m.id_kategori = k.id_kategori " +
                "WHERE m.is_active = TRUE " +
                "ORDER BY m.nama_menu ASC";

        System.out.println("SQL: " + sql);
        System.out.println();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            int count = 0;
            System.out.println("Results from database:");
            System.out.println("--------------------------------------------------");

            while (rs.next()) {
                count++;
                int id = rs.getInt("id_menu");
                String nama = rs.getString("nama_menu");
                String kategori = rs.getString("nama_kategori");
                double harga = rs.getDouble("harga");
                int stok = rs.getInt("stok");
                boolean isActive = rs.getBoolean("is_active");

                System.out.printf("%2d. %-20s | %-15s | Rp%,10.0f | Stok: %3d | Active: %s%n",
                        count, nama, kategori, harga, stok, isActive);
            }

            System.out.println("--------------------------------------------------");
            System.out.println("TOTAL MENUS FOUND: " + count);

            if (count < 10) {
                System.out.println("\n⚠ WARNING: Found less than 10 menus!");
                System.out.println("   Expected: 10");
                System.out.println("   Found: " + count);
            } else {
                System.out.println("\n✓ All 10 menus found in database!");
            }

        } catch (SQLException e) {
            System.err.println("ERROR querying database:");
            e.printStackTrace();
        }

        System.out.println("\n=== TEST COMPLETED ===");
    }
}
