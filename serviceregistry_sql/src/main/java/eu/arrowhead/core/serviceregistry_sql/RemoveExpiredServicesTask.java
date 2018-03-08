package eu.arrowhead.core.serviceregistry_sql;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

class RemoveExpiredServicesTask extends TimerTask {

  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final Logger log = Logger.getLogger(RemoveExpiredServicesTask.class.getName());

  private ServiceRegistryEntry entry;

  RemoveExpiredServicesTask() {
  }

  private RemoveExpiredServicesTask(ServiceRegistryEntry entry) {
    this.entry = entry;
  }

  @Override
  public void run() {
    int deleteCount = 0;
    if (entry == null) {
      List<ServiceRegistryEntry> srList = dm.getAll(ServiceRegistryEntry.class, null);

      for (ServiceRegistryEntry entry : srList) {
        if (LocalDateTime.now().isAfter(entry.getEndOfValidity())) {
          dm.delete(entry);
          deleteCount++;
        } else {
          int ttl = (int) Duration.between(entry.getEndOfValidity(), LocalDateTime.now()).toMillis();
          if (ttl < 300 * 1000) { // Time to Live < 5 minutes
            TimerTask removeTask = new RemoveExpiredServicesTask(entry);
            Timer timer = new Timer();
            timer.schedule(removeTask, 0L, ttl);
          }
        }
      }
    } else {
      dm.delete(entry);
      deleteCount++;
    }

    log.debug("Removed " + deleteCount + " expired entries from SR database at " + LocalDateTime.now());
  }

}
