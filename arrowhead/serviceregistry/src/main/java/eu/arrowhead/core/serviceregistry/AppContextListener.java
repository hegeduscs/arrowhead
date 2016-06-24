package eu.arrowhead.core.serviceregistry;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("Serviceregistry Listener Initialized.");

		TimerTask pingTask = new PingTask();
		Timer timer = new Timer();
		timer.schedule(pingTask, 60000l, (2l * 60l * 1000l));

	}

	class PingTask extends TimerTask {

		@Override
		public void run() {
			System.out.println("TimerTask " + new Date().toString());
			ServiceRegistry.getInstance().pingAndRemoveServices();			
		}
	}
}
