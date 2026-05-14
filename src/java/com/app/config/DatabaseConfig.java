  
/*  package com.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseConfig {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static final String[] MYSQL_DRIVERS = {
        "com.mysql.jdbc.Driver",
        "com.mysql.jdbc.Driver"
    };
    private static final String URL = "jdbc:mysql://yamabiko.proxy.rlwy.net:46438/railway?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "sKxIkdmTgTPFLUVIYjhNPJCndqpmDKUQ";

    static {
        boolean driverLoaded = false;
        for (String driverClass : MYSQL_DRIVERS) {
            try {
                Class.forName(driverClass);
                LOGGER.info("MySQL JDBC Driver registered: " + driverClass);
                driverLoaded = true;
                break;
            } catch (ClassNotFoundException ignored) {
                // Try the next supported MySQL driver class name.
            }
        }

        if (!driverLoaded) {
            LOGGER.severe("MySQL JDBC Driver not found. Tried: com.mysql.jdbc.Driver, com.mysql.jdbc.Driver");
            throw new RuntimeException("MySQL JDBC Driver not found");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closePool() {
        // Nothing to close - connections are closed individually.
        LOGGER.info("No connection pool to close.");
    }
}


*/
package com.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseConfig {

    private static final Logger LOGGER =
            Logger.getLogger(DatabaseConfig.class.getName());

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
            LOGGER.info("Modern MySQL JDBC Driver loaded.");
            driverLoaded = true;

        } catch (ClassNotFoundException modernDriverMissing) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                LOGGER.warning("Legacy MySQL JDBC Driver loaded.");
                driverLoaded = true;

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