package com.kedaikopi.util;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.category.*;
import org.jfree.data.general.*;
import org.jfree.data.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Factory for creating styled charts for dashboard
 * Provides centralized chart creation with consistent theming
 */
public class ChartFactory {

    private static final Logger logger = LoggerFactory.getLogger(ChartFactory.class);
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    /**
     * Create sales trend line chart (last 7 days)
     */
    public static ChartPanel createSalesTrendChart(Connection conn) {
        try {
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            TimeSeries series = new TimeSeries("Penjualan");

            // Get sales data for last 7 days
            String sql = "SELECT DATE(tanggal) as tanggal, SUM(total_harga) as total " +
                    "FROM tbl_transaksi_header " +
                    "WHERE tanggal >= CURRENT_DATE - INTERVAL '7 days' " +
                    "GROUP BY DATE(tanggal) " +
                    "ORDER BY DATE(tanggal)";

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    java.sql.Date date = rs.getDate("tanggal");
                    double total = rs.getDouble("total");
                    series.add(new Day(date), total);
                }
            }

            dataset.addSeries(series);

            // Create chart with HD+ rendering
            JFreeChart chart = org.jfree.chart.ChartFactory.createTimeSeriesChart(
                    "Tren Penjualan 7 Hari Terakhir",
                    "Tanggal",
                    "Total Penjualan (Rp)",
                    dataset,
                    false, // legend
                    true, // tooltips
                    false // urls
            );

            // Enable HD+ rendering for chart
            chart.setAntiAlias(true);
            chart.setTextAntiAlias(true);

            // Customize chart appearance
            customizeChart(chart);
            XYPlot plot = chart.getXYPlot();

            // Set line renderer
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesPaint(0, ColorScheme.CHART_COLORS[0]);
            renderer.setSeriesStroke(0, new BasicStroke(3.0f));
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8));
            plot.setRenderer(renderer);

            // Customize axes
            DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
            domainAxis.setDateFormatOverride(new SimpleDateFormat("dd MMM"));
            domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));

            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setNumberFormatOverride(currencyFormat);
            rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));

            return new ChartPanel(chart);

        } catch (SQLException e) {
            logger.error("Error loading sales trend chart data", e);
            return createErrorChart("Error loading sales trend");
        }
    }

    /**
     * Create category distribution pie chart
     */
    public static ChartPanel createCategoryDistributionChart(Connection conn) {
        try {
            DefaultPieDataset dataset = new DefaultPieDataset();

            // Get sales by category
            String sql = "SELECT k.nama_kategori, SUM(td.subtotal) as total " +
                    "FROM tbl_transaksi_detail td " +
                    "JOIN tbl_menu m ON td.id_menu = m.id_menu " +
                    "JOIN tbl_kategori k ON m.id_kategori = k.id_kategori " +
                    "JOIN tbl_transaksi_header th ON td.id_transaksi_header = th.id_transaksi_header " +
                    "WHERE th.tanggal >= CURRENT_DATE - INTERVAL '30 days' " +
                    "GROUP BY k.nama_kategori " +
                    "ORDER BY total DESC";

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String category = rs.getString("nama_kategori");
                    double total = rs.getDouble("total");
                    dataset.setValue(category, total);
                }
            }

            // Create chart with HD+ rendering
            JFreeChart chart = org.jfree.chart.ChartFactory.createPieChart(
                    "Distribusi Penjualan per Kategori",
                    dataset,
                    true, // legend
                    true, // tooltips
                    false // urls
            );

            // Enable HD+ rendering
            chart.setAntiAlias(true);
            chart.setTextAntiAlias(true);

            // Customize chart appearance
            customizeChart(chart);
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setBackgroundPaint(ColorScheme.CHART_BG);
            plot.setOutlineVisible(false);
            plot.setShadowPaint(null);
            plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));

            // Set colors for pie sections
            int colorIndex = 0;
            for (Object key : dataset.getKeys()) {
                plot.setSectionPaint((Comparable) key,
                        ColorScheme.CHART_COLORS[colorIndex % ColorScheme.CHART_COLORS.length]);
                colorIndex++;
            }

            return new ChartPanel(chart);

        } catch (SQLException e) {
            logger.error("Error loading category distribution chart data", e);
            return createErrorChart("Error loading category distribution");
        }
    }

    /**
     * Create top products bar chart
     */
    public static ChartPanel createTopProductsChart(Connection conn) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            // Get top 10 selling products
            String sql = "SELECT m.nama_menu, SUM(td.qty) as total_qty " +
                    "FROM tbl_transaksi_detail td " +
                    "JOIN tbl_menu m ON td.id_menu = m.id_menu " +
                    "JOIN tbl_transaksi_header th ON td.id_transaksi_header = th.id_transaksi_header " +
                    "WHERE th.tanggal >= CURRENT_DATE - INTERVAL '30 days' " +
                    "GROUP BY m.nama_menu " +
                    "ORDER BY total_qty DESC " +
                    "LIMIT 10";

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String menuName = rs.getString("nama_menu");
                    int qty = rs.getInt("total_qty");
                    dataset.addValue(qty, "Terjual", menuName);
                }
            }

            // Create chart with HD+ rendering
            JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
                    "Top 10 Menu Terlaris (30 Hari)",
                    "Menu",
                    "Jumlah Terjual",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false, // legend
                    true, // tooltips
                    false // urls
            );

            // Enable HD+ rendering
            chart.setAntiAlias(true);
            chart.setTextAntiAlias(true);

            // Customize chart appearance
            customizeChart(chart);
            CategoryPlot plot = chart.getCategoryPlot();

            // Set bar renderer
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, ColorScheme.CHART_COLORS[1]);
            renderer.setBarPainter(new StandardBarPainter());
            renderer.setShadowVisible(false);
            renderer.setMaximumBarWidth(0.08);

            // Customize axes
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

            return new ChartPanel(chart);

        } catch (SQLException e) {
            logger.error("Error loading top products chart data", e);
            return createErrorChart("Error loading top products");
        }
    }

    /**
     * Customize chart appearance to match application theme
     */
    private static void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(ColorScheme.CHART_BG);
        chart.setBorderVisible(false);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        // Title styling
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
        chart.getTitle().setPaint(ColorScheme.TEXT_PRIMARY);

        // Plot styling
        Plot plot = chart.getPlot();
        plot.setBackgroundPaint(ColorScheme.CHART_BG);
        plot.setOutlineVisible(false);

        if (plot instanceof XYPlot) {
            XYPlot xyPlot = (XYPlot) plot;
            xyPlot.setDomainGridlinePaint(ColorScheme.CHART_GRID);
            xyPlot.setRangeGridlinePaint(ColorScheme.CHART_GRID);
        } else if (plot instanceof CategoryPlot) {
            CategoryPlot categoryPlot = (CategoryPlot) plot;
            categoryPlot.setDomainGridlinePaint(ColorScheme.CHART_GRID);
            categoryPlot.setRangeGridlinePaint(ColorScheme.CHART_GRID);
        }
    }

    /**
     * Create error chart when data loading fails
     */
    private static ChartPanel createErrorChart(String errorMessage) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(0, "Error", "Error");

        JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
                errorMessage,
                "",
                "",
                dataset);
        customizeChart(chart);

        return new ChartPanel(chart);
    }
}
