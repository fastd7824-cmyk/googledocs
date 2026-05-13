package com.app.listener;

import com.app.config.DatabaseConfig;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application Lifecycle Listener.
 * - Initializes resources when the web application starts.
 * - Performs cleanup when the application shuts down.
 * - Logs startup/shutdown events.
 * - Verifies database connectivity.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(AppContextListener.class.getName());
    private static final String APP_START_TIME_ATTR = "appStartTime";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        String appName = ctx.getServletContextName();
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        logger.info("=================================================");
        logger.info("Starting Enterprise Document Manager Application");
        logger.info("Application Name: " + (appName != null ? appName : "DocumentManager"));
        logger.info("Start Time: " + startTime);
        logger.info("=================================================");
        
        // Store start time in application scope
        ctx.setAttribute(APP_START_TIME_ATTR, startTime);
        
        // Test database connectivity
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                logger.info("Database connection pool initialized successfully.");
                logger.info("Database product: " + conn.getMetaData().getDatabaseProductName());
                logger.info("Database version: " + conn.getMetaData().getDatabaseProductVersion());
            } else {
                logger.severe("Failed to obtain database connection from pool.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection test FAILED!", e);
            ctx.setAttribute("databaseStartupError", e.getMessage());
            logger.warning("Application will stay deployed, but database-backed features may fail until the database settings are corrected.");
        }
        
        // Set application-wide attributes (e.g., upload path)
        String uploadPath = ctx.getRealPath("/") + "assets/uploads";
        ctx.setAttribute("uploadPath", uploadPath);
        logger.info("File upload directory: " + uploadPath);
        
        logger.info("Application startup complete. Ready to serve requests.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("=================================================");
        logger.info("Shutting down Enterprise Document Manager Application");
        logger.info("Shutdown time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // Close the database connection pool (if any)
        DatabaseConfig.closePool();
        
        logger.info("Application shutdown complete.");
        logger.info("=================================================");
    }
}
