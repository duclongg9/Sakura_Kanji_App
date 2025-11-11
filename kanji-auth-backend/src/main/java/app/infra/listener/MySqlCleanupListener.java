package app.infra.listener;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet context listener that makes sure MySQL's background cleanup thread
 * and any JDBC drivers registered by the web application are shut down when
 * the application stops. This prevents "Illegal access" warnings from
 * {@link com.mysql.cj.jdbc.AbandonedConnectionCleanupThread} after redeploys.
 */
@WebListener
public class MySqlCleanupListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(MySqlCleanupListener.class.getName());

    /**
     * Releases JDBC resources that belong to this web application when the
     * servlet context is being destroyed.
     *
     * @param servletContextEvent the context destruction event raised by the container
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ClassLoader webAppClassLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();

        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == webAppClassLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                    LOGGER.log(Level.INFO, "Deregistered JDBC driver {0}", driver);
                } catch (SQLException exception) {
                    LOGGER.log(Level.SEVERE, "Failed to deregister JDBC driver", exception);
                }
            }
        }

        AbandonedConnectionCleanupThread.uncheckedShutdown();
        LOGGER.info("MySQL abandoned connection cleanup thread shut down");
    }

    /**
     * No initialization is required for this listener, but the method is
     * provided to satisfy the {@link ServletContextListener} contract.
     *
     * @param servletContextEvent the context initialization event raised by the container
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // No initialization required.
    }
}
