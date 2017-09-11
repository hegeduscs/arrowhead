package eu.arrowhead.core.serviceregistry_sql;


import java.util.Date;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class PingProvidersTask extends TimerTask {

  private static Logger log = Logger.getLogger(ServiceRegistry.class.getName());

  @Override
  public void run() {
    log.debug("Cleaning up SR SQLite at " + new Date().toString());
    pingAndRemoveServices();
  }

  public void pingAndRemoveServices() {
    //TODO do it with databasemanager and registryutils.pinghost
  }
}