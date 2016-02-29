package eu.arrowhead.common.listener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.jdbc.JDBCAppender;
import org.hibernate.cfg.Configuration;

public class ServletContextClass implements ServletContextListener {

	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("[Arrowhead Core] Servlet deployed.");

		// Access Hibernate config data
		Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

		// Configure Log4j appender
		JDBCAppender appender = new JDBCAppender();
		appender.setURL(configuration.getProperty("hibernate.connection.url"));
		appender.setUser(configuration.getProperty("hibernate.connection.username"));
		appender.setPassword(configuration.getProperty("hibernate.connection.password"));
		appender.setDriver(configuration.getProperty("hibernate.connection.driver_class"));
		appender.setSql(
				"INSERT INTO LOGS VALUES(DEFAULT,'%x','%d{yyyy-MM-dd HH:mm:ss}','%C','%p','%m',DEFAULT,DEFAULT)");

		// Set appender and the appropriate log level
		Logger.getRootLogger().addAppender(appender);
		Logger.getRootLogger().setLevel(Level.DEBUG);

	}

	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("[Arrowhead Core] Servlet destroyed.");

		// This manually deregisters JDBC driver, which prevents Tomcat 7 from
		// complaining about memory leaks wrto this class
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
			} catch (SQLException e) {

			}

		}
	}

}