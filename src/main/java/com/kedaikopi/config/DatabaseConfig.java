package com.kedaikopi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database Configuration dengan Connection Pooling menggunakan HikariCP
 * Singleton pattern untuk satu instance pool di seluruh aplikasi
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private HikariDataSource dataSource;

    // Database configuration
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/db_kedai_kopi";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "kepanjen45"; //Ganti dengan pass db anda

    // Connection pool configuration
    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final int MINIMUM_IDLE = 2;
    private static final long CONNECTION_TIMEOUT = 30000; // 30 seconds
    private static final long IDLE_TIMEOUT = 600000; // 10 minutes
    private static final long MAX_LIFETIME = 1800000; // 30 minutes

    /**
     * Private constructor - Singleton pattern
     */
    private DatabaseConfig() {
        initializeDataSource();
    }

    /**
     * Get singleton instance
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * Initialize HikariCP DataSource
     */
    private void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();

            // JDBC settings
            config.setJdbcUrl(DB_URL);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);
            config.setDriverClassName("org.postgresql.Driver");

            // Pool settings
            config.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
            config.setMinimumIdle(MINIMUM_IDLE);
            config.setConnectionTimeout(CONNECTION_TIMEOUT);
            config.setIdleTimeout(IDLE_TIMEOUT);
            config.setMaxLifetime(MAX_LIFETIME);

            // Pool name
            config.setPoolName("KedaiKopiPool");

            // Performance settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            // Auto-commit
            config.setAutoCommit(true);

            // Create datasource
            dataSource = new HikariDataSource(config);

            logger.info("Database connection pool initialized");

        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Get DataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Get Database Connection
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or already closed");
        }
        return dataSource.getConnection();
    }

    /**
     * Test database connection
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            logger.info("Database connection test OK");
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }

    /**
     * Get connection pool statistics
     */
    public String getPoolStats() {
        if (dataSource != null) {
            return String.format(
                    "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
        return "Pool not initialized";
    }

    /**
     * Close connection pool - call this on application shutdown
     */
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }

    /**
     * Reinitialize connection pool
     */
    public synchronized void reinitialize() {
        closePool();
        initializeDataSource();
    }
}
