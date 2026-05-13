package com.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());

    private static final String URL =
            "jdbc:mysql://yamabiko.proxy.rlwy.net:46438/railway"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC"
            + "&useUnicode=true"
            + "&characterEncoding=UTF-8";

    private static final String USER = "root";
    private static final String PASSWORD = "sKxIkdmTgTPFLUVIYjhNPJCndqpmDKUQ";

    static {
        boolean driverLoaded = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
            LOGGER.info("MySQL JDBC Driver loaded: com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException modernDriverMissing) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                driverLoaded = true;
                LOGGER.warning("Using legacy MySQL JDBC Driver: com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException legacyDriverMissing) {
                LOGGER.severe("MySQL JDBC driver not found.");
            }
        }

        DriverManager.setLoginTimeout(2);

        if (!driverLoaded) {
            throw new RuntimeException("Unable to load MySQL JDBC Driver");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closePool() {
        LOGGER.info("No connection pool to close.");
    }
}