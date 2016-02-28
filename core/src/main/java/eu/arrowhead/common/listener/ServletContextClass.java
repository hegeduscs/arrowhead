package eu.arrowhead.common.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextClass implements ServletContextListener {

	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("Servlet deployed.");
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("Servlet destroyed.");
	}

}