/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.serviceregistry;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class RemoveExpiredServicesTask extends TimerTask {

  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final Logger log = Logger.getLogger(RemoveExpiredServicesTask.class.getName());

  private ServiceRegistryEntry entry;

  public RemoveExpiredServicesTask() {
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
        if (entry.getEndOfValidity() != null) {
          if (LocalDateTime.now().isAfter(entry.getEndOfValidity())) {
            dm.delete(entry);
            deleteCount++;
          } else {
            long ttl = Duration.between(LocalDateTime.now(), entry.getEndOfValidity()).toMillis();
            if (ttl < (ServiceRegistryService.TTL_INTERVAL * 60 * 1000
                + 200)) { // minutes -> milliseconds conversion + 200 extra ms to avoid edge cases
              TimerTask removeTask = new RemoveExpiredServicesTask(entry);
              Timer timer = new Timer();
              timer.schedule(removeTask, ttl);
            }
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
